#!/bin/sh
# Sends one console command to the Minestom lobby (mounted as /usr/local/bin/console).
#   docker compose exec minestom console lp user FPNS info
# The output appears in `docker compose logs minestom`, not here.
set -eu
[ "$#" -gt 0 ] || { echo "usage: console <command>" >&2; exit 2; }
printf '%s\n' "$*" >/tmp/console.in
echo "sent: $* (see: docker compose logs --tail 30 minestom)"
