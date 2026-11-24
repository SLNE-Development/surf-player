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

date -u +%FT%TZ >"$MARKER"
echo "LuckPerms provisioned."
