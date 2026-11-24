# Wire protocol

surf-player talks over two connections, both named `surf-player`:

- **RabbitMQ RPC** between the client plugins (Paper, Minestom, Velocity) and the single
  microservice. Every service is an `@RpcService` interface in
  `core.common.<feature>.rpc`, implemented by an `object` in `microservice.<feature>.rpc` and
  registered by the feature's `<Feature>MicroserviceModule`; clients use the lazies in
  `core.client.<feature>.rpc/RpcServices.kt`. Requests time out after the configured
  `requestTimeoutSeconds` (default 60 s); chat history lookups additionally give up after 10 s.
  Commands wait at most 10 s (`INTERACTIVE_RPC_TIMEOUT`, core-client `command/ServiceCalls.kt`)
  and then fail with "Der Dienst ist gerade nicht erreichbar. Bitte versuche es später erneut.";
  the 60 s request timeout stays for background work such as playtime saves.
- **Redis** (surf-redis) between the client plugins: events and requests for live cross-server
  messaging, synchronized structures and caches. The microservice does not use Redis.

The whole protocol is internal and may change in any release; the network is restarted as a
whole and Redis may be flushed. Only two formats are consumed by external systems and must stay
stable:

| Format                                                       | Consumer     |
|--------------------------------------------------------------|--------------|
| Redis stream `surf-discord:discord-roles:whitelisted-roles`  | surf-discord |
| Postgres table `public.account`                              | website      |

## Conventions

**RPC methods** use one verb per kind of operation:

| Verb       | Meaning                                                                 |
|------------|-------------------------------------------------------------------------|
| `find*`    | read; returns `null` / an empty collection if nothing matches           |
| `create*`  | insert                                                                  |
| `update*`  | change an existing row                                                  |
| `save*`    | insert or replace                                                       |
| `delete*`  | remove                                                                  |

Domain verbs are kept where they describe a state transition (`acceptInvite`,
`answerFriendRequest`, `revokeFriendRequest`, `calculateStreak`). Expected failures are sealed
results; a `Boolean` is only returned where it has one documented meaning ("did it exist").
A client never calls an RPC method in a loop; batch methods exist instead
(`createSettings`, `updateLoginStreak`).

**Redis keys** of every raw Redisson structure, cache and sync structure are
`surf-player:<feature>:<name>` (`playerRedisKey(feature, name)` in core-common), in kebab-case
and without version suffixes. Events and requests are `@Serializable` classes in
`core.client.<feature>.redis`; their names are their channel identity.

surf-redis stores its sync structures (sync list, set, map, value) under keys of its own, which a
caller cannot change: the id is given as `surf-player:<feature>:<name>` like every other key, and
surf-redis turns it into `surf-redis:sync:<list|set|map|value>:<id with ':' replaced by '_'>:<part>`
(parts `stream`, `version` and, for maps, `snapshot`). The sync map
`surf-player:vanish:vanished-players`, for example, lives in
`surf-redis:sync:map:surf-player_vanish_vanished-players:stream`/`:version`. The tables below name
the ids.

## chat

RPC:

- `ChatFunctionalityRpcService`
  - `findFunctionalities(serverName): Functionalities` - creates the defaults for unknown servers
  - `findAllFunctionalities(): Map<String, Functionalities>`
  - `saveFunctionalities(serverName, functionalities)` - upsert keyed by the server name
- `ChatHistoryRpcService`
  - `createHistoryEntry(entry)`
  - `findHistoryEntries(filter): List<HistoryEntry>` - newest first
  - `updateHistoryEntryDeleted(messageUuid, deletedBy, deletionReason, deletedAt)`
- `ChatIgnoreRpcService`
  - `createIgnore(playerUuid, targetUuid): Boolean` - `false` if already ignored; also deletes
    the pending friend requests between both players
  - `deleteIgnore(playerUuid, targetUuid): Boolean` - `false` if not ignored
  - `findIgnoreList(playerUuid): List<IgnoreListEntry>`
  - `isIgnoring(playerUuid, targetUuid): Boolean` - asked by the sender's server before a direct
    message to a receiver on another server (whose ignore list it has not cached); a receiver on
    the same server is checked in the local cache
- `ChatModerationRpcService`
  - `createModerationLog(messageData, classification)`

Redis:

| Kind     | Name                                                                               | Purpose                                                                                       |
|----------|------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| map      | `surf-player:chat:reply-targets`                                                    | last direct message partner per player for `/reply`, 15 min TTL per entry                      |
| sync set | `surf-player:chat:spies`                                                            | `SpyEntry(spy, observed)`: `/spy` network-wide; a spy's entries are removed when the spy leaves the network (`FriendConnectionRedisEvent` with `online = false`) or runs `/spy clear` |
| event    | `PrivateMessageSpyRedisEvent(messageData, spies)`                                   | a delivered direct message (not blocked by a pre processor) for the spies of its sender or receiver; every server shows it to the spies it hosts |
| event    | `TeamBroadcastRedisEvent(message, permission)`                                      | team chat messages and message deletion notices, shown on every server to players with permission |
| event    | `AiModerationNoticeRedisEvent(messageData, action)`                                 | AI moderation flag; every server renders the notice for its team itself, so the delete button's click callback is registered where it is clicked |
| event    | `DeleteRemoteMessageRedisEvent(messageSignature)`                                   | deletes a signed chat message on every server; every chat deletion goes through it            |
| request  | `SendDirectMessageRedisRequest` -> `SendDirectMessageHandledRedisResponse`         | delivers a direct message on the server hosting the receiver                                  |
| request  | `SendSignedMessageRedisRequest(sender, senderName, target, message, requiredSetting?)` -> `SendSignedMessageHandledRedisResponse` | shows a signed message on the server hosting the target; with `requiredSetting` (a boolean setting name, e.g. `clan:chat-messages` for clan chat) that server answers but does not show it if the target turned the setting off |

The chat events (and the network quit of `FriendConnectionRedisEvent`, to end spying) are handled
once for all platforms by `ChatRedisListener` (core-client); the two requests need platform types
and are handled by the Paper and Minestom chat features.

## clan

RPC:

- `ClanRpcService` (`update*`/`delete*` return `false` if the clan does not exist)
  - `createClan(name, tag, owner, tagForegroundColor, tagBackgroundColor, tagShadowColor, description, discordInvite): ClanCreationResult`
  - `deleteClan(clanId): Boolean`
  - `findClansByMemberCount(): List<ClanImpl>` - without members, biggest first
  - `findClanById(clanId)`, `findClanByMember(memberUuid)`, `findClanByTag(tag)`,
    `findClanByUuid(clanUuid)`: `ClanImpl?`
  - `findClanTagsByPrefix(prefix, limit): List<String>` - uppercase, alphabetical; a blank prefix
    returns the first `limit` tags of all clans (suggestions before anything was typed)
  - `updateClanDescription(clanId, description): Boolean`
  - `updateClanDiscordInvite(clanId, discordInvite): Boolean`
  - `updateClanTagColor(clanId, foreground, background, shadow): Boolean`
- `ClanInviteRpcService`
  - `acceptInvite(inviteId, invitee, invitedBy): ClanInviteAcceptance` -
    `Accepted(clan)` / `AlreadyInClan` / `InviteNotFound`; the joined clan is returned directly
  - `createInvite(clanId, invitee, invitedBy): ClanInviteResult` - the client checks the
    invitee's setting `clan:invites` (`InvitationsDisabled`) before calling
  - `deleteInvite(clanId, invitee): Boolean`
  - `findPendingInvitesOfClan(clanId): Set<ClanInviteImpl>`
  - `findPendingInvitesOfPlayer(invitee): List<ClanInviteImpl>`
  - `findPendingInvite(invitee, clanName): ClanInviteImpl?`
- `ClanMemberRpcService`
  - `createMember(clanId, player, role, invitedBy): ClanMemberAddResult`
  - `deleteMember(clanId, player): Boolean`
  - `updateMemberRole(memberId, role): Boolean`

Redis:

| Kind            | Name                        | Purpose                                                                    |
|-----------------|-----------------------------|----------------------------------------------------------------------------|
| set cache       | `surf-player:clan:clans`    | `ClanImpl` by id, indexed by tag, uuid and member uuids; 15 min            |

Writes invalidate the affected cache entries; there are no clan events. Memberships
(`findClanMember`) are read from the clan in this cache, so every server sees joins, leaves,
kicks, role changes and deletions at once. The table `clan.clan_players` (column
`accepts_clan_invites`) is no longer used; it is kept only so existing databases need no migration.

## connection

RPC: `ConnectionRpcService` (reads and writes `public.account`)

- `findDiscordConnection(minecraftUuid): DiscordConnection?`
- `findTwitchConnection(minecraftUuid): TwitchConnection?`
- `deleteMinecraftLink(minecraftUuid): UnlinkResult` - `NotLinked` / `Unlinked(discordId)`

`public.account` (Auth.js): every row is `("userId", provider, "providerAccountId")`; the rows of
one website user share `"userId"` (any text, not necessarily a uuid). The `minecraft` row holds the
player's uuid (matched with or without dashes, in either case), the `discord` and `twitch` rows hold
the **numeric** account id of that platform; the display name is fetched from Discord / ivr.fi by
that id (cached 30 min, the id itself when the lookup fails). A non-numeric id is ignored with a
warning in the microservice log. Nothing is cached between the database and the clients: every
lookup reads the table.

The content creator feature reads a player's Twitch link when they join a server and keeps it for
that session; a link made while the player is online is picked up on their next server join, or at
once by `/surfplayer contentcreator debug live|offline`, which reads it again for a player that has
none cached.

Redis:

| Kind   | Name                                          | Purpose                                                                                     |
|--------|-----------------------------------------------|---------------------------------------------------------------------------------------------|
| stream | `surf-discord:discord-roles:whitelisted-roles` | **external**, read by surf-discord: field `request`, JSON `{"action":"ADD"/"REMOVE","userId":<long>}`, max length 10 000 |

## friends

RPC: `FriendsRpcService`. Clients check the preconditions against the synchronized lists; the
microservice checks ignores, the target's setting `friend:requests` (read in-process from the
settings tables) and cooldowns.

- `findAllFriendRequests(): List<FriendRequest>`
- `findAllFriendships(): List<Friendship>` - stored once per direction
- `createFriendRequest(senderUuid, targetUuid, privileges): FriendRequestCreateResult` - checked in
  the order `Blocked` (ignore), `RequestsDisabled` (`friend:requests` off), `OnCooldown`.
  `FriendRequestPrivileges(ignoreExempt, cooldownBypass)` is set by the client from the sender's
  permissions (`surf.player.chat.ignore.exempt`: the target ignoring the sender does not block;
  `surf.player.friends.cooldown.bypass`: no cooldown); the microservice trusts it
- `deleteFriendRequestCooldowns(senderUuid, targetUuid?): Int` - the sender's cooldowns, only the
  one towards `targetUuid` if given (`/surfplayer friends cooldown clear`)
- `revokeFriendRequest(senderUuid, targetUuid): FriendRequestRemoveResult`
- `answerFriendRequest(senderUuid, targetUuid, state): FriendRequestAnswer` - `Accepted(friendships)`
  with both stored directions (clients add exactly these to the sync list, timestamps included) /
  `Declined`
- `deleteFriendship(senderUuid, targetUuid): FriendshipRemoveResult` - both directions

Redis:

| Kind      | Name                                          | Purpose                                                                                       |
|-----------|-----------------------------------------------|-----------------------------------------------------------------------------------------------|
| sync list | `surf-player:friends:requests`                | every pending `FriendRequest`                                                                 |
| sync list | `surf-player:friends:friendships`             | every `Friendship`                                                                            |
| lock      | `surf-player:friends:reload-lock`             | serializes the reload of both lists from the microservice that every server does on startup  |
| event     | `FriendsRedisEvent(action, executorUuid, targetUuid)` | `REQUEST_SENT`, `REQUEST_REVOKED`, `REQUEST_ACCEPTED`, `REQUEST_DECLINED`, `FRIENDSHIP_REMOVED`; the servers hosting either player notify them |
| event     | `FriendConnectionRedisEvent(playerUuid, online, serverName?)` | published by Velocity on a network join (first server connection, `serverName` = that server) and a network quit; server switches publish nothing. Every server notifies the friends it hosts; the server named `serverName` also greets the player |

Whether the target of `REQUEST_SENT` is notified is decided by the server hosting the target,
the only one that has the target's settings cached. Likewise every server applies the friend
notification and sound settings (`friend:notifications`, `friend:sounds`) of the friends it hosts.

## playtime

RPC: `PlaytimeRpcService`. Clients track running sessions and persist them through `saveSession`.

- `saveSession(session): Boolean` - sessions shorter than one minute are not stored
- `findSessions(playerUuid): Set<PlaytimeSession>`
- `findPlaytimeSeconds(playerUuid, server?, category?, excludedSessionId?): Long` - summed by
  the database; the caller adds its running session and excludes its stored save
- `findPlaytimePerPlayer(filter): Map<UUID, Long>`
- `findStreak(playerUuid): PlaytimeStreak?`
- `calculateStreak(playerUuid): Int` - not stored
- `updateLoginStreak(playerUuid, today): LoginStreakUpdate` - `Created` / `Updated`; the whole
  login streak update of a join (load, pauses, calculate, store) in one call
- `recalculateStreak(playerUuid): PlaytimeStreak?`, `recalculateAllStreaks(): Int`
- `findStreakPauses()`, `createStreakPause(startDate, endDate)`, `deleteStreakPause(pauseId): Boolean`

Redis: none.

## queue

No RPC. The queues live in Redis, one set of keys per target server:

| Kind              | Name                                                    | Purpose                                         |
|-------------------|---------------------------------------------------------|-------------------------------------------------|
| scored sorted set | `surf-player:queue:servers:<server>:entries`            | queued players by priority score                |
| map               | `surf-player:queue:servers:<server>:meta`               | `QueueEntry` per queued player                  |
| map               | `surf-player:queue:servers:<server>:last-seen`          | last heartbeat per queued player                |
| map               | `surf-player:queue:servers:<server>:retry-count`        | failed transfer attempts per player             |
| bucket            | `surf-player:queue:servers:<server>:paused`             | whether the queue is paused                     |
| bucket            | `surf-player:queue:servers:<server>:epoch-ms`           | creation time; its existence marks a queue, discovered by key scan every 30 s |
| semaphore         | `surf-player:queue:servers:<server>:transfer-semaphore` | one transfer at a time                          |
| semaphore         | `surf-player:queue:servers:<server>:cleanup-semaphore`  | one cleanup at a time                           |
| map cache         | `surf-player:queue:reconnects`                          | server a disconnected player is re-queued for, with per-entry TTL (Velocity) |

## settings

RPC: `SettingsRpcService`

- `findAllSettings(): List<Setting>`
- `createSettings(defaultValues: Map<String, String>): List<Setting>` - registers several
  settings in one transaction; every platform registers its built-in settings with one call
- `findPlayerSettings(playerUuid): Map<String, String>` - only values differing from the default
- `savePlayerSetting(playerUuid, settingName, value)`

Player settings are cached per server (and on the proxy) for the players online there; values of
players not cached on a server (`getCachedValueOrLoad`) are loaded with `findPlayerSettings`
without being cached. The microservice reads `friend:requests` directly from its tables.

| Kind  | Name                                                         | Purpose                                                                                     |
|-------|--------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| event | `PlayerSettingChangedRedisEvent(playerUuid, settingName, value)` | published after every `savePlayerSetting`; every server and proxy that has the player's settings cached updates its cache |

The listener (`SettingsRedisListener`) is subscribed by `SurfPlayerClient.load` on every platform.

## tab

No RPC.

| Kind  | Name                                     | Purpose                                                   |
|-------|------------------------------------------|-----------------------------------------------------------|
| event | `TabEntryUpdateRedisEvent(toUpdateUuid)` | every server re-renders the tab entry of that player       |

## vanish

No RPC.

| Kind     | Name                                    | Purpose                                                                                     |
|----------|-----------------------------------------|---------------------------------------------------------------------------------------------|
| sync map | `surf-player:vanish:vanished-players`   | vanished player UUID → vanish priority (`Int`); kept across server switches on purpose      |

The map is created on every platform by `SurfPlayerClient.load` (`VanishedPlayers` in
core-client) and only changed by the Paper vanish feature: it stores a player when they vanish
and removes them when they reappear. The value is the player's highest
`surf.player.vanish.priority.<n>` (0 without any), refreshed when a vanished player joins a Paper
server. It replaces the former sync set of the same name, so every server of the network must run
the same version.

Who sees a vanished player is decided by one rule, `VanishSight.canSee` in core-client: the
player themself, holders of `surf.player.vanish.bypass`, the console, and viewers whose own
priority (highest `surf.player.vanish.priority.<n>`, 0 with `surf.player.team.vanish`, none
otherwise) is at least the stored priority. It applies on every platform to in-game visibility,
tab list, maps, command suggestions and name lookups (`PlayerVisibility`), the friend list,
friend online/offline notices and the friend count in the join greeting. Minestom and Velocity read
the viewer's permission nodes from LuckPerms.

## Features without protocol

contentcreator (uses `ConnectionRpcService` through `SurfConnectionApi`), minimap, nametag,
profile and shrieker (tables only) have no RPC services, Redis keys or events of their own.
