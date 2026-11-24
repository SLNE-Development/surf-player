package dev.slne.surf.player.tests.scenarios

import dev.slne.surf.player.tests.bot.Bot
import dev.slne.surf.player.tests.bot.Text
import dev.slne.surf.player.tests.dsl.Ctx
import java.nio.charset.StandardCharsets
import java.util.*

/** Shared set-up and clean-up of the Dev* test accounts. Only their data is ever touched. */

const val PAPER = "paper"
const val LOBBY = "lobby"
/** The names of this scenario's bot group (clean-up never touches other groups). */
val Ctx.DEV_NAMES: List<String> get() = listOf(A, P1, P2)

fun offlineUuid(name: String): UUID = UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray(StandardCharsets.UTF_8))

val Ctx.DEV_UUIDS_SQL: String get() = DEV_NAMES.joinToString(",") { "'${offlineUuid(it)}'" }

fun dataDir(server: String) = when (server) {
    PAPER -> "/data/plugins/surf-player-paper"
    LOBBY, "minestom" -> "/server/plugins/surf-player-minestom"
    "velocity" -> "/server/plugins/surf-player-velocity"
    else -> error(server)
}

fun service(server: String) = if (server == LOBBY) "minestom" else server

/**
 * Removes friendships, requests and cooldowns of the group's accounts. Friendships and requests
 * live in Redis sync lists filled from the database only at server start, so they are ended
 * through the commands; the database rows are removed afterwards as a fallback.
 */
fun Ctx.resetFriends() {
    val name = { uuid: String -> DEV_NAMES.firstOrNull { offlineUuid(it).toString() == uuid } }
    val friendships = net.sql(
        """SELECT player_uuid, friend_uuid FROM "surf-friends".friendships WHERE player_uuid IN ($DEV_UUIDS_SQL)"""
    )
    for ((a, b) in friendships.map { it[0] to it[1] }) {
        val owner = name(a) ?: continue
        val friend = name(b) ?: continue
        val bot = botByName(owner)
        val m = bot.run("friend remove $friend")
        val conf = bot.await(4000, m) { Text.has(it.plain, "wirklich beenden") } ?: continue
        conf.clicks.filter { Text.has(it.text, "Bestätigen") }.minByOrNull { it.text.length }?.let { bot.click(it.event) }
        bot.await(3000, m) { Text.has(it.plain, "als Freund entfernt") || Text.has(it.plain, "nicht mehr") }
    }
    val requests = net.sql(
        """SELECT sender_uuid, target_uuid FROM "surf-friends".friend_requests WHERE sender_uuid IN ($DEV_UUIDS_SQL) OR target_uuid IN ($DEV_UUIDS_SQL)"""
    )
    for ((s, t) in requests.map { it[0] to it[1] }) {
        val sender = name(s); val target = name(t)
        if (sender != null && target != null) {
            val b = botByName(sender); val m = b.run("friend revoke $target"); b.firstMessage(m, 3000)
        }
    }
    // Only the cooldowns are deleted directly (the microservice reads them from the database);
    // deleting friendships/requests in SQL would leave the Redis lists of the servers stale.
    net.sql("""DELETE FROM "surf-friends".friend_request_cooldowns WHERE sender_uuid IN ($DEV_UUIDS_SQL) OR receiver_uuid IN ($DEV_UUIDS_SQL)""")
}

/** Un-ignores every Dev* pair through the command (the ignore list is cached on the servers). */
fun Ctx.resetIgnores(vararg who: Bot) {
    val rows = net.sql("""SELECT user_id, target_uuid FROM "surf-chat".chat_ignorelist_entries WHERE user_id IN ($DEV_UUIDS_SQL)""")
    if (rows.isEmpty()) return
    for (row in rows) {
        val owner = DEV_NAMES.firstOrNull { offlineUuid(it).toString() == row[0] } ?: continue
        val target = DEV_NAMES.firstOrNull { offlineUuid(it).toString() == row[1] }
        val bot = who.firstOrNull { it.name == owner } ?: botByName(owner)
        if (target != null) bot.cmd("ignore remove $target")
    }
    sleep(800)
    net.sql("""DELETE FROM "surf-chat".chat_ignorelist_entries WHERE user_id IN ($DEV_UUIDS_SQL)""")
}

fun Ctx.botByName(name: String): Bot = when (name) {
    A -> DevAdmin
    P1 -> DevPlayer1
    else -> DevPlayer2
}

/** Deletes every clan a Dev* account is in and their open clan invites. */
fun Ctx.resetClans() {
    val tags = net.sql(
        """SELECT DISTINCT c.tag FROM "surf-clan".clan_clans c LEFT JOIN "surf-clan".clan_members m ON m.clan_id = c.id
           WHERE m.uuid IN ($DEV_UUIDS_SQL) OR c.created_by IN ($DEV_UUIDS_SQL)"""
    ).map { it[0] }
    for (tag in tags) net.console(PAPER, "surfplayer clan delete $tag", waitMs = 1500)
    net.sql("""DELETE FROM "surf-clan".clan_invites WHERE invited IN ($DEV_UUIDS_SQL) OR invited_by IN ($DEV_UUIDS_SQL)""")
    net.sql("""UPDATE "surf-clan".clan_players SET accepts_clan_invites = true WHERE uuid IN ($DEV_UUIDS_SQL)""")
    if (tags.isNotEmpty()) {
        net.console(PAPER, "surfplayer clan invalidatecaches", waitMs = 1500)
    }
}

/** Creates clan [name]/[tag] owned by [owner] (after removing old Dev* clans). */
fun Ctx.createClan(owner: Bot, name: String = CN, tag: String = CT) {
    resetClans()
    val m = owner.run("clan create $name $tag")
    owner.expect("erfolgreich erstellt", since = m, timeout = 8000)
}

/** Puts [member] into [owner]'s clan via invite + accept. */
fun Ctx.joinClan(owner: Bot, member: Bot, name: String = CN, tag: String = CT) {
    val m = member.mark()
    owner.cmd("clan invite ${member.name}")
    member.expect("eingeladen", since = m, timeout = 8000)
    val m2 = member.run("clan accept $tag")
    member.expect("beigetreten", since = m2, timeout = 8000)
}

/** Sets a boolean setting through the command and waits for the confirmation. */
fun Ctx.setSetting(bot: Bot, key: String, value: Boolean) {
    val m = bot.run("settings toggle $key ${if (value) "an" else "aus"}")
    bot.firstMessage(m, 5000)
}

/** Restores the defaults of the settings the scenarios change. */
fun Ctx.resetSettings(bot: Bot) {
    val changed = net.sql(
        """SELECT setting_name, value FROM "surf-settings".settings_entries WHERE player_uuid = '${bot.uuid}'"""
    )
    val defaults = mapOf(
        "chat:pings" to "true", "chat:death-messages" to "true", "chat:direct-messages" to "true",
        "chat:connection-messages" to "true", "lobby:scroll-sound" to "false", "clan:invites" to "true",
        "clan:chat-messages" to "true", "friend:request-notifications" to "true", "friend:notifications" to "true",
        "friend:sounds" to "true", "nametag:show-nametags" to "true", "scoreboard:show-scoreboard" to "true",
        "queue:auto-queue-after-reconnect" to "true",
    )
    for ((key, value) in changed) {
        val def = defaults[key] ?: continue
        if (value != def) setSetting(bot, key, def.toBoolean())
    }
}

/**
 * Runs [body] with a modified plugin config file and restores the original afterwards; [reload]
 * applies the file (e.g. `/surfplayer chat reload` on the console).
 */
fun Ctx.withConfig(server: String, file: String, transform: (String) -> String, reload: () -> Unit, body: () -> Unit) {
    val path = "${dataDir(server)}/$file"
    val original = net.cat(service(server), path)
    check(original.isNotBlank()) { "config $path is empty or missing" }
    val changed = transform(original)
    check(changed != original) { "config change of $path had no effect" }
    net.write(service(server), path, changed)
    try {
        reload()
        body()
    } finally {
        net.write(service(server), path, original)
        reload()
    }
}

fun Ctx.reloadOn(server: String, feature: String) {
    net.console(server, "surfplayer $feature reload", waitMs = 2000)
}

fun Ctx.lp(command: String) = net.velocity("lpv $command")

/** Grants a permission to a Dev* account for the duration of [body]. */
fun Ctx.withPermission(bot: Bot, permission: String, value: Boolean = true, body: () -> Unit) {
    lp("user ${bot.uuid} permission set $permission $value")
    sleep(2500)
    try {
        body()
    } finally {
        lp("user ${bot.uuid} permission unset $permission")
        sleep(1500)
    }
}

fun Ctx.vanish(bot: Bot, on: Boolean) {
    on(PAPER, bot)
    if (isVanished(bot) != on) {
        val m = bot.run("vanish")
        bot.firstMessage(m, 5000)
        sleep(1500)
    }
}

/** Restarts containers with all bots disconnected and waits until the network accepts bots again. */
fun Ctx.restartServices(vararg services: String) {
    quitAll()
    net.restart(*services)
    sleep(5000)
    net.waitReady(400)
    sleep(10000)
}

fun Ctx.restartPaper() = restartServices("paper")

/** Vanished players get the action bar "Du bist für andere Spieler unsichtbar." every second. */
fun Ctx.isVanished(bot: Bot): Boolean {
    val m = bot.mark()
    return bot.await(2500, m) {
        it.kind == dev.slne.surf.player.tests.bot.Received.Kind.ACTIONBAR && it.plain.contains("unsichtbar")
    } != null
}

/** Unique text so a message of this scenario is not confused with an earlier one. */
fun uniq(prefix: String = "t") = prefix + (100000..999999).random()
