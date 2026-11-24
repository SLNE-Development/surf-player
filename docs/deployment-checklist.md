# Deployment checklist: switching the network to surf-player

The switch replaces 14 separate plugins and their microservices at once. The whole network goes
offline for it; old and new versions cannot talk to each other.

## Before the switch

- [ ] Note the `SURF_DATABASE_SCHEMA` of every old microservice (chat, clan, friends, shrieker,
      settings, playtime). The surf-player microservice needs the same values, see below.
- [ ] Back up the database.

## Plugins

- [ ] Remove the old jars on every server: `surf-chat-*`, `surf-clan-*`, `surf-friends-*`,
      `surf-shrieker-*`, `surf-tab-*`, `surf-nametag-*`, `surf-content-creator-*`,
      `surf-settings-*`, `surf-profile-*`, `surf-vanish-*`, `surf-playtime-*`, `surf-queue-*`,
      `surf-minimap-companion-*` and the old `surf-social-*`.
- [ ] Install `surf-player-paper` (Paper), `surf-player-minestom` (Minestom),
      `surf-player-velocity` (Velocity).
- [ ] Install `surf-player-placeholder-expansion` on **Velocity** (MiniPlaceholders expansions
      folder); it requires `surf-player-velocity`.
- [ ] Move existing config files from the old plugin folders into the feature subfolders of the new
      plugin folder, e.g. `plugins/surf-chat-paper/config.yml` →
      `plugins/surf-player-paper/chat/config.yml`. Subfolders: `chat`, `clan`, `friends`, `tab`,
      `nametag`, `contentcreator`, `vanish`, `playtime`, `queue`, `minimap`, `settings`,
      `connection`. The shared RabbitMQ client config lives in the plugin root folder.

## Microservice

- [ ] Stop and remove the old microservices (chat, clan, friends, shrieker, settings, playtime,
      social).
- [ ] Deploy one `surf-player-microservice` with one set of `SURF_DATABASE_*` and
      `SURF_RABBITMQ_*` variables (same database as before).
- [ ] Set `SURF_PLAYER_SCHEMA_CHAT`, `_CLAN`, `_FRIENDS`, `_SHRIEKER`, `_SETTINGS`, `_PLAYTIME` to the
      old `SURF_DATABASE_SCHEMA` values if they differ from the defaults (`surf-chat`, `surf-clan`,
      …). Otherwise the features start on empty schemas.
- [ ] Optional: friend request cooldowns via `SURF_PLAYER_FRIEND_REQUEST_DECLINE_COOLDOWN` (24h),
      `_WITHDRAW_COOLDOWN` (30m), `_COOLDOWN_ESCALATION_WINDOW` (30d), `_MAX_COOLDOWN` (7d).
- [ ] Configure the Discord bot token in `config/social.properties` of the microservice.

## Redis and RabbitMQ

- [ ] Flush Redis (`FLUSHALL`). Every key was renamed to `surf-player:<feature>:<name>`; old keys
      are never read again.
- [ ] Delete the old RabbitMQ queues of the separate microservices (`surf-chat`, `surf-clan`,
      `surf-friends`, `surf-shrieker`, `surf-settings`, `surf-playtime`, old `surf-social`). The
      only queue is now `surf-player`.

## Permissions (LuckPerms)

- [ ] Update every group with the mapping in [command-migration.md](command-migration.md).
- [ ] Grant `surf.player.command.*` to the default group (player commands are not op-free
      otherwise).
- [ ] Team ranks: `surf.player.team.*` plus the bypasses they had before
      (`surf.player.chat.ignore.exempt`, `surf.player.vanish.bypass`,
      `surf.player.friends.cooldown.bypass`, …).
- [ ] Admin ranks: `surf.player.admin.*`.

## Database clean-up (after the switch works)

- [ ] Drop the column that clan invites no longer use (the `clan:invites` setting decides now):
      `ALTER TABLE "surf-clan".clan_players DROP COLUMN accepts_clan_invites;`
      (use the real clan schema if it is not `surf-clan`).
- [ ] Delete the removed scoreboard setting:
      `DELETE FROM "surf-settings".settings_settings WHERE name = 'scoreboard:show-scoreboard';`

## Other plugins

- [ ] Update the plugins that used the old APIs to `dev.slne.surf.player:surf-player-api`:
      civilization, surf-minimap-companion's users, surf-lobby, surf-stats, surf-sharding,
      surf-event, surf-minestom-lobby (download list), surf-deathmessages and surf-lobby read
      settings through the new settings API.
