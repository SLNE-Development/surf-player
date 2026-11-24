#!/bin/sh
# Automated in-game player tests of surf-player against the local Docker test network
# (docker/test-network). One entry point:
#
#   test/player-tests/run.sh [--no-build] [--no-deploy] [scenario filters ...]
#
#   1. builds the surf-player jars when sources are newer than the deployed jars
#   2. deploys them (fetch-plugins.sh, which checks every jar with unzip -t) and restarts the
#      servers whose jars changed
#   3. waits until Paper, Minestom, the microservice and both proxies are up
#   4. runs the scenarios with the bots DevAdmin, DevPlayer1, DevPlayer2 (offline proxy :25566)
#   5. writes build/report.json (and merges into a test plan with --plan <tests.json>)
#   6. prints a summary; the exit code is 1 if any scenario is "bad"
#
# Filters are passed to the runner: --feature chat, --id friends-12, --id clan-*, --failed,
# --plan <tests.json>, --destructive, --list. See README.md in this directory.
set -eu

HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/../.." && pwd)
NET="$ROOT/docker/test-network"
# Git Bash on Windows rewrites arguments that look like paths ("/msg") without this.
export MSYS_NO_PATHCONV=1
export MSYS2_ARG_CONV_EXCL='*'

BUILD=1
DEPLOY=1
for arg in "$@"; do
    case "$arg" in
        --no-build) BUILD=0 ;;
        --no-deploy) DEPLOY=0 ;;
    esac
done
# drop our own flags, keep the runner's
for arg in "$@"; do
    shift
    case "$arg" in
        --no-build|--no-deploy) ;;
        *) set -- "$@" "$arg" ;;
    esac
done

# Run compose from the network directory: Docker Desktop on Windows cannot resolve Git Bash
# paths like /s/... given with -f.
compose() { (cd "$NET" && docker compose -p surf-player-test "$@"); }
# The JVM needs a native path on Windows (S:/...), cygpath exists only in Git Bash/Cygwin.
if command -v cygpath >/dev/null 2>&1; then NET_NATIVE=$(cygpath -m "$NET"); else NET_NATIVE=$NET; fi

# jar names carry the project version (gradle.properties), like fetch-plugins.sh expects them
VERSION=$(sed -n 's/^version=//p' "$ROOT/gradle.properties" | tr -d '\r')
JARS="runtime/paper/build/libs/surf-player-paper-$VERSION-all.jar
runtime/velocity/build/libs/surf-player-velocity-$VERSION-all.jar
runtime/minestom/build/libs/surf-player-minestom-$VERSION-all.jar
runtime/placeholder-expansion/build/libs/surf-player-placeholder-expansion-$VERSION-all.jar
runtime/microservice/build/libs/surf-player-microservice-$VERSION-all.jar"

build_jars() {
    (cd "$ROOT" && ./gradlew :surf-player-paper:shadowJar :surf-player-minestom:shadowJar \
        :surf-player-velocity:shadowJar :surf-player-microservice:shadowJar \
        :surf-player-placeholder-expansion:shadowJar)
}

# @AutoService(X::class) names of the given source directories
services_of() {
    grep -rhoE '@AutoService\([A-Za-z]+::class' --include='*.kt' "$@" 2>/dev/null |
        sed 's/@AutoService(//; s/::class//' | sort -u
}

# Every platform jar must contain META-INF/services/<...>.<Name> for each @AutoService of
# core-client and of its own runtime module.
check_services() {
    ok=0
    for platform in paper minestom velocity; do
        jar="$ROOT/runtime/$platform/build/libs/surf-player-$platform-$VERSION-all.jar"
        [ -f "$jar" ] || { echo "  missing $jar"; ok=1; continue; }
        listing=$(unzip -l "$jar" | grep 'META-INF/services/' || true)
        for name in $(services_of "$ROOT/core/core-client/src/main" "$ROOT/runtime/$platform/src/main"); do
            if ! printf '%s\n' "$listing" | grep -q "META-INF/services/.*\.$name\$"; then
                echo "  $platform jar lacks META-INF/services/…$name"
                ok=1
            fi
        done
    done
    return $ok
}

# --- 1. build -------------------------------------------------------------------------------
if [ "$BUILD" = 1 ]; then
    stale=0
    for jar in $JARS; do
        if [ ! -f "$ROOT/$jar" ]; then stale=1; break; fi
        if [ -n "$(find "$ROOT/api" "$ROOT/core" "$ROOT/runtime" -path '*/build' -prune -o \
                -type f -newer "$ROOT/$jar" \( -name '*.kt' -o -name '*.kts' -o -name '*.yml' \) -print 2>/dev/null | head -n 1)" ]; then
            stale=1; break
        fi
    done
    if [ "$stale" = 1 ]; then
        echo "== Building surf-player jars (sources changed)"
        build_jars
    else
        echo "== surf-player jars are up to date"
    fi
    # Parallel/incremental Gradle builds have produced jars without some META-INF/services
    # entries (the plugin then fails with "Service ...Api not available"). Verify, and rebuild
    # once from clean if anything is missing.
    if ! check_services; then
        echo "== Service registrations missing: clean rebuild"
        (cd "$ROOT" && ./gradlew --stop >/dev/null 2>&1 || true)
        (cd "$ROOT" && ./gradlew :surf-player-api:clean :surf-player-core:surf-player-core-common:clean \
            :surf-player-core:surf-player-core-client:clean :surf-player-paper:clean :surf-player-minestom:clean \
            :surf-player-velocity:clean :surf-player-microservice:clean :surf-player-placeholder-expansion:clean)
        build_jars
        check_services || { echo "Service registrations still missing after a clean build" >&2; exit 1; }
    fi
fi

# --- 2. deploy ------------------------------------------------------------------------------
sums() {
    for f in paper/surf-player-paper.jar velocity/surf-player-velocity.jar minestom/surf-player-minestom.jar \
        microservice/surf-player-microservice.jar velocity/miniplaceholders/expansions/surf-player-placeholder-expansion.jar; do
        if [ -f "$NET/plugins/$f" ]; then cksum < "$NET/plugins/$f"; else echo missing; fi
    done
}
if [ "$DEPLOY" = 1 ]; then
    before=$(sums)
    echo "== Deploying jars (fetch-plugins.sh verifies them with unzip -t)"
    (cd "$NET" && ./fetch-plugins.sh)
    after=$(sums)
    compose up -d >/dev/null
    if [ "$before" != "$after" ]; then
        echo "== Jars changed: restarting paper, minestom, velocity, velocity-bots, player-microservice"
        compose restart player-microservice paper minestom velocity velocity-bots
    fi
fi

# --- 3.-6. wait, run, report -----------------------------------------------------------------
echo "== Building the test harness"
(cd "$HERE" && "$ROOT/gradlew" -q -p "$HERE" installDist)

# the POSIX start script also works in Git Bash on Windows
BIN="$HERE/build/install/player-tests/bin/player-tests"

export JAVA_OPTS="${JAVA_OPTS:-} -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8"
cd "$HERE"
exec "$BIN" --network-dir "$NET_NATIVE" --wait-ready "$@"
