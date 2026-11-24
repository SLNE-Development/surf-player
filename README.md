# surf-player

All social features of the SLNE network in one repository, one plugin per platform and one
microservice.

| Feature          | Formerly                | Packages (`dev.slne.surf.player.<layer>.<feature>`) |
|------------------|-------------------------|-----------------------------------------------------|
| `chat`           | surf-chat               | api, core.common, core.client, paper, minestom, microservice |
| `clan`           | surf-clan               | api, core.common, core.client, paper, minestom, velocity, microservice, placeholder.expansion |
| `friends`        | surf-friends            | api, core.common, core.client, paper, minestom, microservice |
| `shrieker`       | surf-shrieker           | api, core.common, paper, microservice |
| `tab`            | surf-tab                | api, core.client, paper, minestom |
| `nametag`        | surf-nametag            | api, core.client, paper, minestom |
| `contentcreator` | surf-content-creator    | api, core.client, paper, minestom |
| `connection`     | surf-player (account linking) | api, core.common, core.client, velocity, microservice |

The full git history of every former repository is preserved; `git log --follow` works across the
move.

## Modules

| Module | Content |
|--------|---------|
| `surf-player-api` | Public API of every feature. Entry point: `dev.slne.surf.player.api.SurfPlayerApi` |
| `surf-player-core:surf-player-core-common` | Code shared by clients and the microservice (rabbit packets, RPC interfaces, serializers) |
| `surf-player-core:surf-player-core-client` | Client side logic and the shared `SurfPlayerClient` connection |
| `surf-player-paper` | The Paper plugin `PlayerPaperPlugin` |
| `surf-player-minestom` | The Minestom plugin `PlayerMinestomPlugin` |
| `surf-player-velocity` | The Velocity plugin `PlayerVelocityPlugin` |
| `surf-player-microservice` | The microservice `PlayerMicroservice` |
| `surf-player-placeholder-expansion` | MiniPlaceholders expansion for clans |

```kotlin
SurfPlayerApi.chat.registerChatProcessor(...)
SurfPlayerApi.clans.clans.findClanByTag(...)
SurfPlayerApi.connections.findConnection<TwitchConnection>(uuid)
```

## Architecture

- **One plugin per platform.** Every feature is a feature object (`PaperFeature`,
  `MinestomPlayerFeature`, `VelocityPlayerFeature`) that the platform plugin loads in a fixed
  order and disables in reverse.
- **One connection.** All features share a single RabbitMQ client and a single Redis API named
  `surf-player` (`dev.slne.surf.player.core.client.SurfPlayerClient`). The plugin connects RabbitMQ
  before features load, Redis after all features registered their Redis listeners, and disconnects
  once after all features are disabled.
- **One microservice.** `PlayerMicroservice` owns one database connection and one RabbitMQ server
  connection (`surf-player`). Each feature contributes its tables and handlers through a
  `<Feature>MicroserviceModule`.

## Migrating from the separate plugins

- Remove `surf-chat-*`, `surf-clan-*`, `surf-friends-*`, `surf-shrieker-*`, `surf-tab-*`,
  `surf-nametag-*`, `surf-content-creator-*` and the old `surf-player-*` jars; install
  `surf-player-paper`, `surf-player-minestom` or `surf-player-velocity` instead.
- Every feature now has its own subfolder in the plugin data folder, e.g.
  `plugins/surf-chat-paper/config.yml` → `plugins/surf-player-paper/chat/config.yml`.
  Move existing configs by hand. The shared RabbitMQ client config lives in the plugin root folder.
- The RabbitMQ queue is `surf-player` for everything. The microservice and all clients must be
  deployed together; old and new versions cannot talk to each other.
- Deploy a single `surf-player-microservice` container instead of four. Configure one set of
  `SURF_DATABASE_*` / `SURF_RABBITMQ_*` variables (same database as before).
- Every feature keeps its tables in its own Postgres schema, exactly as before. The schema names
  default to `surf-chat`, `surf-clan`, `surf-friends` and `surf-shrieker` and can be overridden with
  `SURF_PLAYER_SCHEMA_CHAT`, `SURF_PLAYER_SCHEMA_CLAN`, `SURF_PLAYER_SCHEMA_FRIENDS` and
  `SURF_PLAYER_SCHEMA_SHRIEKER`. **Set these to the `SURF_DATABASE_SCHEMA` values of the old
  deployments** if they differ from the defaults. Connection accounts stay in `public.account`.

## Building

```shell
./gradlew build
```
