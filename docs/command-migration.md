# Command and permission migration

Every command and permission node of surf-player was renamed to the scheme in
[command-conventions.md](command-conventions.md). This page maps the old names to the new ones so
the LuckPerms groups of the live network can be updated.

## What to do on the live network

1. Grant every player command to the default group (Bukkit treats unknown nodes as op-only, so
   players need them explicitly):

   ```
   lp group default permission set surf.player.command.* true
   ```

   Perks are deliberately **not** below `surf.player.command`: `surf.player.clan.color.*` and
   `surf.player.playtime.others` must still be granted to the ranks that had the old nodes.
2. Give staff groups `surf.player.team.<name>` for the team tools they had, and the admin groups
   `surf.player.admin` plus the `surf.player.admin.<feature>.<action>` nodes they need
   (`surf.player.admin.*` for everything).
3. Replace every old node below with its new node, then remove the old nodes. Nodes marked
   *removed* have no successor.

## Commands

### Player commands (Paper and Minestom unless noted)

| Old command | New command |
|---|---|
| `/msg`, `/dm`, `/w`, `/whisper`, `/tell`, `/pm` | unchanged |
| `/reply`, `/r` | unchanged |
| `/ignore <player>` (toggle) | `/ignore add <spieler>`, `/ignore remove <spieler>` |
| `/ignore #list` | `/ignore list [seite]` |
| `/friend`, `/f` | `/friend` (shows the help; `/f` removed) |
| `/friend add <player>`, `/fa` | `/friend add <spieler>` (`/fa` removed) |
| `/friend list`, `/fl` | `/friend list [seite]` (`/fl` removed) |
| `/friend info <friend>` | unchanged |
| `/friend remove <friend>` | unchanged, now asks for confirmation |
| `/friend requests` | `/friend requests [seite]` |
| `/friend accept <player>` | unchanged |
| `/friend decline <player>` | `/friend deny <spieler>` |
| `/friend revoke <player>` | unchanged |
| `/clan` | `/clan` (shows the help) |
| `/clan create <name> <tag>` | unchanged |
| `/clan disband` | `/clan delete` (asks for confirmation) |
| `/clan leave` | unchanged (asks for confirmation) |
| `/clan info [clanTag]` | `/clan info [clan]` |
| `/clan whois <player>` | unchanged |
| `/clan list` | `/clan list [seite]` |
| `/clan members [clan]` | unchanged |
| `/clan setdiscord <link\|NULL>` | `/clan discord <link\|remove>` (`NULL` still accepted) |
| `/clan invite <player>` | unchanged |
| – | `/clan revoke <spieler>` (new: withdraws an invitation) |
| `/clan accept <clan name>` | `/clan accept <clan>` (tag, the name still works) |
| `/clan deny <clan name>` | `/clan deny <clan>` (tag, the name still works) |
| `/clan promote/demote/kick <member>` | unchanged |
| `/clan options tagcolor foreground\|background\|shadow <color>` | `/clan color foreground\|background\|shadow <farbe>` (`#` optional) |
| `/clan player settings invite [bool]` | removed: setting `clan:invites` in `/settings` |
| `/clan chat <message>`, `/cc`, `/clanchat` | `/clan chat <nachricht>`, `/cc`, `/clanchat` |
| `/playtime [player]`, `/pt` | unchanged |
| `/profile` | unchanged |
| `/settings` | unchanged (opens the menu) |
| `/settings <setting> [on\|off]` | `/settings toggle <einstellung> [an\|aus]` |
| `/settings <setting> #info` | `/settings info <einstellung>` |
| – | `/settings help` |
| `/link [code]`, `/unlink` (Velocity) | unchanged |
| `/nick <name>`, `/nick #unnick` (Paper) | removed (nick feature removed) |

### Team commands

| Old command | New command |
|---|---|
| `/teamchat`, `/tc` | unchanged |
| `/spy <player>` (toggle) | `/spy add <spieler>`, `/spy remove <spieler>` (network-wide: any player online in the network) |
| `/spy #clear` | `/spy clear` |
| `/slowchat #status` | `/slowchat info` |
| `/slowchat <on\|off>` | `/slowchat toggle [an\|aus]` |
| `/vanish`, `/vanish spectate` (Paper) | unchanged |

### Admin commands

Paper and Minestom: `/surfplayer`; Velocity: `/surfplayervelocity`.

| Old command | New command |
|---|---|
| `/surfchat reload`, `/sc reload` | `/surfplayer chat reload` |
| `/surfchat lookup [query]` | `/surfplayer chat lookup [--filter wert ...]` (`--message` now works; filter names and values are tab-completed) |
| `/surfchat lookuphelp` | `/surfplayer chat filters [seite]` |
| `/surfchat functionality status` | `/surfplayer chat status` |
| `/surfchat functionality change <on\|off>` | `/surfplayer chat toggle <an\|aus>` |
| `/surfchat functionality list [page]` | `/surfplayer chat servers [seite]` |
| `/clan admin reload` | `/surfplayer clan reload` (Velocity: `/surfplayervelocity clan reload`) |
| `/clan admin invalidateAllCaches` | `/surfplayer clan invalidatecaches` |
| `/clan admin delete <clan>` | `/surfplayer clan delete <clan>` |
| – | `/surfplayer friends cooldown clear <spieler> [ziel]` (new: deletes friend request cooldowns) |
| `/playtimeadmin reload` | `/surfplayer playtime reload` |
| `/playtimeadmin streak recalculate <player>` | `/surfplayer playtime recalculate <spieler>` |
| `/playtimeadmin streak recalculateall` | `/surfplayer playtime recalculate` |
| `/playtimeadmin streakpause create <von> <bis>` | `/surfplayer playtime pause create <von> <bis>` |
| `/playtimeadmin streakpause delete <id>` | `/surfplayer playtime pause delete <id>` |
| `/playtimeadmin streakpause list` | `/surfplayer playtime pause list` |
| `/squeue list\|info\|enqueue\|dequeue\|clear\|cleanup\|fix [...]` | `/surfplayer queue list\|info\|enqueue\|dequeue\|clear\|cleanup\|fix [...]` |
| `/squeue pause pause\|resume\|status [server]` | `/surfplayer queue pause\|resume\|status [server]` |
| `/squeue metrics startLogging\|stopLogging\|snapshot` | `/surfplayer queue metrics start\|stop\|snapshot` |
| – | `/surfplayer queue reload` |
| `/squeue-velocity pause <server> pause\|resume\|status` | `/surfplayervelocity queue pause\|resume\|status <server>` |
| – | `/surfplayervelocity queue reload` |
| `/surfSettings refresh\|list` | `/surfplayer settings refresh\|list` (Velocity: `/surfplayervelocity settings ...`) |
| `/surftab reload` | `/surfplayer tab reload` |
| – | `/surfplayer nametag reload` |
| `/surfcontentcreator debug live\|offline` | `/surfplayer contentcreator debug live\|offline` |
| – | `/surfplayer contentcreator reload` |
| `/minimapcompanion reload` | `/surfplayer minimap reload` |
| `/minimapcompanion info <player>` | `/surfplayer minimap info <spieler>` |
| `/vanish reload` | `/surfplayer vanish reload` |
| `/surfsocial reload` (Velocity) | `/surfplayervelocity connection reload` |
| `/link lookup <player>` (Velocity) | `/surfplayervelocity connection lookup <spieler>` |

## Permission nodes

### Admin root

| Old node | New node |
|---|---|
| – | `surf.player.admin` (needed to see `/surfplayer` and `/surfplayervelocity`) |

### Chat

| Old node | New node |
|---|---|
| `surf.chat.command.msg` | `surf.player.command.msg` |
| `surf.chat.command.reply` | `surf.player.command.reply` |
| `surf.chat.command.ignore` | `surf.player.command.ignore`, `surf.player.command.ignore.add`, `surf.player.command.ignore.remove` |
| `surf.chat.command.ignore.list` | `surf.player.command.ignore.list` |
| `surf.chat.command.teamchat` | `surf.player.team.teamchat` (also receives team chat) |
| `surf.chat.command.direct-spy` | `surf.player.team.spy` |
| `surf.chat.command.direct-spy.clear` | `surf.player.team.spy` |
| `surf.chat.slowchat.command` | `surf.player.team.slowchat` |
| `surf.chat.command.surfchat` | `surf.player.admin` |
| `surf.chat.command.surfchat.reload` | `surf.player.admin.chat.reload` |
| `surf.chat.command.surfchat.lookup` | `surf.player.admin.chat.lookup` |
| `surf.chat.command.surfchat.lookup.help` | `surf.player.admin.chat.lookup` |
| `surf.chat.command.surfchat.functionality` | removed (the three actions below have their own nodes) |
| `surf.chat.command.surfchat.functionality.status` | `surf.player.admin.chat.status` |
| `surf.chat.command.surfchat.functionality.toggle` | `surf.player.admin.chat.toggle` |
| `surf.chat.command.surfchat.functionality.list` | `surf.player.admin.chat.servers` |
| `surf.chat.command.surfchat.delete` | `surf.player.chat.delete` (delete button on messages) |
| `surf.chat.command.surfchat.teleport` | `surf.player.chat.teleport` (`[TP]` button in front of the sender's name in chat and in the PM spy; clicking a name now suggests `/msg <name>` for everyone) |
| `surf.chat.bypass.filter` | `surf.player.chat.bypass.filter` |
| `surf.chat.disabling.bypass` | `surf.player.chat.bypass.disabling` |
| `surf.chat.bypass.functionality` | `surf.player.chat.bypass.functionality` |
| `surf.chat.bypass.spy` | `surf.player.chat.bypass.spy` |
| `surf.chat.slowchat.bypass` | `surf.player.chat.bypass.slowchat` |
| `surf.chat.slowchat.notify` | `surf.player.chat.notify.slowchat` |
| `surf.chat.team.notify.functionality` | `surf.player.chat.notify.functionality` |
| `surf.chat.team.notify.deletion` | `surf.player.chat.notify.deletion` |
| `surf.chat.team` (AI moderation notices) | `surf.player.chat.notify.moderation` |
| `surf.chat.connection.always-show` | `surf.player.chat.connection.always-show` (now: the holder sees every join/quit message, despite their setting and the config; of vanished players only if they may see them) |
| – | `surf.player.chat.ignore.exempt` (new, team: cannot be ignored; messages, private messages and friend requests get through older ignores) |

### Clan

| Old node | New node |
|---|---|
| `surf.clan.command.clan` | `surf.player.command.clan` |
| `surf.clan.command.clan.create` | `surf.player.command.clan.create` |
| `surf.clan.command.clan.disband` | `surf.player.command.clan.delete` |
| `surf.clan.command.clan.leave` | `surf.player.command.clan.leave` |
| `surf.clan.command.clan.info` | `surf.player.command.clan.info` |
| `surf.clan.command.clan.whois` | `surf.player.command.clan.whois` |
| `surf.clan.command.clan.setDiscord` | `surf.player.command.clan.discord` |
| `surf.clan.command.clan.invite` | `surf.player.command.clan.invite` |
| – | `surf.player.command.clan.revoke` |
| `surf.clan.command.clan.acceptInvite` | `surf.player.command.clan.accept` |
| `surf.clan.command.clan.denyInvite` | `surf.player.command.clan.deny` |
| `surf.clan.command.clan.promoteMember` | `surf.player.command.clan.promote` |
| `surf.clan.command.clan.demoteMember` | `surf.player.command.clan.demote` |
| `surf.clan.command.clan.kickMember` | `surf.player.command.clan.kick` |
| `surf.clan.command.clan.viewMembers` | `surf.player.command.clan.members` |
| `surf.clan.list` | `surf.player.command.clan.list` |
| `surf.clan.chat` | `surf.player.command.clan.chat` |
| `surf.clan.command.options` | `surf.player.command.clan.color` |
| `surf.clan.command.options.tagColor` | `surf.player.command.clan.color` |
| `surf.clan.command.options.tagColor.foreground` | `surf.player.clan.color.foreground` (perk, also checked for the clan owner on join) |
| `surf.clan.command.options.tagColor.background` | `surf.player.clan.color.background` (perk) |
| `surf.clan.command.options.tagColor.shadow` | `surf.player.clan.color.shadow` (perk) |
| `surf.clan.player` | removed |
| `surf.clan.player.settings` | removed |
| `surf.clan.player.settings.invite` | removed (setting `clan:invites` in `/settings`) |
| `surf.clan.admin` | `surf.player.admin` |
| `surf.clan.admin.reload` | `surf.player.admin.clan.reload` |
| `surf.clan.admin.invalidateCache` | `surf.player.admin.clan.invalidatecaches` |
| `surf.clan.admin.delete` | `surf.player.admin.clan.delete` |

### Friends

| Old node | New node |
|---|---|
| `surf.friends.command` | `surf.player.command.friend` |
| `surf.friends.command.request.send` | `surf.player.command.friend.add` |
| `surf.friends.command.list` | `surf.player.command.friend.list` |
| `surf.friends.command.info` | `surf.player.command.friend.info` |
| `surf.friends.command.remove` | `surf.player.command.friend.remove` |
| `surf.friends.command.request.list` | `surf.player.command.friend.requests` |
| `surf.friends.command.request.accept` | `surf.player.command.friend.accept` |
| `surf.friends.command.request.decline` | `surf.player.command.friend.deny` |
| `surf.friends.command.request.revoke` | `surf.player.command.friend.revoke` |
| `surf.friends.command.jump` | removed (was never used) |
| – | `surf.player.friends.cooldown.bypass` (new, staff: friend requests ignore cooldowns) |
| – | `surf.player.admin.friends.cooldown` (new: `/surfplayer friends cooldown clear`) |

### Playtime

| Old node | New node |
|---|---|
| `surf.playtime.command` | `surf.player.command.playtime` |
| `surf.playtime.command.others` | `surf.player.playtime.others` (perk) |
| `surf.playtime.command.admin` | `surf.player.admin.playtime.reload`, `surf.player.admin.playtime.recalculate`, `surf.player.admin.playtime.pause` |

### Profile

| Old node | New node |
|---|---|
| `surf.profile.command.profile` | `surf.player.command.profile` |

### Settings

| Old node | New node |
|---|---|
| `surf.settings.command.settings` | `surf.player.command.settings`, `surf.player.command.settings.toggle`, `surf.player.command.settings.info` |
| `surf.settings.command.surfsettings` | `surf.player.admin` |
| `surf.settings.command.surfsettings.refresh` | `surf.player.admin.settings.refresh` |
| `surf.settings.command.surfsettings.list` | `surf.player.admin.settings.list` |

### Queue

| Old node | New node |
|---|---|
| `surf.queue.command.queue` (Paper, Minestom, Velocity) | `surf.player.admin` |
| `surf.queue.command.queue.list` | `surf.player.admin.queue.list` |
| `surf.queue.command.queue.info` | `surf.player.admin.queue.info` |
| `surf.queue.command.queue.enqueue` | `surf.player.admin.queue.enqueue` |
| `surf.queue.command.queue.dequeue` | `surf.player.admin.queue.dequeue` |
| `surf.queue.command.queue.clear` | `surf.player.admin.queue.clear` |
| `surf.queue.command.queue.cleanup` | `surf.player.admin.queue.cleanup` |
| `surf.queue.command.queue.fix` | `surf.player.admin.queue.fix` |
| `surf.queue.command.queue.pause` (Paper, Minestom) | `surf.player.admin.queue.pause`, `surf.player.admin.queue.resume`, `surf.player.admin.queue.status` |
| `surf.queue.command.pause` (Velocity) | `surf.player.admin.queue.pause`, `surf.player.admin.queue.resume`, `surf.player.admin.queue.status` |
| `surf.queue.command.metrics` | `surf.player.admin.queue.metrics` |
| – | `surf.player.admin.queue.reload` |

### Tab, nametag, content creator, minimap

| Old node | New node |
|---|---|
| `surf.tab.command.surftab` | `surf.player.admin.tab.reload` |
| – | `surf.player.admin.nametag.reload` |
| `surf.contentcreator.command` | `surf.player.admin.contentcreator.debug` |
| – | `surf.player.admin.contentcreator.reload` |
| `surf.minimapcompanion.command` | `surf.player.admin` |
| `surf.minimapcompanion.command.reload` | `surf.player.admin.minimap.reload` |
| `surf.minimapcompanion.command.info` | `surf.player.admin.minimap.info` |

### Vanish

| Old node | New node |
|---|---|
| `surf.vanish.command` | `surf.player.team.vanish` (`/vanish` and `/vanish spectate`) |
| `surf.vanish.command.reload` | `surf.player.admin.vanish.reload` |
| `surf.vanish.command.nick` | removed (nick feature removed) |
| `surf.vanish.bypass` | `surf.player.vanish.bypass` (also shows vanished friends as online in `/friend list`) |
| `surf.vanish.save-fly-state` | `surf.player.vanish.save-fly-state` |
| `surf.vanish.priority.<n>` | `surf.player.vanish.priority.<n>` |

### Connection (Velocity)

| Old node | New node |
|---|---|
| `surf.social.command.link` | `surf.player.command.link` |
| `surf.social.command.unlink` | `surf.player.command.unlink` |
| `surf.social.command.link.lookup` | `surf.player.admin.connection.lookup` |
| `surf.social.command.surfsocial` | `surf.player.admin.connection.reload` |

## Behaviour changes

Deliberate differences from the 14 original plugins that are kept (from the parity audit). One
line each; everything not listed behaves as before.

### Chat

- Caps filter: `spamConfig.maxUppercaseCharsInRow` is the maximum *allowed* run of capitals. With
  the default 5, "HALLO" is allowed and 6 capitals in a row are blocked (surf-chat blocked 5).
- Spam filter: `spamConfig.amount` messages are allowed per `interval`; the next one is blocked.
  Blocked messages no longer count towards the limit, and the repeat check runs before the rate check.
- AI moderation only runs with `enabled: true` **and** a non-blank API key; `passAutoMod` honours
  `enabled` as well.
- `/surfplayer chat lookup` with an unknown `--sender`, `--receiver` or `--deletedBy` name aborts
  with "player not found" instead of silently dropping that filter.
- The chat-disabled rejection reads "Der Chat ist auf diesem Server derzeit deaktiviert." (was
  "Der Chat ist vorübergehend deaktiviert.").
- Paper join/quit messages: as in surf-chat, a message another plugin removed (set to `null`) is
  left alone, and `PlayerJoinMessageEvent`/`PlayerQuitMessageEvent` and the chat MOTD only follow a
  public announcement (connection messages enabled, player not vanished, event not cancelled). New:
  holders of `surf.player.chat.connection.always-show` still get our message when connection
  messages are disabled or the player is vanished (vanished only if they may see them); no event is
  fired for it. Minestom sends the MOTD on every join, as before.
- MiniPlaceholders is required on Paper (surf-chat had a plain-MiniMessage fallback); see the
  deployment checklist.

### Clan

- Clicking a clan in `/clan list` runs `/clan info <tag>`, so it needs
  `surf.player.command.clan.info` (surf-clan rendered the info through a callback).
- Clan invite opt-outs live in the setting `clan:invites`; the microservice copies the old
  `accepts_clan_invites = false` values once on startup (see the deployment checklist).

### Friends

- The join greeting counts friends online anywhere in the network (minus vanished friends the
  player may not see), not only on the same server.

### Settings

- The settings menu marks the selected value with "»" (was a "▪" marker with the value in bold).
- The menu label of `friend:request-notifications` is "Freundschaftsanfrage-Benachrichtigungen"
  (was "Freundschaftsanfragen"), to tell it apart from the new `friend:requests` setting.
- `deleteSetting` / `DeleteSettingRequestPacket` are gone; settings are deleted with SQL (see the
  deployment checklist).
- `/surfplayer settings list` shows the settings this server declares and, separately, those
  only known from the database (created by plugins on other servers). Every server knows every
  stored setting, as surf-settings did.

### Nametag

- The setting `nametag:show-nametags` now works: with `false` the viewer sees no nametags of other
  players. surf-nametag never read it, so players who switched it off earlier see no nametags after
  the switch; the stored values are kept (see the deployment checklist).

### Vanish

- Spectate hotkeys: "Zurück" needs the sneak key held while pressing swap-offhand; a double sneak
  counts only key presses (surf-vanish used swap-offhand within 1 s after a sneak packet).
- A vanished player who joins no longer gets the "hat den Server unsichtbar betreten" notice about
  themself.

### Playtime

- Your own `/playtime` loads the streak from the microservice (10 s timeout) instead of reading
  only the cache.
- `AfkStateChangeEvent` is fired on the global region thread on Paper, as surf-playtime did.

### Queue

- `/surfplayer queue clear` removes the entries but keeps a paused queue paused; resume it with
  `/surfplayer queue resume` (surf-queue's clear also resumed it).
- surf-queue's bStats integration was dead code (never started) and was removed.

### Messages

- Replies for "nothing found" or an empty list use the info prefix instead of the error prefix
  (command conventions §4): empty ignore list, chat lookup without results, no streak pauses,
  no sessions found, empty queue, no settings, no friend request cooldowns to delete.

### Other

- Permission nodes are no longer registered with Bukkit through surf-api `PermissionRegistry`;
  grant them explicitly (see "What to do on the live network").
- surf-social's unused `encryptUuid` helper was removed; `encryptionSecret` in the Velocity
  `SocialConfig` is no longer read.

## API changes for dependent plugins

The 14 plugin APIs are now one module, `dev.slne.surf.player:surf-player-api`, under
`dev.slne.surf.player.api.<feature>`. Most code only needs new imports, following the package table
and the general rules below. The tables after that list every public member that has no 1:1
counterpart with the same simple name in its new package. surf-shrieker only changed its package,
so it has no table. There are no deprecated aliases or typealiases in the new API, so everything
below is a hard break.

### Package mapping

| Repo | Old package | New package |
|---|---|---|
| surf-chat | `dev.slne.surf.chat.api.*` | `dev.slne.surf.player.api.chat.*` |
| surf-clan | `dev.slne.clan.api.*` | `dev.slne.surf.player.api.clan.*` (e.g. `clan.api.clan.Clan` → `player.api.clan.clan.Clan`) |
| surf-content-creator | `dev.slne.surf.content.creator.api.*` | `dev.slne.surf.player.api.contentcreator.*` |
| surf-friends | `dev.slne.surf.friends.api.*` | `dev.slne.surf.player.api.friends.*` |
| surf-minimap-companion | `dev.slne.surf.minimapcompanion.api` | `dev.slne.surf.player.api.minimap` |
| surf-nametag | `dev.slne.surf.nametag.api.*` | `dev.slne.surf.player.api.nametag.*` |
| surf-playtime | `dev.slne.surf.playtime.api.common.*` | `dev.slne.surf.player.api.playtime.*` |
| surf-playtime (events) | `dev.slne.surf.playtime.api.paper.event` / `.api.minestom.event` | `dev.slne.surf.player.api.playtime.event` |
| surf-profile | `dev.slne.surf.profile.api` | `dev.slne.surf.player.api.profile` |
| surf-queue | `dev.slne.surf.queue.api.*` | `dev.slne.surf.player.api.queue.*` |
| surf-settings | `dev.slne.surf.settings.api.*` | `dev.slne.surf.player.api.settings.*` |
| surf-shrieker | `dev.slne.surf.shrieker.api.*` | `dev.slne.surf.player.api.shrieker.*` |
| surf-social | `dev.slne.surf.social.api.*` | `dev.slne.surf.player.api.connection.*` |
| surf-tab | `dev.slne.surf.tab.api.*` | `dev.slne.surf.player.api.tab.*` |
| surf-vanish | `dev.slne.surf.vanish.api.*` | `dev.slne.surf.player.api.vanish.*` |

### General rules

See [api-conventions.md](api-conventions.md) for the full rules.

1. **One entry point per feature:** `Surf<Feature>Api`, used as `SurfXApi.member(...)` (and
   `SurfXApi.INSTANCE` to pass it around). Gone: top-level instance vals (`surfVanishApi`,
   `surfProfileApi`, `surfPlaytimeApi`, `chatProcessorRegistry`), extra services (`ClanService`,
   `ClanMemberService`, `ClanInviteService`, `ClanPlayerService`, `FriendsPlayerManager`,
   `SurfQueueService`), static helpers on domain types (`Clan.byTag`, `SurfQueue.byServer`, …) and
   the opt-in annotations `@InternalClanApi`, `@InternalSurfQueueApi`.
2. **One capability per feature:** per-player types (`FriendsPlayer`, `ClanPlayer`) are removed;
   their members are on `<Feature>Capability`, implemented by `SurfCommunityPlayer`
   (`SurfPlayerApi.player(uuid)` or `SurfCommunityPlayer(uuid)`). Player-specific entry-point
   functions take the player's `UUID` as first parameter.
3. **No platform types:** the API takes and returns UUIDs only, never Bukkit or Minestom
   `Player`/`OfflinePlayer`.
4. **Events are surf-api events** (`SurfSyncEvent`/`SurfAsyncEvent`): listen with
   `@SurfEventHandler` (`dev.slne.surf.api.core.event.SurfEventHandler`), not Bukkit
   `@EventHandler` or a Minestom event node. `handlerList`/`getHandlers()` and the `player` getters
   are gone; use `playerUuid`.
5. **Sealed results gained variants** (listed per repo): an exhaustive `when` needs a new branch.

### surf-chat

| Old API | Replacement |
|---|---|
| `SurfChatApi.getCachedIgnoreList(uuid)` | `SurfChatApi.cachedIgnoreList(uuid)` / `ChatCapability.cachedIgnoreList` |
| `SurfChatApi.getIgnoreList(uuid)` | `SurfChatApi.ignoreList(uuid)` / `ChatCapability.ignoreList()` |
| `SurfChatApi.sendSignedMessage(signedMessage, senderUuid, targetUuid, content)` | `SurfChatApi.sendSignedMessage(senderUuid, targetUuid, signedMessage, content)` (parameter order changed) / `ChatCapability.sendSignedMessage(targetUuid, signedMessage, content)` |
| `MessageRedirectorRegistry` (`register`, `clear`, `redirectors`) | `SurfChatApi.registerMessageRedirector(redirector)`; the registry is internal |
| `ChatProcessorRegistry` class + top-level `chatProcessorRegistry` | `SurfChatApi.registerChatProcessor(PreChatProcessor / PostChatProcessor)`; the registry is internal |
| `MessageValidationResult.MessageValidationError` (nested) | top-level `chat.message.MessageValidationError` |
| `MessageValidationError.DenylistedWord`, `.BadLink`, `.BadCharacters`, `.TooOften` | removed — these checks no longer exist (remaining: `EmptyContent`, `TooManyCaps`, `AutoDisabled`, `ChatDisabled`) |
| `MessageValidationResult.isSuccess()` | `!isFailure()` or `is MessageValidationResult.Success` |
| `MessageValidationResult.getErrorOrNull()` | `(result as? MessageValidationResult.Failure)?.error` |
| package `api.denylist` (`DenylistEntry`, `DenylistAction`, `DenylistActionType`) | removed — the denylist feature was dropped |

### surf-clan

| Old API | Replacement |
|---|---|
| `Clan.registerListener(l)` / `Clan.unregisterListener(l)` | `SurfClanApi.registerListener(l)` / `SurfClanApi.unregisterListener(l)` |
| `Clan.byPlayer(uuid)`, `UUID.findClanByPlayer()` | `SurfClanApi.findClanByPlayer(uuid)` / `ClanCapability.clan()` |
| `Clan.byUuid(uuid)`, `UUID.findClanByUuid()` | `SurfClanApi.findClanByUuid(clanUuid)` |
| `Clan.byTag(tag)` | `SurfClanApi.findClanByTag(tag)` |
| `Clan.validateClanNameAndTag(name, tag)` | `SurfClanApi.validateClanNameAndTag(name, tag)` |
| `Clan.createClan(name, tag, owner) { }` | `SurfClanApi.createClan(owner, name, tag) { }` (parameter order changed) / `ClanCapability.createClan(name, tag) { }` |
| `Clan.changeClanTagColor(update: ClanTagColor.Update)` | `Clan.changeClanTagColor(update: ClanTagColorUpdate)`; the lambda overload is unchanged except for the receiver type |
| `ClanTagColor.Update` | top-level `ClanTagColorUpdate` (still built with `ClanTagColor.update { }`) |
| `ClanTagColor.Update.Builder` | top-level `ClanTagColorUpdateBuilder` |
| `ClanTagColor.withDefaultsAsFallback(fg, bg, shadow)` | `ClanTagColor(fg ?: Clan.DEFAULT_CLAN_TAG_FOREGROUND_COLOR, bg ?: Clan.DEFAULT_CLAN_TAG_BACKGROUND_COLOR, shadow ?: Clan.DEFAULT_CLAN_TAG_SHADOW_COLOR)` |
| `ClanTagColor?.update { ... }` (lambda overload) | `colorOrNull.update(ClanTagColor.update { ... })` |
| `ClanService`, `ClanInviteService`, `ClanMemberService` (internal) | `SurfClanApi` |
| `ClanInvite.pendingInvitesByPlayer(uuid)` | `SurfClanApi.findClanInvites(uuid)` / `ClanCapability.clanInvites()` |
| `ClanInvite.pendingInviteByPlayerAndClanName(uuid, clanName)` | `SurfClanApi.findClanInvite(uuid, clanName)` / `ClanCapability.clanInvite(clanName)` |
| `ClanInviteAcceptResult` (exhaustive `when`) | new variant `ClanInviteAcceptResult.InviteNotFound` |
| `ClanMember.registerListener(l)` / `unregisterListener(l)` | `SurfClanApi.registerListener(l: ClanMemberListener)` / `unregisterListener(l)` |
| `ClanMember.byUuid(uuid)` | `SurfClanApi.findClanMember(uuid)` / `ClanCapability.clanMember()` |
| `ClanMember.byName(name)` | `SurfClanApi.findClanMemberByName(name)` |
| `ClanPlayer` / `ClanPlayer.byUuid(uuid)` | removed — use `SurfCommunityPlayer(uuid)` |
| `ClanPlayer.acceptsClanInvites` | `SettingsCapability.setting(SettingKeys.CLAN_INVITES)` |
| `ClanPlayer.setAcceptsClanInvites(value)` | `SettingsCapability.saveSetting(SettingKeys.CLAN_INVITES, value)` (returns `Unit`, not `Boolean`) |
| `ClanPlayerService` (internal) | removed — see `ClanPlayer` |
| `@InternalClanApi` (`api.util`) | removed — the internal services it marked no longer exist |

### surf-content-creator

| Old API | Replacement |
|---|---|
| `ContentCreatorApi` (incl. `INSTANCE`) | `SurfContentCreatorApi` |
| `ContentCreatorApi.renderLiveTag(uuid, space)` | `SurfContentCreatorApi.liveTag(uuid, space)` / `ContentCreatorCapability.liveTag(space)` |
| `ContentCreatorPlatform` | `StreamingPlatform` |
| `ContentCreatorPlatform.plattform` | `StreamingPlatform.type` |
| `PlatformType.toPlattform(name, state)` | `PlatformType.toStreamingPlatform(name, state)` |
| `ContentCreator.getPlatform(type)` | `ContentCreator.getStreamingPlatform(type)` |
| `StateChangeListener.onStateChanged(uuid, contentCreatorPlatform: ContentCreatorPlatform, newState)` | `onStateChanged(uuid, streamingPlatform: StreamingPlatform, newState)` (now a `fun interface`) |

### surf-friends

| Old API | Replacement |
|---|---|
| `FriendsPlayer` / `FriendsPlayer[uuid]` | `FriendsCapability` / `SurfCommunityPlayer(uuid)`, or `SurfFriendsApi.<member>(uuid, ...)` |
| `FriendsPlayer.surfPlayer()` | `uuid.toSurfPlayer()` (`api.friends.utils`) |
| `FriendsPlayer.sendFriendRequest(target: FriendsPlayer)` (and `revokeFriendRequest`, `acceptFriendRequest`, `declineFriendRequest`, `hasSentFriendRequest`, `hasReceivedFriendRequest`, `findFriendship`, `hasFriendship`, `removeFriendship`) | same names on `FriendsCapability`/`SurfFriendsApi`, taking a `UUID` instead of a `FriendsPlayer` |
| `FriendsPlayer.notificationsEnabled` | `FriendsCapability.friendNotificationsEnabled` / `SurfFriendsApi.friendNotificationsEnabled(uuid)` |
| `FriendsPlayer.soundsEnabled` | `FriendsCapability.friendSoundsEnabled` / `SurfFriendsApi.friendSoundsEnabled(uuid)` |
| `FriendsPlayerManager.findPlayer(uuid)` | `SurfCommunityPlayer(uuid)` |
| `FriendsPlayerManager.players` | removed — no per-player cache; use `SurfCommunityPlayer(uuid)` |
| `FriendsPlayerManager.invalidatePlayer(uuid)` | removed — internal cache handling |
| `SurfPlayer.friendPlayer()` | `SurfCommunityPlayer(surfPlayer.uuid)` |
| `FriendRequest.sender` / `.target`, `Friendship.player` / `.friend` | still there; type changed from `FriendsPlayer` to `SurfCommunityPlayer` |
| `FriendRequestCreateResult` (exhaustive `when`) | new variants `OnCooldown(targetUuid, remaining)`, `Blocked(targetUuid)`, `RequestsDisabled(targetUuid)` |

### surf-minimap-companion

| Old API | Replacement |
|---|---|
| `SurfMinimapCompanionApi` | `SurfMinimapApi` |
| `SurfMinimapCompanionApi.levelId(playerUuid: UUID)` | `SurfMinimapApi.minimapLevelId(playerUuid)` / `MinimapCapability.minimapLevelId()` |

### surf-nametag

| Old API | Replacement |
|---|---|
| `SurfNametagApi.preventNametagUpdate(uuid): Boolean` | `SurfNametagApi.preventNametagUpdates(uuid)` (returns `Unit`) / `NametagCapability.preventNametagUpdates()` |
| `SurfNametagApi.allowNametagUpdate(uuid): Boolean` | `SurfNametagApi.allowNametagUpdates(uuid)` (returns `Unit`) / `NametagCapability.allowNametagUpdates()` |

### surf-playtime

| Old API | Replacement |
|---|---|
| top-level `surfPlaytimeApi` | `SurfPlaytimeApi` (companion) / `SurfPlaytimeApi.INSTANCE` |
| `SurfPlaytimeApi.getTotalPlaytime(uuid): Long?` | now returns `Long` (non-null) / `PlaytimeCapability.totalPlaytime()` |
| `PlaytimeStreak.SimpleStreak` | removed — use `PlaytimeStreak` (`SurfPlaytimeApi.getLoginStreak(uuid)` / `PlaytimeCapability.loginStreak()`) |
| `playtime.api.paper.event.AfkStateChangeEvent` (Bukkit `Event`, `handlerList`) | `playtime.event.AfkStateChangeEvent(playerUuid, isAfk)`, a `SurfSyncEvent` fired on the global region thread; listen with `@SurfEventHandler` |
| `playtime.api.minestom.event.AfkStateChangeEvent(player: Player, isAfk)` (Minestom `PlayerEvent`) | the same `playtime.event.AfkStateChangeEvent`, UUID-based (`playerUuid`); listen with `@SurfEventHandler` |

### surf-profile

| Old API | Replacement |
|---|---|
| top-level `surfProfileApi` | `SurfProfileApi` (companion) / `SurfProfileApi.INSTANCE`; `openOwnProfileMenu(uuid)` unchanged, also `ProfileCapability.openProfileMenu()` |

### surf-queue

| Old API | Replacement |
|---|---|
| `SurfQueue.byServer(serverName)` / `SurfQueue.byServer(server)` | `SurfQueueApi.queue(serverName)` / `SurfQueueApi.queue(server)` |
| `SurfServer.queue()` (extension) | `SurfQueueApi.queue(server)` |
| `SurfQueueAvailableSlotsProvider.set(provider)` / `.get()` | `SurfQueueApi.availableSlotsProvider = provider` / `SurfQueueApi.availableSlotsProvider` |
| `SurfQueueService` (`api.service`, internal) | `SurfQueueApi` |
| `@InternalSurfQueueApi` | removed — the internal service it marked no longer exists |

### surf-settings

| Old API | Replacement |
|---|---|
| `SurfSettingsApi.getPlayerSettings(uuid)` (deprecated) | removed — read each value with `getSettingValue(uuid, key)` / `SettingsCapability.setting(key)` |
| `SurfSettingsApi.getPlayerSetting(uuid, name)` (deprecated) | `getSettingValue(uuid, key: SettingKey<T>)` / `SettingsCapability.setting(key)` |
| `SurfSettingsApi.createSetting(name, defaultValue)` (deprecated) | `createSetting(SettingKey.ofBoolean/ofInt/ofDouble/ofString(key, default))` |
| `SurfSettingsApi.saveSetting(uuid, playerSetting: PlayerSetting)` (deprecated) | `saveSetting(uuid, key, value)` / `SettingsCapability.saveSetting(key, value)` |
| `SurfSettingsApi.saveSetting(uuid, settingName, settingValue: String)` (deprecated) | `saveSetting(uuid, key, value)` (typed value) |
| `SurfSettingsApi.getSetting(name: String)` (deprecated) | `getSetting(key: SettingKey<T>)` or `getSettings().find { it.name == name }` |
| `SettingKeys.SHOW_SCOREBOARD` (`scoreboard:show-scoreboard`) | removed — the setting was dropped |

`PlayerSetting` still exists in the API, but no public member returns or accepts it any more. A
setting created by a plugin on another server is known on every server (lookups, `/settings`, the
menu), as in surf-settings.

### surf-social → connection

| Old API | Replacement |
|---|---|
| `SurfSocialApi` | `SurfConnectionApi` |
| `SurfSocialApi.findConnection(connectionClass, minecraftUuid)` | `SurfConnectionApi.findConnection(uuid, connectionClass)` (parameter order swapped) / `ConnectionCapability.socialConnection(connectionClass)` |
| `SurfSocialApi.findConnection<T>(minecraftUuid)` (reified extension) | `SurfCommunityPlayer(uuid).socialConnection<T>()`, or `SurfConnectionApi.findDiscordConnection(uuid)` / `findTwitchConnection(uuid)` |

### surf-tab

| Old API | Replacement |
|---|---|
| `SurfTabApi.instance` (lowercase) | `SurfTabApi.INSTANCE` |
| `SurfTabApi.updateEntry(uuid)` | `SurfTabApi.updateTabEntry(playerUuid)` / `TabCapability.updateTabEntry()` |
| `SurfTabApi.broadcastEntryUpdate(uuid)` | `SurfTabApi.broadcastTabEntryUpdate(playerUuid)` / `TabCapability.broadcastTabEntryUpdate()` |
| `api.redis.TabEntryUpdateRedisEvent` | removed from the API (internal now) — call `SurfTabApi.broadcastTabEntryUpdate(uuid)` |

`TabEntryRenderEvent.order` keeps surf-tab's meaning: the LuckPerms weight, plus 1,000,000 for a
vanished player (higher is listed first).

### surf-vanish

| Old API | Replacement |
|---|---|
| top-level `surfVanishApi` (deprecated) | `SurfVanishApi` / `SurfVanishApi.INSTANCE` |
| `SurfVanishApi.isVanished(player: OfflinePlayer)` | `SurfVanishApi.isVanished(uuid)` / `VanishCapability.isVanished` |
| `SurfVanishApi.vanishedPlayers(): ObjectSet<OfflinePlayer>` | `SurfVanishApi.vanishedPlayers(): ObjectSet<UUID>` |
| `SurfVanishApi.onlineVanishedPlayersUuid()` | `SurfVanishApi.onlineVanishedPlayers()` |
| `PlayerVanishEvent` / `PlayerReappearEvent` (Bukkit `Event`, `player`, `handlerList`) | same names in `api.vanish.event`, now `SurfSyncEvent(playerUuid)`; listen with `@SurfEventHandler`; use `Bukkit.getPlayer(event.playerUuid)` instead of `event.player` |
| `PlayerNickEvent`, `PlayerUnNickEvent` | removed — the nick feature was dropped |
