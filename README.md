# surf-player

The player layer of the SLNE network: chat, friends, clans, tab list, nametags, profiles,
settings, vanish, playtime, queue, minimap integration, content creators and account linking,
shipped as one plugin per platform and one microservice.

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

## The API in one minute

There are two ways into the API, and every feature follows the same shape.

**A player and what they have** — `SurfCommunityPlayer`:

```kotlin
val player = SurfPlayerApi.player(uuid)

player.clan()                                  // the player's clan, or null
player.sendFriendRequest(other)                // a sealed result
player.isVanished
player.setting(SettingKeys.CHAT_PINGS)
player.totalPlaytime()
player.enqueue("survival")
```

`SurfCommunityPlayer` is a value class around the player's `UUID` that implements one capability
per feature (`ClanCapability`, `FriendsCapability`, `VanishCapability`, …). Creating it is free.

**A feature as a whole** — `Surf<Feature>Api`:

```kotlin
SurfClanApi.findClanByTag("SLNE")
SurfTabApi.registerPlaceholder(OnlineStaffPlaceholder)
SurfQueueApi.queue("survival").pause()
```

`SurfPlayerApi` lists all of them (`SurfPlayerApi.clans`, `SurfPlayerApi.tab`, …).

| Feature           | Entry point             | Player capability          | Paper | Minestom | Velocity |
|-------------------|-------------------------|----------------------------|:-----:|:--------:|:--------:|
| Chat              | `SurfChatApi`           | `ChatCapability`           | ✓     | ✓        |          |
| Clans             | `SurfClanApi`           | `ClanCapability`           | ✓     | ✓        | ✓        |
| Friends           | `SurfFriendsApi`        | `FriendsCapability`        | ✓     | ✓        |          |
| Tab list          | `SurfTabApi`            | `TabCapability`            | ✓     | ✓        |          |
| Nametags          | `SurfNametagApi`        | `NametagCapability`        | ✓     | ✓        |          |
| Vanish            | `SurfVanishApi`         | `VanishCapability`         | ✓     |          |          |
| Settings          | `SurfSettingsApi`       | `SettingsCapability`       | ✓     | ✓        | ✓        |
| Profile           | `SurfProfileApi`        | `ProfileCapability`        | ✓     | ✓        |          |
| Playtime          | `SurfPlaytimeApi`       | `PlaytimeCapability`       | ✓     | ✓        |          |
| Queue             | `SurfQueueApi`          | `QueueCapability`          | ✓     | ✓        | ✓        |
| Minimap           | `SurfMinimapApi`        | `MinimapCapability`        | ✓     | ✓        |          |
| Content creators  | `SurfContentCreatorApi` | `ContentCreatorCapability` | ✓     | ✓        |          |
| Account links     | `SurfConnectionApi`     | `ConnectionCapability`     | ✓     | ✓        | ✓        |

Calling a feature that the running platform does not provide fails when its service is resolved.

### Conventions

- **Players are identified by `UUID`.** The API never takes platform types such as Bukkit's
  `Player`.
- **`suspend` means network.** Members that may talk to the microservice or Redis are `suspend`;
  call them from a coroutine, never blocking on the main thread. Non-suspending members answer
  from state cached on the current server.
- **Expected failures are sealed results**, so the compiler makes you handle every case.
- **Events extend surf-api `SurfEvent`** and are received through the surf-api event bus.
- **Listeners are `fun interface`s**, so a lambda is enough.

The full rules for contributors are in [docs/api-conventions.md](docs/api-conventions.md).

## Features

### Chat

```kotlin
SurfChatApi.registerChatProcessor(object : PreChatProcessor {
    override val order = 100

    override fun process(context: MessageContext): MessageContext {
        // modify or cancel the message before it is delivered
        return context
    }
})

val ignored = SurfPlayerApi.player(uuid).ignoreList()
val history = SurfChatApi.lookupHistory(filter)
```

`PreChatProcessor`s run before a message is delivered and may change or cancel it,
`PostChatProcessor`s run afterwards, and a `MessageRedirector` can take over delivery entirely.

### Clans

```kotlin
val player = SurfPlayerApi.player(uuid)

when (val result = player.createClan(name = "Warriors", tag = "WAR")) {
    is ClanCreationResult.Success -> result.clan.invite(invitee = friendUuid, invitedBy = uuid)
    else -> {}
}

player.clanInvites()
player.setAcceptsClanInvites(false)

SurfClanApi.registerListener(ClanCreatedListener { clan -> logger.info("${clan.name} was created") })
```

### Friends

```kotlin
val player = SurfPlayerApi.player(uuid)

when (val result = player.sendFriendRequest(targetUuid)) {
    is FriendRequestCreateResult.Success -> {}
    is FriendRequestCreateResult.AlreadyFriends -> {}
    is FriendRequestCreateResult.AlreadySentFriendRequest -> {}
    is FriendRequestCreateResult.AlreadyReceivedFriendRequest -> {}
    is FriendRequestCreateResult.OnCooldown -> println("retry in ${result.remaining}")
    is FriendRequestCreateResult.Blocked -> {}
}

player.friendships.forEach { println(it.friend.uuid) }
player.onlineFriendUuids
```

Declining or withdrawing a request puts the sender on a cooldown for that target, and players
who ignore each other cannot exchange requests.

### Tab list

```kotlin
object OnlineStaffPlaceholder : TabPlaceholder {
    override val tagName = "online_staff"
    override val updates = UpdateCondition.Every(10.seconds)
    override fun value() = Component.text(onlineStaffCount())
}

SurfTabApi.registerPlaceholder(OnlineStaffPlaceholder)
SurfPlayerApi.player(uuid).broadcastTabEntryUpdate()
```

Registered placeholders are available as `<online_staff>` in tab list templates. Use
`UpdateCondition.OnDemand` with `SurfTabApi.invalidate(...)` for values that only change on known
events. `TabEntryRenderEvent` lets you change a player's tab list name and order.

### Nametags

```kotlin
val player = SurfPlayerApi.player(uuid)

player.setNametagVisibility(NametagVisibility.NEVER)
player.resetNametagVisibility()
```

### Vanish

```kotlin
if (SurfPlayerApi.player(uuid).isVanished) return

val vanished = SurfVanishApi.vanishedPlayers()
```

`PlayerVanishEvent` and `PlayerReappearEvent` are fired when a player's vanish state changes.

### Settings

```kotlin
val player = SurfPlayerApi.player(uuid)

if (player.setting(SettingKeys.CHAT_PINGS)) playPing()
player.saveSetting(SettingKeys.DIRECT_MESSAGES, false)
player.openSettingsMenu()

SurfSettingsApi.createSetting(SettingKey.ofBoolean(key("myplugin", "toggle"), defaultValue = true))
```

`setting` answers from the settings cached on this server; `loadSetting` loads them first, for
example for offline players.

### Playtime

```kotlin
val player = SurfPlayerApi.player(uuid)

player.totalPlaytime()             // seconds, including the running session
player.playtimeOnServer("Survival")
player.isAfk
player.loginStreak()
```

`AfkStateChangeEvent` is fired when a player becomes or stops being AFK.

### Profile

```kotlin
SurfPlayerApi.player(uuid).openProfileMenu()
```

### Queue

```kotlin
val player = SurfPlayerApi.player(uuid)

player.enqueue("survival")
player.queuePosition("survival")   // 0-based, null if not queued

SurfQueueApi.queue("survival").pause()
```

### Minimap

```kotlin
SurfMinimapApi.levelId(Key.key("minecraft:overworld"))
SurfPlayerApi.player(uuid).minimapLevelId()
```

### Content creators

```kotlin
SurfContentCreatorApi.registerStateChangeListener { playerUuid, platform, state ->
    // a creator went live or offline
}

val player = SurfPlayerApi.player(uuid)
if (player.isLive) sendMessage(player.liveTag())
```

### Account links

```kotlin
val player = SurfPlayerApi.player(uuid)

player.discordConnection()
player.twitchConnection()
```

## Project layout

| Directory | Artifact | Content |
|-----------|----------|---------|
| `api` | `surf-player-api` | Public API |
| `core/core-common` | `surf-player-core-common` | Code shared by the plugins and the microservice |
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
