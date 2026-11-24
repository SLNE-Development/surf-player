---
name: automated-player-tests
description: Run, read and extend the automated in-game player tests of surf-player (three headless bots in test/player-tests) against the local Docker test network.
---

# Automated player tests (surf-player)

Headless Minecraft 26.3 bots (MCProtocolLib) play the test plan against `docker/test-network`.
Code: `test/player-tests` (standalone Gradle build). Entry point: `test/player-tests/run.sh`.

## Prerequisites

- Docker Desktop running; Java 25 on PATH (Gradle toolchain resolves it otherwise).
- `docker/test-network/.env` exists (copy `.env.example`, replace every `change-me`).
- Network started: `cd docker/test-network && ./fetch-plugins.sh && docker compose up -d`.
  Bots use the offline proxy `velocity-bots` on `127.0.0.1:25566`; humans use 25565.
- In Git Bash always export `MSYS_NO_PATHCONV=1` before running the binary yourself (otherwise
  `/msg` becomes `C:/Program Files/Git/msg`). `run.sh` does this.

## Run

```sh
test/player-tests/run.sh                         # build jars if stale, deploy, wait, run all
test/player-tests/run.sh --feature clan          # one feature (comma separated for several)
test/player-tests/run.sh --id friends-12         # one row; --id "chat-6*" for a prefix
test/player-tests/run.sh --failed                # rerun last bad/odd
test/player-tests/run.sh --no-build --no-deploy --feature tab   # quick iteration
test/player-tests/run.sh --plan <tests.json>     # merge results into the test plan
test/player-tests/run.sh --destructive           # include container stop/restart scenarios
```

Only use `--destructive` when no human tester is online
(`docker compose -p surf-player-test exec -T velocity rcon-cli "glist all"`): it restarts Paper,
the microservice, Redis or RabbitMQ.

Isolated scenarios run on `--parallel N` bot groups (default 6), global ones afterwards with one
group. Some rows wait for AFK detection (3 min), minute changes or expired callbacks (65 s), so run
the full suite in the background and filter while iterating.

## Read the report

`test/player-tests/build/report.json`: `id`, `feature`, `status`, `note` (German), `durationMs`.

- `ok`: behaviour observed as specified; the note says what the bot saw.
- `bad`: expectation not met; the note contains the expected text and what the bot received.
- `odd`: works, but something looks wrong (wrong prefix, missing notification, …).
- `skip`: a bot cannot observe it (visual only, client mod, missing external service) or the row
  is obsolete after a product decision; the note says why.

Turn a `bad` into a fix:

1. Reproduce by hand: `cd test/player-tests && ../../gradlew -q installDist && MSYS_NO_PATHCONV=1
   build/install/player-tests/bin/player-tests --probe DevPlayer1 paper "/friend add DevAdmin"`
   (prints every message with its JSON, open menus, tab list, teams).
2. Decide whether the plugin or the scenario is wrong: grep the message text in
   `core/core-client`, `runtime/*`; check `docs/command-conventions.md` and
   `docs/command-migration.md` for renamed commands.
3. Fix the plugin code, then `test/player-tests/run.sh --id <row>` (rebuilds and redeploys the
   jars automatically) until it is `ok`. Then `--feature <feature>` to catch side effects.

## Add a scenario

- File per feature in `test/player-tests/src/main/kotlin/dev/slne/surf/player/tests/scenarios/`,
  register it in `All.kt`. One `scenario("<row-id>", "<title>") { ... }` per behaviour.
- Never assert on things unit tests cover (pure logic, parsing, formatting helpers); assert on
  what a client receives.
- Use the DSL: `on(PAPER, p1)`, `val m = p1.run("cmd")`, `p1.expect("text", since = m,
  prefix = Prefix.Error)`, `p1.expectNone(...)`, `p1.clickIn(message, "[✔]")`,
  `p1.suggestions("/friend accept ")`, `openMenu(p1, "settings", "Einstellungen")`,
  `p1.expectCommandUnavailable("surfplayer")`, `note("…")` for the German result.
- Set up and clean up in the scenario: `resetFriends()`, `resetClans()`, `resetIgnores(bot)`,
  `setSetting(bot, key, value)`, `withConfig(PAPER, "chat/config.yml", { Yaml.set(it, "a.b", "x") },
  { reloadOn(PAPER, "chat") }) { … }`, `withPermission(bot, node) { … }`. Touch only Dev* data
  (`DEV_UUIDS_SQL`).
- Accounts: `admin` = DevAdmin (plan: NotAmmo), `p1` = DevPlayer1 (FPNS), `p2` = DevPlayer2. Never
  hard-code names: use `${A}`, `${P1}`, `${P2}` and the clan `${CN}`/`${CT}` (another bot group may
  run the same scenario with DevA3/DevP3a/DevP3b).
- Classify the scenario in `dsl/Isolation.kt`: add its id to `isolated` only if it changes and
  observes nothing but its own group's state; otherwise it stays global (serial).
- Container restarts: call `requireDestructive()` first, then `restartServices(...)`.

## Reset the network

- Plugin configs: edit `docker/test-network/data/<service>/…`, then `/surfplayer <feature> reload`.
- LuckPerms for the bots: `docker compose -p surf-player-test exec -T velocity rcon-cli lpv user
  32dda56d-a3bd-33be-9a61-fbbfbb352812 parent add admin` (DevAdmin), `parent set default` for
  DevPlayer1 `d27f8523-eccb-35a2-817a-fa0c37318758` / DevPlayer2 `edaa3910-4f11-3832-a30a-820752953bb9`;
  full re-provisioning: `docker compose run --rm -e FORCE=1 luckperms-init` (also resets the real
  accounts' permissions).
- Everything: `docker compose down -v` (deletes all data), then start again.

## Common failures

- `CORRUPT … (unzip -t failed)` from fetch-plugins: a parallel Gradle build truncated a jar.
  `./gradlew --stop`, then `./gradlew :<project>:clean :<project>:shadowJar`, rerun.
- Bot cannot join / `Connection refused` on 25566: `docker compose up -d velocity-bots`.
- `chat.disabled.missingProfileKey`: Paper lost `ENFORCE_SECURE_PROFILE=FALSE` (compose.yml).
- Commands missing for DevAdmin: LuckPerms not provisioned for the offline UUIDs (see above).
- Many `bad` with "erhalten: nichts" in one feature: check that the servers are up
  (`docker compose ps`) and the bot is on the expected server (`--probe`).
