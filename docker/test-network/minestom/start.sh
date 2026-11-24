#!/bin/sh
# Entrypoint of the minestom container. Mirrors the install script of the pterodactyl egg
# (surf-minestom-lobby/pterodactyl/install.sh): the lobby server jar plus the plugin jars in
# plugins/, then `java -jar lobby.jar`. The jars are not downloaded here but come from
# ../plugins (see fetch-plugins.sh), mounted read-only.
#
#   /opt/lobby/lobby.jar           surf-minestom-lobby-server release jar
#   /opt/plugins/*.jar             surf-player-minestom + surf-core/-rabbitmq/-redis-minestom
#   /opt/lobby/config.yml          config template, ${VAR} placeholders are filled from the env
#   /opt/lobby/plugin-config/      copied over /server/plugins/ on every start
#   /opt/lobby/worlds/*.polar      copied into upload/worlds/ once (the lobby imports and deletes them)
#   /server                        working directory (docker volume): libraries/, plugins/, logs/
set -eu

cd /server
mkdir -p plugins upload/worlds

cp -f /opt/lobby/lobby.jar lobby.jar

# Plugins: replace every jar on each start so plugins/ always matches /opt/plugins.
find plugins -maxdepth 1 -name '*.jar' -delete
cp -f /opt/plugins/*.jar plugins/

if [ -d /opt/lobby/plugin-config ]; then
    cp -rf /opt/lobby/plugin-config/. plugins/
fi

# Fill the placeholders of the config template. The values are test credentials only.
escape() { printf '%s' "$1" | sed 's/[&|\\]/\\&/g'; }
sed \
    -e "s|\${VELOCITY_FORWARDING_SECRET}|$(escape "$VELOCITY_FORWARDING_SECRET")|g" \
    -e "s|\${POSTGRES_PASSWORD}|$(escape "$POSTGRES_PASSWORD")|g" \
    /opt/lobby/config.yml >config.yml

# Seed the lobby world once. The lobby stores uploaded worlds in its database.
if [ ! -f .world-imported ] && ls /opt/lobby/worlds/*.polar >/dev/null 2>&1; then
    cp -f /opt/lobby/worlds/*.polar upload/worlds/
    touch .world-imported
fi

# Console input: the server reads stdin from a FIFO, so commands can be sent from the host with
# `docker compose exec minestom console <command>` (see console.sh); output goes to the logs.
# Opening the FIFO read-write keeps it from ever reporting EOF.
CONSOLE=/tmp/console.in
rm -f "$CONSOLE"
mkfifo "$CONSOLE"
exec 3<>"$CONSOLE"

exec java -XX:MaxRAMPercentage=75.0 $JAVA_OPTS \
    -Dterminal.jline=false -Dterminal.ansi=true \
    -jar lobby.jar <&3
