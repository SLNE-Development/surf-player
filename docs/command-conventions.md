# Command conventions

Every command of surf-player follows these rules, on Paper, Minestom and Velocity alike.

## 1. Three kinds of commands

| Kind | Who | Where | Permission |
|------|-----|-------|------------|
| Player | every player | one root command per feature (`/friend`, `/clan`, …) plus a few short aliases | `surf.player.command.<feature>[.<sub>]` |
| Team | staff in game (moderation tools used while playing) | own root command | `surf.player.team.<name>` |
| Admin | administration, maintenance, debugging | **only** below `/surfplayer` | `surf.player.admin.<feature>.<action>` |

- **Admin** means: reloads, cache resets, recalculations, debug switches, server-wide toggles,
  lookups in stored data, deleting other people's data, pausing queues. All of it lives under one
  command: `/surfplayer <feature> <action> [arguments]`. There are no other admin roots
  (`/playtimeadmin`, `/surftab`, `/surfchat`, `/surfcontentcreator`, `/minimapcompanion`,
  `/squeue`, `/squeue-velocity`, `/surfsocial`, `/clan admin …` are gone).
- **Team** commands are the moderation tools staff use while playing: `/vanish`, `/teamchat`
  (`/tc`), `/spy`, `/slowchat`. They keep short top-level names.
- **Player** commands are everything a normal player uses.

## 2. `/surfplayer`

- `/surfplayer` alone lists the features the sender may administrate on this platform;
  `/surfplayer <feature>` lists that feature's actions. A feature or action the sender lacks the
  permission for is not shown and not suggested.
- Each platform registers the subtrees of the features it hosts; a feature contributes its subtree
  through its runtime feature object.
- Velocity uses **`/surfplayervelocity`** with the Velocity features (clan, connection, queue,
  settings). A proxy command with the same name as a backend command could not be run in game,
  because the backend's command would take precedence, so proxy commands never share a name with
  a Paper or Minestom command.
- Actions that make sense without a player (reloads, recalculations, queue control) accept the
  console as sender.
- Every feature with a config has `/surfplayer <feature> reload`.

## 3. Player commands

- One root per feature; the root alone shows the same styled help: one line per subcommand the
  sender may use, with arguments and a short German description.
- Subcommand verbs are the same everywhere: `add`, `remove`, `list`, `info`, `accept`, `deny`,
  `revoke`, `create`, `delete`, `leave`, `invite`, `kick`, `promote`, `demote`, `toggle`. No
  synonyms (`decline` → `deny`, `show` → `info`).
- Short aliases exist only for the most used actions and are listed in the help: `/msg`
  (`/dm`, `/w`, `/whisper`, `/tell`, `/pm`), `/r`, `/cc` and `/clanchat` (= `/clan chat`),
  `/pt` (= `/playtime`). `/friend` has no short forms.
- Arguments look the same everywhere: a player argument suggests players online in the network
  (names, case-insensitive), a clan argument suggests clan tags, pages are `[seite]`.
- Vanished players stay hidden: every player argument goes through `PlayerVisibility`
  (core-client) and the platform's `player-arguments.kt` (`visiblePlayerArgument`,
  `visibleOfflinePlayerArgument`, `visibleLocalPlayerArgument`). A vanished player is only
  suggested to themself, to senders with `surf.player.vanish.bypass` and to the console; for
  everybody else, typing their name behaves exactly as for an offline or unknown player. Never use
  surf-core's `surfPlayerArgument`/`surfOfflinePlayerArgument` or entity selectors directly.
- Destructive actions (`/clan delete`, `/clan leave`, `/friend remove`) ask for confirmation with a
  clickable button.

## 4. Messages

- All player-facing messages are German, built with the surf-api message builder
  (`appendSuccessPrefix` / `appendErrorPrefix` / `appendInfoPrefix` …) so prefix, colours and
  tone are the same in every feature. Success uses the success prefix, failures the error prefix,
  neutral information the info prefix.
- The same situation produces the same text in every feature: unknown player, no permission,
  player not online, missing argument, cooldown.
- Errors abort the command (`throw CommandAPI.failWithString(...)` / the shared `failCommand`).

## 5. Permissions

`surf.player.command.<feature>[.<sub>]` for player commands, `surf.player.team.<name>` for team
commands, `surf.player.admin.<feature>.<action>` for admin actions (`surf.player.admin` to see
`/surfplayer` at all). Other permissions a feature checks (bypasses, extra visibility) are
`surf.player.<feature>.<name>`. All permission nodes of a feature are declared in one
`<Feature>Permissions` object.

The old → new mapping of every command and permission node is listed in
[docs/command-migration.md](command-migration.md); LuckPerms groups on the live network must be
updated with it.
