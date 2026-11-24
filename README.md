# surf-social

All social features of the SLNE network in one repository, one plugin per platform and one
microservice.

| Feature          | Formerly                | Packages (`dev.slne.surf.social.<layer>.<feature>`) |
|------------------|-------------------------|-----------------------------------------------------|
| `chat`           | surf-chat               | api, core.common, core.client, paper, minestom, microservice |
| `clan`           | surf-clan               | api, core.common, core.client, paper, minestom, velocity, microservice, placeholder.expansion |
| `friends`        | surf-friends            | api, core.common, core.client, paper, minestom, microservice |
| `shrieker`       | surf-shrieker           | api, core.common, paper, microservice |
| `tab`            | surf-tab                | api, core.client, paper, minestom |
| `nametag`        | surf-nametag            | api, core.client, paper, minestom |
| `contentcreator` | surf-content-creator    | api, core.client, paper, minestom |
| `connection`     | surf-social (account linking) | api, core.common, core.client, velocity, microservice |

The full git history of every former repository is preserved; `git log --follow` works across the
move.

## Modules

| Module | Content |
|--------|---------|
| `surf-social-api` | Public API of every feature. Entry point: `dev.slne.surf.social.api.SurfSocialApi` |
| `surf-social-core:surf-social-core-common` | Code shared by clients and the microservice (rabbit packets, RPC interfaces, serializers) |
| `surf-social-core:surf-social-core-client` | Client side logic and the shared `SocialClient` connection |
| `surf-social-paper` | The Paper plugin `SocialPaperPlugin` |
| `surf-social-minestom` | The Minestom plugin `SocialMinestomPlugin` |
| `surf-social-velocity` | The Velocity plugin `SocialVelocityPlugin` |
| `surf-social-microservice` | The microservice `SocialMicroservice` |
| `surf-social-placeholder-expansion` | MiniPlaceholders expansion for clans |

```kotlin
SurfSocialApi.chat.registerChatProcessor(...)
SurfSocialApi.clans.clans.findClanByTag(...)
SurfSocialApi.connections.findConnection<TwitchConnection>(uuid)
```

## Architecture

- **One plugin per platform.** Every feature is a feature object (`PaperFeature`,
  `MinestomSocialFeature`, `VelocitySocialFeature`) that the platform plugin loads in a fixed
  order and disables in reverse.
- **One connection.** All features share a single RabbitMQ client and a single Redis API named
  `surf-social` (`dev.slne.surf.social.core.client.SocialClient`). The plugin connects RabbitMQ
  before features load, Redis after all features registered their Redis listeners, and disconnects
  once after all features are disabled.
- **One microservice.** `SocialMicroservice` owns one database connection and one RabbitMQ server
  connection (`surf-social`). Each feature contributes its tables and handlers through a
  `<Feature>MicroserviceModule`.

## Migrating from the separate plugins

- Remove `surf-chat-*`, `surf-clan-*`, `surf-friends-*`, `surf-shrieker-*`, `surf-tab-*`,
  `surf-nametag-*`, `surf-content-creator-*` and the old `surf-social-*` jars; install
  `surf-social-paper`, `surf-social-minestom` or `surf-social-velocity` instead.
- Every feature now has its own subfolder in the plugin data folder, e.g.
  `plugins/surf-chat-paper/config.yml` → `plugins/surf-social-paper/chat/config.yml`.
  Move existing configs by hand. The shared RabbitMQ client config lives in the plugin root folder.
- The RabbitMQ queue is `surf-social` for everything. The microservice and all clients must be
  deployed together; old and new versions cannot talk to each other.
- Deploy a single `surf-social-microservice` container instead of four. Configure one set of
  `SURF_DATABASE_*` / `SURF_RABBITMQ_*` variables (same database as before).
- Every feature keeps its tables in its own Postgres schema, exactly as before. The schema names
  default to `surf-chat`, `surf-clan`, `surf-friends` and `surf-shrieker` and can be overridden with
  `SURF_SOCIAL_SCHEMA_CHAT`, `SURF_SOCIAL_SCHEMA_CLAN`, `SURF_SOCIAL_SCHEMA_FRIENDS` and
  `SURF_SOCIAL_SCHEMA_SHRIEKER`. **Set these to the `SURF_DATABASE_SCHEMA` values of the old
  deployments** if they differ from the defaults. Connection accounts stay in `public.account`.

## Building

```shell
./gradlew build
```
