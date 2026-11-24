#!/usr/bin/env bash
# Downloads every server/proxy jar and plugin the local test network needs and copies the
# locally built surf-player jars next to them. Idempotent: files that already exist are kept,
# so re-running it only refreshes the surf-player jars (after `./gradlew build -x test`).
#
#   ./fetch-plugins.sh            download missing jars, copy local surf-player jars
#   ./fetch-plugins.sh --clean    delete plugins/ first and download everything again
#
# Everything is public; no credentials are used. Versions are pinned below so the network is
# reproducible. Bump them here (and run with --clean) to test against newer releases.
#
# Sources:
#   reposilite  https://reposilite.slne.dev/releases   (surf-api, minestom plugins, surf-punish)
#   GitHub      SLNE-Development/<repo> release assets (paper/velocity builds of surf-core,
#                                                       surf-rabbitmq, surf-redis; the lobby)
#   LuckPerms   https://download.luckperms.net
#   GitHub      MiniPlaceholders/MiniPlaceholders, CommandAPI/CommandAPI, retrooper/packetevents
#               release assets
set -euo pipefail

SURF_API_VERSION="${SURF_API_VERSION:-3.52.1}"
SURF_CORE_VERSION="${SURF_CORE_VERSION:-2.5.6}"
SURF_RABBITMQ_VERSION="${SURF_RABBITMQ_VERSION:-1.6.10}"
SURF_REDIS_VERSION="${SURF_REDIS_VERSION:-1.12.1}"
# 1.2.5 is the newest surf-punish on the public reposilite; newer builds are only in the
# private SLNE-Development/surf-punish repository.
SURF_PUNISH_VERSION="${SURF_PUNISH_VERSION:-1.2.5}"
LOBBY_VERSION="${LOBBY_VERSION:-2.1.0}"
LUCKPERMS_BUILD="${LUCKPERMS_BUILD:-1678}"
LUCKPERMS_VERSION="${LUCKPERMS_VERSION:-5.5.91}"
MINIPLACEHOLDERS_VERSION="${MINIPLACEHOLDERS_VERSION:-3.1.0}"
# Required by surf-api (Paper and Velocity); versions as in surf-api's libs.versions.toml.
COMMANDAPI_VERSION="${COMMANDAPI_VERSION:-12.1.0}"
PACKETEVENTS_VERSION="${PACKETEVENTS_VERSION:-2.14.0}"

REPOSILITE="https://reposilite.slne.dev/releases"
GITHUB="https://github.com"

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$HERE/../.." && pwd)"
PLAYER_VERSION="$(sed -n 's/^version=//p' "$REPO_ROOT/gradle.properties" | tr -d '\r')"
OUT="$HERE/plugins"

if [[ "${1:-}" == "--clean" ]]; then
    rm -rf "$OUT"
fi

mkdir -p "$OUT/paper" "$OUT/velocity/miniplaceholders/expansions" "$OUT/minestom" "$OUT/minestom-server" "$OUT/microservice"

# fetch <target file> <url>
fetch() {
    local target=$1 url=$2
    if [[ -s "$target" ]]; then
        echo "  ok      ${target#"$HERE/"}"
        return
    fi
    echo "  fetch   ${target#"$HERE/"}  <-  $url"
    curl -fsSL --retry 3 --retry-delay 2 --connect-timeout 15 -o "$target.part" "$url"
    if ! unzip -t -q "$target.part" >/dev/null 2>&1; then
        rm -f "$target.part"
        echo "  CORRUPT download from $url (unzip -t failed)" >&2
        exit 1
    fi
    mv -f "$target.part" "$target"
}

# maven <dir> <group> <artifact> <version>   (downloads the -all jar from reposilite)
maven() {
    local dir=$1 group=$2 artifact=$3 version=$4
    fetch "$OUT/$dir/$artifact-$version.jar" \
        "$REPOSILITE/${group//.//}/$artifact/$version/$artifact-$version-all.jar"
}

# gh_release <dir> <owner/repo> <tag> <asset> [target name]
gh_release() {
    local dir=$1 repo=$2 tag=$3 asset=$4 name=${5:-$4}
    fetch "$OUT/$dir/$name" "$GITHUB/$repo/releases/download/$tag/$asset"
}

echo "Paper plugins"
maven paper dev.slne.surf.api surf-api-paper-server "$SURF_API_VERSION"
gh_release paper SLNE-Development/surf-core "v$SURF_CORE_VERSION" "surf-core-paper-$SURF_CORE_VERSION-all.jar"
gh_release paper SLNE-Development/surf-rabbitmq "v$SURF_RABBITMQ_VERSION" "surf-rabbitmq-paper-$SURF_RABBITMQ_VERSION-all.jar"
gh_release paper SLNE-Development/surf-redis "v$SURF_REDIS_VERSION" "surf-redis-paper-$SURF_REDIS_VERSION-all.jar"
maven paper dev.slne.surf.punish surf-punish-paper "$SURF_PUNISH_VERSION"
fetch "$OUT/paper/LuckPerms-Bukkit-$LUCKPERMS_VERSION.jar" \
    "https://download.luckperms.net/$LUCKPERMS_BUILD/bukkit/loader/LuckPerms-Bukkit-$LUCKPERMS_VERSION.jar"
gh_release paper MiniPlaceholders/MiniPlaceholders "$MINIPLACEHOLDERS_VERSION" "MiniPlaceholders-Paper-$MINIPLACEHOLDERS_VERSION.jar"
gh_release paper CommandAPI/CommandAPI "$COMMANDAPI_VERSION" "CommandAPI-$COMMANDAPI_VERSION-Paper.jar"
gh_release paper retrooper/packetevents "v$PACKETEVENTS_VERSION" "packetevents-spigot-$PACKETEVENTS_VERSION.jar"

echo "Velocity plugins"
maven velocity dev.slne.surf.api surf-api-velocity-server "$SURF_API_VERSION"
gh_release velocity SLNE-Development/surf-core "v$SURF_CORE_VERSION" "surf-core-velocity-$SURF_CORE_VERSION-all.jar"
gh_release velocity SLNE-Development/surf-rabbitmq "v$SURF_RABBITMQ_VERSION" "surf-rabbitmq-velocity-$SURF_RABBITMQ_VERSION-all.jar"
gh_release velocity SLNE-Development/surf-redis "v$SURF_REDIS_VERSION" "surf-redis-velocity-$SURF_REDIS_VERSION-all.jar"
fetch "$OUT/velocity/LuckPerms-Velocity-$LUCKPERMS_VERSION.jar" \
    "https://download.luckperms.net/$LUCKPERMS_BUILD/velocity/LuckPerms-Velocity-$LUCKPERMS_VERSION.jar"
# Hosts the surf-player placeholder expansion (it requires the surf-player-velocity complement).
gh_release velocity MiniPlaceholders/MiniPlaceholders "$MINIPLACEHOLDERS_VERSION" "MiniPlaceholders-Velocity-$MINIPLACEHOLDERS_VERSION.jar"
gh_release velocity CommandAPI/CommandAPI "$COMMANDAPI_VERSION" "CommandAPI-$COMMANDAPI_VERSION-Velocity.jar"
gh_release velocity retrooper/packetevents "v$PACKETEVENTS_VERSION" "packetevents-velocity-$PACKETEVENTS_VERSION.jar"

echo "Minestom server and plugins"
gh_release minestom-server SLNE-Development/surf-minestom-lobby "v$LOBBY_VERSION" surf-minestom-lobby-server.jar lobby.jar
maven minestom dev.slne.surf.core surf-core-minestom "$SURF_CORE_VERSION"
maven minestom dev.slne.surf.rabbitmq surf-rabbitmq-minestom "$SURF_RABBITMQ_VERSION"
maven minestom dev.slne.surf.redis surf-redis-minestom "$SURF_REDIS_VERSION"

echo "Microservices"
gh_release microservice SLNE-Development/surf-core "v$SURF_CORE_VERSION" "surf-core-microservice-$SURF_CORE_VERSION-all.jar" surf-core-microservice.jar

echo "Local surf-player jars"
copy_local() {
    local source="$REPO_ROOT/$1" target="$OUT/$2"
    if [[ ! -f "$source" ]]; then
        echo "  missing $1 - run ./gradlew build -x test in the repository root" >&2
        exit 1
    fi
    # Concurrent Gradle builds into the same build/libs have produced truncated jars before.
    if ! unzip -t -q "$source" >/dev/null 2>&1; then
        echo "  CORRUPT $1 (unzip -t failed) - rebuild it: ./gradlew --stop, then" \
            "./gradlew :<project>:clean :<project>:shadowJar" >&2
        exit 1
    fi
    cp -f "$source" "$target"
    echo "  copied  ${target#"$HERE/"}"
}
copy_local runtime/paper/build/libs/surf-player-paper-$PLAYER_VERSION-all.jar paper/surf-player-paper.jar
copy_local runtime/velocity/build/libs/surf-player-velocity-$PLAYER_VERSION-all.jar velocity/surf-player-velocity.jar
copy_local runtime/minestom/build/libs/surf-player-minestom-$PLAYER_VERSION-all.jar minestom/surf-player-minestom.jar
copy_local runtime/placeholder-expansion/build/libs/surf-player-placeholder-expansion-$PLAYER_VERSION-all.jar velocity/miniplaceholders/expansions/surf-player-placeholder-expansion.jar
copy_local runtime/microservice/build/libs/surf-player-microservice-$PLAYER_VERSION-all.jar microservice/surf-player-microservice.jar

echo "Done."
