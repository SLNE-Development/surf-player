-- Runs once, when the postgres volume is created (docker compose down -v resets it).
--
-- surf-player-microservice creates its own schemas (surf-chat, surf-clan, surf-friends,
-- surf-shrieker, surf-settings, surf-playtime) and public.account itself. In production
-- public.account belongs to the website; here the microservice's SchemaUtils.create makes it.
--
-- surf-core-microservice connects with the search path "surf-core" and creates unqualified
-- tables, so its schema has to exist up front. surf-player's clan module reads
-- "surf-core".surf_players from it.
CREATE SCHEMA IF NOT EXISTS "surf-core";

-- Schema of the Minestom lobby server (surf-minestom-lobby, database.schema in its config.yml).
CREATE SCHEMA IF NOT EXISTS surf_minestom_lobby;

-- Shared LuckPerms storage of Paper, Velocity and Minestom.
CREATE DATABASE luckperms OWNER surf;
