#!/bin/bash
# One-shot provisioning of the shared LuckPerms storage (service luckperms-init in compose.yml).
# Runs the lp commands through the RCON console of the Paper server; Velocity and the Minestom
# lobby share the same PostgreSQL storage and receive the change through Redis messaging.
#
# Runs once per volume (marker /state/provisioned); `docker compose down -v` resets it. To run it
# again without a reset: docker compose run --rm -e FORCE=1 luckperms-init
#
# Players are resolved to their Mojang UUIDs first: the proxy runs in online mode, so these are
# the UUIDs the players will have on every backend.
set -euo pipefail

ADMINS=(NotAmmo)
DEFAULT_ONLY=(FPNS)
MARKER=/state/provisioned

if [[ -f "$MARKER" && "${FORCE:-0}" != "1" ]]; then
    echo "LuckPerms already provisioned ($(cat "$MARKER")); set FORCE=1 to run again."
    exit 0
fi

rcon() {
    echo "> $*"
    rcon-cli --host paper --port 25575 --password "$RCON_PASSWORD" "$@"
}

echo "Waiting for the Paper RCON console..."
for _ in $(seq 1 120); do
    if rcon-cli --host paper --port 25575 --password "$RCON_PASSWORD" "lp info" >/dev/null 2>&1; then
        break
    fi
    sleep 5
done

# Prints the dashed Mojang UUID of player $1.
mojang_uuid() {
    local id
    id=$(curl -fsS "https://api.mojang.com/users/profiles/minecraft/$1" |
        sed -n 's/.*"id" *: *"\([0-9a-fA-F]\{32\}\)".*/\1/p')
    [[ -n "$id" ]] || { echo "Could not resolve the UUID of $1" >&2; return 1; }
    echo "${id:0:8}-${id:8:4}-${id:12:4}-${id:16:4}-${id:20:12}"
}

# Player commands need explicit permissions (Bukkit's default for unknown nodes is op-only), so the
# default group gets every player command of surf-player; see docs/command-migration.md.
rcon "lp group default permission set surf.player.command.* true"

rcon "lp creategroup admin"
rcon "lp group admin setweight 100"
rcon "lp group admin permission set * true"

for name in "${ADMINS[@]}"; do
    uuid=$(mojang_uuid "$name")
    echo "$name = $uuid"
    rcon "lp user $uuid parent add admin"
done

for name in "${DEFAULT_ONLY[@]}"; do
    uuid=$(mojang_uuid "$name")
    echo "$name = $uuid"
    rcon "lp user $uuid parent set default"
    rcon "lp user $uuid permission clear"
done

# Test bots (docker/test-network/bots) join through the offline-mode proxy velocity-bots, so they
# have offline UUIDs: UUID.nameUUIDFromBytes("OfflinePlayer:<name>") (MD5, version 3).
offline_uuid() {
    local h
    h=$(printf 'OfflinePlayer:%s' "$1" | md5sum | cut -c1-32)
    # set version 3 and the IETF variant
    local v=$(( (0x${h:12:2} & 0x0f) | 0x30 ))
    local r=$(( (0x${h:16:2} & 0x3f) | 0x80 ))
    h="${h:0:12}$(printf '%02x' "$v")${h:14:2}$(printf '%02x' "$r")${h:18:14}"
    echo "${h:0:8}-${h:8:4}-${h:12:4}-${h:16:4}-${h:20:12}"
}

# Group 1 plus the parallel bot groups 2..6 of test/player-tests (DevA<n>, DevP<n>a, DevP<n>b).
BOT_ADMINS=(DevAdmin DevA2 DevA3 DevA4 DevA5 DevA6)
BOT_DEFAULT_ONLY=(DevPlayer1 DevPlayer2 DevP2a DevP2b DevP3a DevP3b DevP4a DevP4b DevP5a DevP5b DevP6a DevP6b)

for name in "${BOT_ADMINS[@]}"; do
    uuid=$(offline_uuid "$name")
    echo "$name (offline) = $uuid"
    rcon "lp user $uuid parent add admin"
done

for name in "${BOT_DEFAULT_ONLY[@]}"; do
    uuid=$(offline_uuid "$name")
    echo "$name (offline) = $uuid"
    rcon "lp user $uuid parent set default"
    rcon "lp user $uuid permission clear"
done

date -u +%FT%TZ >"$MARKER"
echo "LuckPerms provisioned."
