# surf-player

The player layer of the SLNE network: chat, friends, clans, tab list, nametags, profiles,
settings, vanish, playtime, queue and more, shipped as one plugin per platform and one
microservice.

Other plugins integrate through a single, platform-independent API: `surf-player-api`.

## Installation

```kotlin
repositories {
    maven("https://reposilite.slne.dev/releases")
}

dependencies {
    compileOnly("dev.slne.surf.player:surf-player-api:<version>")
}
```

Declare a dependency on the plugin of your platform so it is loaded before yours:

```kotlin
// Paper
surfPaperPluginApi {
    serverDependencies {
        registerRequired("surf-player-paper")
    }
}

// Minestom
minestomPluginFile {
    pluginDependencies {
        register("surf-player-minestom")
    }
}

// Velocity
velocityPluginFile {
    pluginDependencies {
        register("surf-player-velocity")
    }
}
```

## Getting started

Every feature is reachable through `SurfPlayerApi`:

```kotlin
import dev.slne.surf.player.api.SurfPlayerApi

val clan = SurfPlayerApi.clans.clans.findClanByPlayer(player.uniqueId)
val friends = SurfPlayerApi.friends.findPlayer(player.uniqueId).friendships
val vanished = SurfPlayerApi.vanish.isVanished(player.uniqueId)
```

Each feature also has its own entry point (for example `SurfChatApi`, `ClanService`), which
resolves to the same instance.

### Conventions

- **Players are identified by `UUID`.** The API never takes platform types such as Bukkit's
  `Player`, so the same code runs on Paper, Minestom and Velocity.
- **`suspend` means network.** Functions that may talk to the microservice or Redis are
  `suspend`. Call them from a coroutine (e.g. mccoroutine on Paper), never from a blocking
  context on the main thread.
- **Results are sealed types.** Operations that can fail for expected reasons return a sealed
  result instead of throwing, so the compiler makes you handle every case.
- **Events are surf-api events.** Feature events extend `SurfEvent` and are listened to through
  the surf-api event bus on every platform.

## Features

### Chat

```kotlin
SurfPlayerApi.chat.registerChatProcessor(object : PreChatProcessor {
    override val order = 100

    override fun process(context: MessageContext): MessageContext {
        // modify or cancel the message before it is sent
        return context
    }
})

val ignored = SurfPlayerApi.chat.getIgnoreList(playerUuid)
val history = SurfPlayerApi.chat.lookupHistory(filter)
```

`PreChatProcessor`s run before a message is delivered and may modify or cancel it;
`PostChatProcessor`s run afterwards. `MessageRedirector`s can take over delivery entirely.

### Friends

```kotlin
val self = SurfPlayerApi.friends.findPlayer(playerUuid)
val target = SurfPlayerApi.friends.findPlayer(targetUuid)

when (val result = self.sendFriendRequest(target)) {
    is FriendRequestCreateResult.Success -> {}
    is FriendRequestCreateResult.AlreadyFriends -> {}
    is FriendRequestCreateResult.AlreadySentFriendRequest -> {}
    is FriendRequestCreateResult.AlreadyReceivedFriendRequest -> {}
    is FriendRequestCreateResult.OnCooldown -> println("retry in ${result.remaining}")
    is FriendRequestCreateResult.Blocked -> {}
}
```

Declining or withdrawing a request puts the sender on a cooldown for that target, and players
who ignore each other cannot exchange requests.

### Clans

```kotlin
val clan = Clan.byTag("SLNE") ?: return

clan.invite(invitee = targetUuid, invitedBy = playerUuid)

Clan.registerListener(ClanCreatedListener { created ->
    logger.info("Clan ${created.name} was created")
})
```

`SurfPlayerApi.clans` exposes the clan, member, invite and player services.

### Tab list

```kotlin
object OnlineStaffPlaceholder : TabPlaceholder {
    override val tagName = "online_staff"
    override val updates = UpdateCondition.Every(10.seconds)
    override fun value() = Component.text(onlineStaffCount())
}

SurfPlayerApi.tab.registerPlaceholder(OnlineStaffPlaceholder)
```

Registered placeholders can be used as `<online_staff>` in tab list templates. Use
`UpdateCondition.OnDemand` together with `SurfPlayerApi.tab.invalidate(...)` for values that only
change on known events. `TabEntryRenderEvent` lets you change a player's tab list name and order.

### Nametags

```kotlin
SurfPlayerApi.nametags.setNametagVisibility(playerUuid, NametagVisibility.NEVER)
SurfPlayerApi.nametags.resetNametagVisibility(playerUuid)
```

### Vanish

```kotlin
if (SurfPlayerApi.vanish.isVanished(playerUuid)) return

val visible = onlinePlayers.filterNot { SurfPlayerApi.vanish.isVanished(it) }
```

Vanish is provided by the Paper plugin.

### Content creators

```kotlin
SurfPlayerApi.contentCreators.registerStateChangeListener { playerUuid, platform, state ->
    // a creator went live or offline
}

val liveTag = SurfPlayerApi.contentCreators.renderLiveTag(playerUuid)
```

### Account connections

```kotlin
val twitch = SurfPlayerApi.connections.findConnection<TwitchConnection>(playerUuid)
val discord = SurfPlayerApi.connections.findConnection<DiscordConnection>(playerUuid)
```

## Platform support

| Feature          | Paper | Minestom | Velocity |
|------------------|:-----:|:--------:|:--------:|
| Chat             | ✓     | ✓        |          |
| Friends          | ✓     | ✓        |          |
| Clans            | ✓     | ✓        | ✓        |
| Tab list         | ✓     | ✓        |          |
| Nametags         | ✓     | ✓        |          |
| Vanish           | ✓     |          |          |
| Content creators | ✓     | ✓        |          |
| Connections      |       |          | ✓        |

The API is available on every platform; calling a feature that is not provided by the running
platform fails when the service is resolved.

## Project layout

| Directory | Artifact | Content |
|-----------|----------|---------|
| `api` | `surf-player-api` | Public API |
| `core/core-common` | `surf-player-core-common` | Code shared by plugins and the microservice |
| `core/core-client` | `surf-player-core-client` | Platform-independent plugin logic |
| `runtime/paper` | `surf-player-paper` | Paper plugin |
| `runtime/minestom` | `surf-player-minestom` | Minestom plugin |
| `runtime/velocity` | `surf-player-velocity` | Velocity plugin |
| `runtime/microservice` | `surf-player-microservice` | Microservice |
| `runtime/placeholder-expansion` | `surf-player-placeholder-expansion` | MiniPlaceholders expansion |

## Building

```shell
./gradlew build
```

Requires Java 25.
