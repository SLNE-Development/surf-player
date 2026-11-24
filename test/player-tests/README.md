# surf-player automated player tests

In-game tests of every behaviour a player can observe, run by three headless Minecraft 26.3
clients against the local Docker test network ([docker/test-network](../../docker/test-network)).
Everything that unit tests in the plugin code can cover stays there; these scenarios check what
only a connected client sees: chat and system messages (with their prefix), action bars, titles,
sounds, tab list (entries, order, header/footer), scoreboard teams (nametags), menus (titles,
items, lore, clicks), tab completion, click events, plus logs, database, Redis and RabbitMQ.

Standalone Gradle build (not part of the plugin build in the repository root).

## Run

```sh
test/player-tests/run.sh                       # build + deploy if needed, wait, run everything
test/player-tests/run.sh --feature chat        # one feature (repeatable / comma separated)
test/player-tests/run.sh --id friends-12       # single row; --id "clan-*" for a prefix
test/player-tests/run.sh --failed              # rerun what was bad/odd in the last report
test/player-tests/run.sh --plan path/to/tests.json   # merge results into a test plan
test/player-tests/run.sh --no-build --no-deploy --list
test/player-tests/run.sh --destructive         # also scenarios that stop/restart containers
test/player-tests/run.sh --parallel 1          # everything serially with one bot group
```

Runs are parallel: scenarios marked `isolated` in `dsl/Isolation.kt` (they only touch their own
bots: friends, own clan, ignore, PMs, spy, own settings, menus, …) run on `--parallel N` bot groups
at once (default 6); everything else is `global` (server-wide toggles, config edits/reloads,
vanish, tab counts, queue, infra) and runs afterwards with only group 1 online. Unknown ids are
global. The summary prints the wall clock and the serial sum of all scenario durations.

`run.sh` (POSIX sh, works in Git Bash) builds the surf-player jars when sources are newer than
the deployed jars, deploys them with `docker/test-network/fetch-plugins.sh` (which rejects corrupt
jars), restarts the servers whose jars changed, waits until all servers are up, runs the scenarios
and prints a summary. Exit code 1 when any scenario is `bad`.

Report: `build/report.json`, one entry per scenario: `id`, `feature`, `status`
(`ok` | `bad` | `odd` | `skip`), `note` (German, what the bot observed), `durationMs`. Results of
scenarios not run this time are kept, so `--failed` and filtered runs update the same report.
With `--plan`, every row of the plan gets `"result": {"status", "note", "by": "bot"}`.

Look at a single command by hand (prints every packet text, menu, tab list and team):

```sh
cd test/player-tests && ../../gradlew -q installDist
MSYS_NO_PATHCONV=1 build/install/player-tests/bin/player-tests --probe DevAdmin paper "/friend list" "Hallo"
```

## Accounts

| Bot          | Plays the role of | Permissions                         |
|--------------|-------------------|-------------------------------------|
| `DevAdmin`   | NotAmmo (admin)   | group `admin` (`*`)                 |
| `DevPlayer1` | FPNS              | group `default` (`surf.player.command.*`) |
| `DevPlayer2` | third account     | group `default`                     |

They join through the offline proxy `velocity-bots` on `127.0.0.1:25566`; LuckPerms knows them by
their offline UUIDs (`docker/test-network/luckperms/init.sh`). Parallel groups 2..N use
`DevA<n>` (admin), `DevP<n>a`, `DevP<n>b` and are provisioned by the runner at start. Scenarios
address roles (`admin`/`p1`/`p2`, names `A`/`P1`/`P2`, clan `CN`/`CT`), grant extra permissions
temporarily (`withPermission`) and only ever touch their own group's data. Result notes of other
groups are rewritten to the role names DevAdmin/DevPlayer1/DevPlayer2.

## Library

[MCProtocolLib](https://github.com/GeyserMC/MCProtocolLib) `26.3-4` (GeyserMC), which speaks
protocol 777 (Minecraft 26.3) natively; no ViaVersion layer is needed. Known protocol quirks the
bot works around: no movement packets after a server teleport (Paper 26.3 kicks with
"Invalid move player packet"), client options sent in the configuration phase, automatic
respawn (dead players cannot chat). MCProtocolLib's text serializer drops the payload of
`custom` click events, which Paper's `ClickEvent.callback` (confirm buttons, [✘], teleport) needs;
`src/main/java/org/geysermc/adventure/text/serializer/nbt/ClickEventSerializerImpl.java` replaces
that class on the harness class path and keeps the payload.

## Layout

| Path | What |
|------|------|
| `src/main/kotlin/.../bot/Bot.kt` | one client: connection, everything it receives, actions (chat, command, click events, slots, tab completion, sneak, swap hands) |
| `src/main/kotlin/.../dsl/` | scenario DSL (`scenario("id") { ... }`, `expect`, `expectNone`, `clickIn`, verdicts), bot manager |
| `src/main/kotlin/.../infra/` | Docker access (consoles, logs, psql, redis-cli, RabbitMQ API), YAML edits |
| `src/main/kotlin/.../scenarios/` | one file per feature, one scenario per test-plan row id, `Fixtures.kt` for set-up/clean-up |
| `run.sh` | the entry point |

## Writing a scenario

```kotlin
scenario("friends-05", "add: an sich selbst") {
    on(PAPER, p1)                                   // DevPlayer1 on Paper
    val m = p1.run("friend add DevPlayer1")         // mark + command
    val r = p1.expect("keine Freundschaftsanfrage selbst senden", since = m, prefix = Prefix.Error)
    note("„${r.plain.trim()}“")                     // becomes the German result note
}
```

- One scenario per behaviour (test-plan row id). It sets up and cleans up its own state
  (`resetFriends()`, `resetClans()`, `withConfig(...)`, `withPermission(...)`, `setSetting(...)`).
- A failed expectation ends the scenario as `bad` with what the bot actually received;
  `odd(...)` for "works, but something looks wrong", `skip(...)`/`notAutomatable(...)` with the
  reason when a bot cannot observe it.
- Scenarios that stop or restart containers call `requireDestructive()` first.
