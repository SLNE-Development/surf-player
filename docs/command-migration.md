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
