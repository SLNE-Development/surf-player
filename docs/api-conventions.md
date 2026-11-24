# API conventions

Every feature of surf-player exposes its public API in exactly the same shape. When you add or
change a feature, follow these rules; reviewers reject anything that deviates.

## 1. One entry point per feature: `Surf<Feature>Api`

Network-wide operations (everything that is not about one specific player) live in a single
interface in `dev.slne.surf.player.api.<feature>`:

```kotlin
private val instance = requiredService<SurfClanApi>()

interface SurfClanApi {
    suspend fun findClanByTag(tag: String): Clan?
    fun registerListener(listener: ClanListener)

    companion object : SurfClanApi by instance {
        val INSTANCE: SurfClanApi get() = instance
    }
}
```

- The file contains the private `instance` lookup, the interface and the companion — nothing else.
- Callers write `SurfClanApi.findClanByTag(...)`; `SurfClanApi.INSTANCE` is only for passing the
  API around.
- There are no additional services (`ClanMemberService`, `FriendsPlayerManager`, ...), no
  top-level `val`s holding instances and no static helpers on domain types (`Clan.byTag(...)`)
  that duplicate the entry point.

| Feature        | Entry point             | Player capability   |
|----------------|-------------------------|---------------------|
| chat           | `SurfChatApi`           | `HasChat`           |
| clan           | `SurfClanApi`           | `HasClan`           |
| connection     | `SurfConnectionApi`     | `HasConnections`    |
| contentcreator | `SurfContentCreatorApi` | `HasContentCreator` |
| friends        | `SurfFriendsApi`        | `HasFriends`        |
| minimap        | `SurfMinimapApi`        | `HasMinimap`        |
| nametag        | `SurfNametagApi`        | `HasNametag`        |
| playtime       | `SurfPlaytimeApi`       | `HasPlaytime`       |
| profile        | `SurfProfileApi`        | `HasProfile`        |
| queue          | `SurfQueueApi`          | `HasQueue`          |
| settings       | `SurfSettingsApi`       | `HasSettings`       |
| tab            | `SurfTabApi`            | `HasTab`            |
| vanish         | `SurfVanishApi`         | `HasVanish`         |

## 2. One capability per feature for a player: `Has<Feature>`

Everything a single player can do or has in a feature lives in `Has<Feature>` in the same package:

```kotlin
interface HasClan {
    val uuid: UUID

    suspend fun clan(): Clan? = SurfClanApi.findClanByPlayer(uuid)
    suspend fun clanInvites(): List<ClanInvite> = SurfClanApi.findInvitesOf(uuid)
}
```

- The only abstract member is `val uuid: UUID`. Every other member has a default implementation
  that delegates to the feature's entry point, so the capability needs no implementation of its
  own.
- `SurfCommunityPlayer` implements every capability, so member names must be unique across all
  `Has<Feature>` interfaces. Name members after the feature when a name could clash
  (`clanInvites()`, not `invites()`; `playtimeSession()`, not `session()`).
- Player-specific functions on the entry point take the player's `UUID` as first parameter;
  `Has<Feature>` is the convenient way to call them.
- Feature-specific "player" types that only exist to hold per-player functions
  (`FriendsPlayer`, `ClanPlayer`, ...) are removed from the API; their members move to
  `Has<Feature>`. Real domain objects (`Clan`, `FriendRequest`, `SurfQueue`) stay.

## 3. One player type: `SurfCommunityPlayer`

```kotlin
val player = SurfPlayerApi.player(uuid)   // or SurfCommunityPlayer(uuid)
player.clan()
player.sendFriendRequest(other.uuid)
player.isVanished
```

`SurfCommunityPlayer` is a value class around the player's `UUID` that implements every
`Has<Feature>`. It is cheap to create and never cached.

## 4. Common rules

- Players are identified by `UUID`; the API never exposes platform types (Bukkit `Player`,
  Minestom `Player`, Velocity `Player`). Platform conveniences live in the runtime modules.
- `suspend` means the call may go over the network (microservice, Redis). Non-suspending members
  answer from local state.
- Expected failures are sealed result types, never exceptions or `null` with hidden meaning.
- Events extend surf-api `SurfEvent` and live in `api.<feature>.event`.
- Listeners are `fun interface`s.
- No nested classes except the variants of a sealed type and companions.

## 5. Implementation side (core-client and runtime modules)

- `Surf<Feature>ApiImpl` in `core.client.<feature>.api`, registered with
  `@AutoService(Surf<Feature>Api::class)` — either in core-client or, if it needs platform types,
  in the runtime module.
- Platform hooks a feature needs from Paper/Minestom/Velocity form one interface
  `<Feature>Platform` in `core.client.<feature>.platform`, implemented once per runtime module
  with `@AutoService`. That is the only other place AutoService is used.
- Lifecycle (load, enable, disable) is driven by the runtime feature objects
  (`PaperPlayerFeature`, `MinestomPlayerFeature`, `VelocityPlayerFeature`). There are no
  `<Feature>Instance` / `Client<Feature>Instance` / `<Feature>Loader` singletons looked up through
  `requiredService`.
- Implementations are `object`s or classes referenced directly; `requiredService` is used only
  for the API entry points and the `<Feature>Platform` interfaces.
