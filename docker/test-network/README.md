# surf-player local test network

A Velocity proxy with one Paper and one Minestom (surf-minestom-lobby) backend, both microservices
(surf-core, surf-player), PostgreSQL, Redis and RabbitMQ, all in Docker. For local testing only;
every credential is a generated test value in `.env`.

| Service               | Image / version                                    | Host port                |
|-----------------------|----------------------------------------------------|--------------------------|
| `velocity`            | itzg/mc-proxy, Velocity 4.3.1                      | `25565` (all interfaces) |
| `velocity-bots`       | second Velocity 4.3.1, **offline mode**, for the test bots | `127.0.0.1:25566` |
| `paper`               | itzg/minecraft-server, Paper 26.3 build 259        | none (behind the proxy)  |
| `minestom`            | surf-minestom-lobby 2.1.0 (MC 26.3), temurin 25    | none (behind the proxy)  |
| `player-microservice` | local `surf-player-microservice` jar               | none                     |
| `core-microservice`   | surf-core-microservice 2.5.6                       | none                     |
| `postgres`            | postgres:17-alpine, database `surf` + `luckperms`  | `127.0.0.1:5432`         |
| `redis`               | redis:7.4-alpine (password)                        | `127.0.0.1:6379`         |
| `rabbitmq`            | rabbitmq:4.1-management-alpine, user `surf`        | `127.0.0.1:5672`, UI `http://localhost:15672` |
| `luckperms-init`      | one-shot, provisions LuckPerms, then exits         | none                     |

## Start

```sh
cd docker/test-network
cp .env.example .env          # then replace every change-me (openssl rand -hex 16)
../../gradlew -p ../.. :surf-player-paper:shadowJar :surf-player-minestom:shadowJar \
    :surf-player-velocity:shadowJar :surf-player-microservice:shadowJar \
    :surf-player-placeholder-expansion:shadowJar
./fetch-plugins.sh            # downloads pinned jars into plugins/, copies the local jars
docker compose up -d
docker compose logs -f paper minestom velocity player-microservice
```

`fetch-plugins.sh` checks every jar with `unzip -t` and stops on a corrupt one (a parallel
Gradle build can leave truncated jars in `build/libs`). After changing surf-player code: rebuild,
run `./fetch-plugins.sh` again, then `docker compose restart paper minestom velocity player-microservice`.

## Stop / reset

```sh
docker compose stop           # keep all data
docker compose down           # remove containers, keep volumes
docker compose down -v        # remove everything: databases, worlds, LuckPerms, libraries
```

## Connect

Minecraft 26.3 client, server `localhost:25565`. The proxy runs in online mode (real Mojang
accounts) with modern forwarding; Paper and Minestom are offline-mode backends reachable only
through the proxy. Players land on `lobby` (Minestom); `/server paper` switches to Paper.

## Permissions (LuckPerms)

Paper (LuckPerms-Bukkit), Velocity (LuckPerms-Velocity) and Minestom (the surf-api LuckPerms
feature) share one storage, the PostgreSQL database `luckperms`, and propagate changes through
Redis messaging. On the first start `luckperms-init` (`luckperms/init.sh`) creates group `admin`
(weight 100, `*`), grants `default` every player command (`surf.player.command.*`), adds
**NotAmmo** to `admin` and leaves **FPNS** in `default` only, addressing both by their Mojang UUID. It runs once per volume; rerun with
`docker compose run --rm -e FORCE=1 luckperms-init`.

The test bots get the same treatment by their **offline UUIDs** (`UUID.nameUUIDFromBytes("OfflinePlayer:<name>")`):
**DevAdmin** (`32dda56d-a3bd-33be-9a61-fbbfbb352812`) → `admin`, **DevPlayer1**
(`d27f8523-eccb-35a2-817a-fa0c37318758`) and **DevPlayer2** (`edaa3910-4f11-3832-a30a-820752953bb9`) →
`default` only. On a network provisioned before the bots existed, add them without touching the
real accounts:

```sh
docker compose exec velocity rcon-cli lpv user 32dda56d-a3bd-33be-9a61-fbbfbb352812 parent add admin
docker compose exec velocity rcon-cli lpv user d27f8523-eccb-35a2-817a-fa0c37318758 parent set default
docker compose exec velocity rcon-cli lpv user edaa3910-4f11-3832-a30a-820752953bb9 parent set default
```

## Console commands

```sh
docker compose exec paper rcon-cli <command>       # Paper (RCON)
docker compose exec velocity rcon-cli <command>    # Velocity (RCON via Velocircon), LuckPerms is lpv
docker compose exec minestom console <command>     # Minestom: output appears in the logs
docker compose logs --tail 30 minestom

# examples (FPNS = 5cdf1a4c-54c2-457b-8ee5-61fdb209c453, NotAmmo = 5c63e51b-82b1-4222-af0f-66a4c31e36ad)
docker compose exec velocity rcon-cli lpv user FPNS permission set surf.player.example true
docker compose exec velocity rcon-cli lpv user FPNS permission unset surf.player.example
docker compose exec velocity rcon-cli lpv user FPNS info
docker compose exec paper rcon-cli lp user FPNS permission set surf.player.example true
docker compose exec minestom console lp user NotAmmo info
```

Velocity's RCON returns LuckPerms output; Paper answers RCON asynchronously, so `lp` there
applies changes but prints nothing (use `lpv` on Velocity or the Minestom console to read). Changes
made on any server reach the others through the shared storage and Redis. LuckPerms only knows a
name after the player has joined once; before that use the UUID instead of the name.

## Bots

Headless test clients for the automated player tests live in [`test/player-tests`](../../test/player-tests)
(see its README and the Claude Code skill `.claude/skills/automated-player-tests`). They connect
to `velocity-bots`, a second proxy in **offline mode** on `127.0.0.1:25566` with the same
backends, forwarding secret and plugins as `velocity` (its surf-core identity is
`velocity-bots`, `config/velocity-bots/`). The real proxy on 25565 stays in online mode.

- Accounts: **DevAdmin** (admin, all permissions, stands in for NotAmmo), **DevPlayer1** (normal
  player, stands in for FPNS), **DevPlayer2** (normal player, the third account).
- Paper runs with `enforce-secure-profile=false` (`ENFORCE_SECURE_PROFILE`): offline bots have no
  Mojang chat-signing key and could not chat otherwise. Real clients still sign their messages.
  It also sets `allow-flight=true` (bots hover without physics and would be kicked for flying) and
  `max-players=100` (six parallel bot groups plus testers). `velocity-bots` has
  `login-ratelimit = 0` because all bots connect from one address.
- Parallel bot groups 2..6 are `DevA<n>` (admin) and `DevP<n>a`/`DevP<n>b`; the runner and
  `luckperms/init.sh` provision them by offline UUID.
- Run all tests: `test/player-tests/run.sh`; one feature: `test/player-tests/run.sh --feature chat`.

## Editing plugin configs

The surf-player data folders are bind-mounted to the host, so configs can be edited without
container access and reloaded in game or on the console with `/surfplayer <feature> reload`:

| Host folder (gitignored)                           | Container path                                   |
|----------------------------------------------------|--------------------------------------------------|
| `data/paper/surf-player-paper/`                    | `paper:/data/plugins/surf-player-paper`          |
| `data/minestom/surf-player-minestom/`              | `minestom:/server/plugins/surf-player-minestom`  |
| `data/velocity/surf-player-velocity/`              | `velocity` and `velocity-bots`: `/server/plugins/surf-player-velocity` |
| `data/player-microservice/config/`                 | `player-microservice:/app/config`                |

Missing folders are created by Docker and filled with the default configs on the next start
(`rm -rf data/paper` and `docker compose restart paper` regenerates Paper's). On Velocity use
`/surfplayervelocity <feature> reload`; the microservice reads its config only at start
(`docker compose restart player-microservice`).

## Friend request cooldowns

The player microservice runs with short cooldowns so the escalation can be tested in minutes
(`compose.yml`, service `player-microservice`; production defaults in parentheses):

| Variable                                               | Test network | Default |
|--------------------------------------------------------|--------------|---------|
| `SURF_PLAYER_FRIEND_REQUEST_DECLINE_COOLDOWN`           | `2m`         | `24h`   |
| `SURF_PLAYER_FRIEND_REQUEST_WITHDRAW_COOLDOWN`          | `1m`         | `30m`   |
| `SURF_PLAYER_FRIEND_REQUEST_COOLDOWN_ESCALATION_WINDOW` | `10m`        | `30d`   |
| `SURF_PLAYER_FRIEND_REQUEST_MAX_COOLDOWN`               | `8m`         | `7d`    |

Each repeated decline/withdrawal inside the window doubles the cooldown up to the maximum.
Change a value and run `docker compose up -d player-microservice`.

## Where the configuration lives

| Path                                   | What                                                                       |
|----------------------------------------|----------------------------------------------------------------------------|
| `compose.yml`                          | services, env vars for surf-rabbitmq/-redis/-database and LuckPerms        |
| `.env` / `.env.example`                | passwords, ports, Paper/Velocity versions                                  |
| `fetch-plugins.sh`                     | pinned versions and sources of every downloaded jar                        |
| `config/velocity/`                     | synced into the proxy's `/server`: `velocity.toml`, surf-core server name  |
| `config/velocity-bots/`                | same for the offline bot proxy (`online-mode = false`, server name `velocity-bots`) |
| `data/`                                | bind-mounted surf-player data folders (gitignored), see "Editing plugin configs" |
| `config/paper/`                        | synced into Paper's `/data`: `config/paper-global.yml` (Velocity secret), surf-core server name |
| `minestom/config.yml`                  | lobby config template (port, Velocity secret, PostgreSQL)                  |
| `minestom/plugin-config/`              | copied over the lobby's `plugins/` (surf-core server name)                 |
| `minestom/worlds/lobby.polar`          | flat lobby world, imported once (made by `minestom/CreateLobbyWorld.java`) |
| `postgres/init.sql`                    | schema `surf-core`, schema `surf_minestom_lobby`, database `luckperms`     |
| `luckperms/init.sh`                    | LuckPerms provisioning                                                     |

surf-rabbitmq, surf-redis and surf-database (microservices) are configured only through
environment variables (`SURF_RABBITMQ_*`, `SURF_REDIS_*`, `SURF_DATABASE_*`); their generated
yml files keep `__default__`. Runtime data lives in Docker volumes (`surf-player-test_*`).

## Notes

- `JAVA_TOOL_OPTIONS` disables Netty's native transports in every JVM: Docker Desktop's WSL2
  kernel fails `io_uring_enter` (EBADFD), and surf-rabbitmq/surf-redis pick io_uring when Netty
  reports it available. Their relocated Netty copies need their own `...shaded.io.netty...` flags.
- surf-punish-paper is 1.2.5, the newest on the public reposilite; newer releases are only in
  the private surf-punish repository. No surf-punish microservice runs.
- The player microservice logs `Discord bot token is not configured!` (social connections need a
  real bot token in `config/social.properties` of the `player-microservice-data` volume).
