package dev.slne.surf.player.tests.scenarios

import dev.slne.surf.player.tests.bot.Bot
import dev.slne.surf.player.tests.dsl.Ctx
import dev.slne.surf.player.tests.dsl.Prefix
import dev.slne.surf.player.tests.dsl.notAutomatable
import dev.slne.surf.player.tests.dsl.scenario

/** Runs [command] as [bot] and fails unless the first reply contains [expected]. */
private fun Ctx.reply(bot: Bot, command: String, expected: String, prefix: Prefix = Prefix.Any): String {
    val m = bot.run(command)
    val r = bot.await(7000, m) { (it.kind == dev.slne.surf.player.tests.bot.Received.Kind.SYSTEM) && dev.slne.surf.player.tests.bot.Text.has(it.plain, expected) }
    if (r == null) {
        // Clan member arguments resolve names through PlayerLookupService (Mojang), which knows no
        // offline-mode UUIDs. Retry with the UUID to still check the command's own logic.
        val unknown = bot.messagesSince(m).firstOrNull { it.plain.contains("ist kein Mitglied deines Clans") }
        if (unknown != null) {
            val byUuid = listOf(admin, p1, p2).fold(command) { c, b -> c.replace(" ${b.name}", " ${b.uuid}") }
            if (byUuid != command) {
                val m2 = bot.run(byUuid)
                val r2 = bot.await(7000, m2) { dev.slne.surf.player.tests.bot.Text.has(it.plain, expected) }
                odd(
                    "Name nicht als Clanmitglied erkannt („${unknown.plain.trim()}“, Offline-UUID ohne Mojang-Namen); " +
                        "mit UUID: ${r2?.plain?.trim()?.let { "„$it“ wie erwartet" } ?: "ebenfalls nicht erwartet: ${observed(bot, m2)}"}"
                )
            }
        }
        bad("${bot.name} /$command: erwartet „$expected“, erhalten: ${observed(bot, m)}")
    }
    if (prefix != Prefix.Any && !hasPrefix(r, prefix)) odd("${bot.name} /$command: „${r.plain.trim()}“ ohne $prefix-Präfix")
    return r.plain.trim().replace("\n", " ⏎ ")
}

/** Promotions in the set-up go by UUID: names of offline bots are not resolved (see [reply]). */
private fun Ctx.clanWithMember(role: String? = null) {
    createClan(admin)
    joinClan(admin, p1)
    when (role) {
        "OFFICER" -> { admin.cmd("clan promote ${p1.uuid}"); sleep(1500) }
        "LEADER" -> { admin.cmd("clan promote ${p1.uuid}"); sleep(1500); admin.cmd("clan promote ${p1.uuid}"); sleep(1500) }
    }
}

private val FORBIDDEN = listOf(
    "clan-09" to "nazi", "clan-10" to "NIG", "clan-11" to "HOMO", "clan-12" to "TRN", "clan-13" to "HOE",
    "clan-14" to "JUDE", "clan-15" to "ISIS", "clan-16" to "911", "clan-17" to "MAGA", "clan-18" to "NOOB",
    "clan-19" to "FAT", "clan-20" to "WEED", "clan-21" to "PORN", "clan-22" to "GUN", "clan-23" to "KYS",
    "clan-24" to "KACK", "clan-25" to "DDOS", "clan-26" to "GORE", "clan-27" to "TEAM", "clan-28" to "AFD",
    "clan-29" to "TARD", "clan-30" to "CHAB", "clan-31" to "ZOOP", "clan-32" to "INCE", "clan-33" to "XENO",
    "clan-34" to "MOB", "clan-35" to "ZETA",
)

/** Clan feature: ${A} owns "${CN}" (${CT}), ${P1} joins as member. */
fun registerClan() {
    scenario("clan-01", "/clan create") {
        on(PAPER, admin)
        resetClans()
        val m = admin.run("clan create ${CN} ${CT.lowercase()}")
        val r = admin.expect("Der Clan ${CN} wurde erfolgreich erstellt", since = m, prefix = Prefix.Success)
        check(r.hovers.isNotEmpty()) { "Kein Hover am Clannamen" }
        val tag = net.sql("""SELECT name, tag FROM "surf-clan".clan_clans WHERE created_by = '${admin.uuid}'""")
        check(tag.firstOrNull()?.get(1) == "${CT}") { "DB: $tag" }
        note("„${r.plain.trim()}“, Hover vorhanden, DB: ${tag.first()}")
        resetClans()
    }

    scenario("clan-02", "create: Name zu kurz") { on(PAPER, p1); resetClans(); note(reply(p1, "clan create ab ABC", "zwischen 3 und 16 Zeichen", Prefix.Error)) }
    scenario("clan-03", "create: Name zu lang") { on(PAPER, p1); resetClans(); note(reply(p1, "clan create Abcdefghijklmnopq ABC", "zwischen 3 und 16 Zeichen", Prefix.Error)) }
    scenario("clan-04", "create: Tag zu kurz/lang") {
        on(PAPER, p1); resetClans()
        note(reply(p1, "clan create Fclan AB", "zwischen 3 und 4 Zeichen"))
        note(reply(p1, "clan create Fclan ABCDE", "zwischen 3 und 4 Zeichen"))
    }
    scenario("clan-05", "create: ungültige Zeichen") {
        on(PAPER, p1); resetClans()
        note(reply(p1, "clan create Fclan A_B", "ungültige Zeichen"))
    }
    scenario("clan-06", "create: bereits im Clan") {
        on(PAPER, admin); createClan(admin)
        note(reply(admin, "clan create Zweit ZWT", "bereits in einem Clan", Prefix.Error)); resetClans()
    }
    scenario("clan-07", "create: Name existiert") {
        on(PAPER, admin, p1); createClan(admin)
        note(reply(p1, "clan create ${CN} XYZ1", "Ein Clan mit diesem Namen", Prefix.Error)); resetClans()
    }
    scenario("clan-08", "create: Tag existiert (case-insensitive)") {
        on(PAPER, admin, p1); createClan(admin)
        note(reply(p1, "clan create ${CN2} ${CT.lowercase()}", "Tag existiert bereits", Prefix.Error)); resetClans()
    }
    for ((id, tag) in FORBIDDEN) scenario(id, "Verbotener Tag $tag") {
        on(PAPER, p1); resetClans()
        note(reply(p1, "clan create Fclan $tag", "Der Clan-Tag ist nicht erlaubt", Prefix.Error))
        resetClans()
    }

    scenario("clan-36", "/clan info eigener Clan") {
        on(PAPER, admin); createClan(admin)
        val m = admin.run("clan info")
        admin.expect("Informationen", since = m)
        sleep(800)
        val all = admin.messagesSince(m).joinToString(" ⏎ ") { it.plain.trim() }
        val missing = listOf("Name", "Tag", "Anführer", "Offiziere", "Mitglieder", "Erstellt").filter { !dev.slne.surf.player.tests.bot.Text.has(all, it) }
        check(missing.isEmpty()) { "Fehlende Zeilen $missing in „${all.take(400)}“" }
        check(!all.contains("Discord")) { "Discord-Zeile trotz <30 aktiven Mitgliedern" }
        note("„${all.take(350)}“")
        resetClans()
    }
    scenario("clan-37", "/clan info <Tag>") {
        on(PAPER, admin, p1); createClan(admin)
        val sugg = p1.suggestions("/clan info ")
        note(reply(p1, "clan info ${CT}", "${CN}"))
        note("Tab-Vorschläge: $sugg")
        if ("${CT}" !in sugg) odd("Tab schlägt ${CT} nicht vor: $sugg; ${notes.first()}")
        resetClans()
    }
    scenario("clan-38", "/clan info unbekannter Tag") {
        on(PAPER, p1)
        note(reply(p1, "clan info XXXX", "Es gibt keinen Clan mit dem Tag 'XXXX'.", Prefix.Error))
    }
    scenario("clan-39", "/clan info ohne Clan") { on(PAPER, p1); resetClans(); note(reply(p1, "clan info", "Du bist in keinem Clan")) }
    scenario("clan-40", "/clan whois im Clan") {
        on(PAPER, admin, p1); createClan(admin)
        val m = p1.run("clan whois ${A}")
        val r = p1.expect("${A} ist im Clan ${CN}", since = m)
        val c = r.clicks.firstOrNull { it.text.contains("${CN}") } ?: bad("Clanname nicht klickbar")
        check(c.value.contains("clan info")) { "Klick: ${c.value}" }
        check(r.hovers.any { it.second.contains("Informationen über den Clan") }) { "Hover: ${r.hovers}" }
        note("„${r.plain.trim()}“, Klick ${c.value}")
        resetClans()
    }
    scenario("clan-41", "/clan whois ohne Clan") { on(PAPER, admin); resetClans(); note(reply(admin, "clan whois ${P1}", "${P1} ist in keinem Clan", Prefix.Info)) }
    scenario("clan-42", "/clan whois unbekannt") {
        on(PAPER, p1)
        val m = p1.run("clan whois NichtExistent123"); val r = p1.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("nicht gefunden")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim()}“")
    }
    scenario("clan-43", "/clan invite") {
        on(PAPER, admin, p1); createClan(admin)
        val m1 = p1.mark()
        val out = reply(admin, "clan invite ${P1}", "Du hast ${P1} in den Clan ${CN} eingeladen", Prefix.Success)
        val inc = p1.expect("Du wurdest von ${A} in den Clan ${CN} eingeladen", since = m1)
        check(inc.plain.contains("Annehmen") && inc.plain.contains("Ablehnen")) { "Buttons: „${inc.plain}“" }
        val db = net.sql("""SELECT count(*) FROM "surf-clan".clan_invites WHERE invited = '${p1.uuid}'""")
        check(db[0][0] == "1") { "clan_invites: ${db[0][0]}" }
        note("„$out“ / „${inc.plain.trim()}“; DB-Einladung vorhanden")
        resetClans()
    }
    scenario("clan-44", "invite serverübergreifend") {
        on(PAPER, admin); on(LOBBY, p1); createClan(admin)
        val m1 = p1.mark(); admin.cmd("clan invite ${P1}")
        p1.expect("eingeladen", since = m1)
        note("${P1} erhält die Einladung in der Lobby"); resetClans()
    }
    scenario("clan-45", "invite: Spieler nicht gefunden") {
        on(PAPER, admin); createClan(admin)
        val m = admin.run("clan invite NichtExistent123"); val r = admin.firstMessage(m) ?: bad("Keine Antwort")
        check(r.plain.contains("nicht gefunden")) { "„${r.plain.trim()}“" }
        note("„${r.plain.trim()}“"); resetClans()
    }
    scenario("clan-46", "invite: nicht im Clan") { on(PAPER, p1); resetClans(); note(reply(p1, "clan invite ${A}", "Du bist in keinem Clan")) }
    scenario("clan-47", "invite: keine Clan-Berechtigung (Mitglied)") {
        on(PAPER, admin, p1, p2); clanWithMember()
        note(reply(p1, "clan invite ${P2}", "keine Berechtigung, Spieler in den Clan einzuladen")); resetClans()
    }
    scenario("clan-48", "invite: bereits im Clan") {
        on(PAPER, admin, p1, p2); clanWithMember()
        val m = p2.run("clan create ${CN2} ${CT2}"); p2.expect("erfolgreich erstellt", since = m)
        note(reply(admin, "clan invite ${P2}", "bereits in einem Clan")); resetClans()
    }
    scenario("clan-49", "invite: bereits eingeladen") {
        on(PAPER, admin, p1); createClan(admin)
        admin.cmd("clan invite ${P1}"); sleep(1500)
        note(reply(admin, "clan invite ${P1}", "bereits eingeladen")); resetClans()
    }
    scenario("clan-50", "invite: Einladungen deaktiviert") {
        on(PAPER, admin, p1); createClan(admin)
        setSetting(p1, "clan:invites", false)
        try {
            note("online: " + reply(admin, "clan invite ${P1}", "Der Spieler nimmt keine Einladungen an"))
            quit(p1)
            note("offline: " + reply(admin, "clan invite ${P1}", "Der Spieler nimmt keine Einladungen an"))
        } finally { setSetting(p1, "clan:invites", true); resetClans() }
    }
    scenario("clan-51", "invite: Offline-Spieler") {
        on(PAPER, admin); createClan(admin); quit(p1)
        val m = admin.run("clan invite ${P1}"); val r = admin.firstMessage(m) ?: bad("Keine Antwort")
        val db = net.sql("""SELECT count(*) FROM "surf-clan".clan_invites WHERE invited = '${p1.uuid}'""")
        check(db[0][0] == "1") { "Keine Einladung in DB; Antwort „${r.plain.trim()}“" }
        note("„${r.plain.trim()}“; Einladung angelegt"); resetClans()
    }
    scenario("clan-52", "[Annehmen]-Button") {
        on(PAPER, admin, p1); createClan(admin)
        val m1 = p1.mark(); admin.cmd("clan invite ${P1}")
        val inv = p1.expect("eingeladen", since = m1)
        val m2 = p1.mark(); val c = p1.clickIn(inv, "Annehmen")
        p1.expect("beigetreten", since = m2)
        note("Klick ${c.value} → beigetreten"); resetClans()
        if (!c.value.contains("clan accept $CT")) bad("Button führt „${c.value}“ aus statt /clan accept $CT")
    }
    scenario("clan-53", "/clan accept") {
        on(PAPER, admin, p1); createClan(admin)
        admin.cmd("clan invite ${P1}"); sleep(1500)
        val ma = admin.mark()
        val a = reply(p1, "clan accept ${CT}", "Du bist dem Clan ${CN} beigetreten", Prefix.Success)
        val b = admin.expect("${P1} ist dem Clan beigetreten", since = ma)
        note("„$a“ / „${b.plain.trim()}“"); resetClans()
    }
    scenario("clan-54", "accept ohne Einladung") {
        on(PAPER, p1); resetClans()
        note(reply(p1, "clan accept XXXX", "Du hast keine offene Einladung vom Clan 'XXXX'."))
    }
    scenario("clan-55", "accept bereits im Clan") {
        on(PAPER, admin, p1, p2); createClan(admin)
        val m = p2.run("clan create ${CN2} ${CT2}"); p2.expect("erfolgreich erstellt", since = m)
        joinClan(p2, p1, CN2, CT2)
        admin.cmd("clan invite ${P1}"); sleep(1500)
        note(reply(p1, "clan accept ${CT}", "bereits in einem Clan")); resetClans()
    }
    scenario("clan-56", "[Ablehnen]-Button") {
        on(PAPER, admin, p1); createClan(admin)
        val m1 = p1.mark(); admin.cmd("clan invite ${P1}")
        val inv = p1.expect("eingeladen", since = m1)
        val m2 = p1.mark(); val c = p1.clickIn(inv, "Ablehnen")
        p1.expect("abgelehnt", since = m2)
        note("Klick ${c.value} → abgelehnt"); resetClans()
    }
    scenario("clan-57", "/clan deny") {
        on(PAPER, admin); on(LOBBY, p1); createClan(admin)
        admin.cmd("clan invite ${P1}"); sleep(1500)
        val ma = admin.mark()
        val a = reply(p1, "clan deny ${CT}", "Du hast die Einladung abgelehnt")
        val b = admin.expect("${P1} hat deine Clan-Einladung abgelehnt", since = ma)
        note("„$a“ / ${A} (Paper): „${b.plain.trim()}“"); resetClans()
    }
    scenario("clan-58", "deny ohne Einladung") {
        on(PAPER, admin, p1); createClan(admin)
        note(reply(p1, "clan deny ${CT}", "Du hast keine offene Einladung vom Clan '${CT}'.")); resetClans()
    }
    scenario("clan-59", "/clan promote Mitglied → Offizier") {
        on(PAPER, admin, p1); clanWithMember()
        val m1 = p1.mark(); admin.cmd("clan promote ${p1.uuid}")
        val r = p1.expect("${P1} wurde durch ${A} von Mitglied zu Offizier befördert", since = m1)
        note("„${r.plain.trim()}“"); resetClans()
    }
    scenario("clan-60", "promote Offizier → Anführer") {
        on(PAPER, admin, p1); clanWithMember("OFFICER")
        val m1 = p1.mark(); admin.cmd("clan promote ${p1.uuid}")
        val r = p1.expect("von Offizier zu Anführer befördert", since = m1)
        note("„${r.plain.trim()}“"); resetClans()
    }
    scenario("clan-61", "promote Anführer: höchste Rolle") { on(PAPER, admin, p1); clanWithMember("LEADER"); note(reply(admin, "clan promote ${P1}", "bereits die höchste Rolle")); resetClans() }
    scenario("clan-62", "promote sich selbst") { on(PAPER, admin); createClan(admin); note(reply(admin, "clan promote ${A}", "nicht selbst befördern")); resetClans() }
    scenario("clan-63", "promote ohne Clanrecht") { on(PAPER, admin, p1); clanWithMember(); note(reply(p1, "clan promote ${A}", "keine Berechtigung, diesen Spieler zu befördern")); resetClans() }
    scenario("clan-64", "promote höhere Rolle") { on(PAPER, admin, p1); clanWithMember("LEADER"); note(reply(p1, "clan promote ${A}", "selben oder einen höheren Rang")); resetClans() }
    scenario("clan-65", "promote fremder Spieler") {
        on(PAPER, admin, p2); createClan(admin)
        val m = p2.run("clan create ${CN2} ${CT2}"); p2.expect("erfolgreich erstellt", since = m)
        note(reply(admin, "clan promote ${P2}", "'${P2}' ist kein Mitglied deines Clans.")); resetClans()
    }
    scenario("clan-66", "promote unbekanntes Mitglied") {
        on(PAPER, admin); createClan(admin)
        note(reply(admin, "clan promote NichtExistent123", "'NichtExistent123' ist kein Mitglied deines Clans.")); resetClans()
    }
    scenario("clan-67", "/clan demote Anführer → Offizier") {
        on(PAPER, admin, p1); clanWithMember("LEADER")
        val m1 = p1.mark(); admin.cmd("clan demote ${p1.uuid}")
        val r = p1.expect("von Anführer zu Offizier degradiert", since = m1)
        note("„${r.plain.trim()}“"); resetClans()
    }
    scenario("clan-68", "demote Offizier → Mitglied") {
        on(PAPER, admin, p1); clanWithMember("OFFICER")
        val m1 = p1.mark(); admin.cmd("clan demote ${p1.uuid}")
        val r = p1.expect("von Offizier zu Mitglied degradiert", since = m1)
        note("„${r.plain.trim()}“"); resetClans()
    }
    scenario("clan-69", "demote niedrigste Rolle") { on(PAPER, admin, p1); clanWithMember(); note(reply(admin, "clan demote ${P1}", "bereits den niedrigsten Rang")); resetClans() }
    scenario("clan-70", "demote sich selbst") { on(PAPER, admin); createClan(admin); note(reply(admin, "clan demote ${A}", "nicht selbst degradieren")); resetClans() }
    scenario("clan-71", "demote ohne Clanrecht") { on(PAPER, admin, p1); clanWithMember("OFFICER"); note(reply(p1, "clan demote ${A}", "keine Berechtigung, diesen Spieler zu degradieren")); resetClans() }
    scenario("clan-72", "demote höhere Rolle") { on(PAPER, admin, p1); clanWithMember("LEADER"); note(reply(p1, "clan demote ${A}", "selben oder einen höheren Rang")); resetClans() }
    scenario("clan-73", "/clan kick") {
        on(PAPER, admin, p1); clanWithMember()
        val ma = admin.mark(); admin.cmd("clan kick ${p1.uuid}")
        val r = admin.expect("${P1} wurde von ${A} aus dem Clan entfernt", since = ma)
        sleep(800)
        note("„${r.plain.trim()}“; danach: ${reply(p1, "clan info", "keinem Clan")}"); resetClans()
    }
    scenario("clan-74", "kick per UUID") {
        on(PAPER, admin, p1); clanWithMember()
        val ma = admin.mark(); admin.cmd("clan kick ${p1.uuid}")
        val r = admin.expect("aus dem Clan entfernt", since = ma)
        note("„${r.plain.trim()}“"); resetClans()
    }
    scenario("clan-75", "kick sich selbst") { on(PAPER, admin); createClan(admin); note(reply(admin, "clan kick ${A}", "nicht selbst rauswerfen")); resetClans() }
    scenario("clan-76", "kick ohne Clanrecht") { on(PAPER, admin, p1); clanWithMember(); note(reply(p1, "clan kick ${A}", "keine Berechtigung, diesen Spieler aus dem Clan zu entfernen")); resetClans() }
    scenario("clan-77", "kick höhere Rolle") { on(PAPER, admin, p1); clanWithMember("OFFICER"); note(reply(p1, "clan kick ${A}", "selben oder einer höheren Rolle")); resetClans() }
    scenario("clan-78", "/clan members") {
        on(PAPER, admin, p1); clanWithMember()
        val m = admin.run("clan members")
        val r = admin.expect("Mitglieder von ${CN}", since = m)
        sleep(600)
        val all = admin.messagesSince(m).joinToString(" ⏎ ") { it.plain.trim() }
        check(all.contains("${A}") && all.contains("${P1}")) { "„$all“" }
        val hover = admin.messagesSince(m).flatMap { it.hovers }.firstOrNull { it.second.contains("Online auf") }
        check(hover != null) { "Kein Hover „Online auf …“" }
        note("„${all.take(300)}“; Hover „${hover!!.second}“"); resetClans()
    }
    scenario("clan-79", "/clan members <Tag>") {
        on(PAPER, admin, p1); createClan(admin)
        note(reply(p1, "clan members ${CT}", "Mitglieder von ${CN}")); resetClans()
    }
    scenario("clan-80", "/clan members ohne Clan") { on(PAPER, p1); resetClans(); note(reply(p1, "clan members", "keinem Clan")) }
    scenario("clan-81", "/clan list") {
        on(PAPER, admin, p1); createClan(admin)
        val m = p1.run("clan list")
        val r = p1.expect("${CN}", since = m)
        val c = r.clicks.filter { it.text.contains("${CN}") }.minByOrNull { it.text.length }
        val hover = r.hovers.firstOrNull { it.first.contains(CN) }?.second
        note("„${r.plain.trim().replace("\n", " ⏎ ").take(250)}“; Klick: ${c?.value ?: "keiner"}; Hover „$hover“")
        if (c == null || !c.value.contains("clan info $CT")) { resetClans(); bad("Zeile führt nicht /clan info $CT aus; ${notes.last()}") }
        val m2 = p1.mark(); p1.click(c.event); p1.expect("Informationen", since = m2)
        resetClans()
    }
    scenario("clan-82", "/clan discord ohne Clanrecht") { on(PAPER, admin, p1); clanWithMember("OFFICER"); note(reply(p1, "clan discord https://discord.gg/abc", "keine Berechtigung, den Discord Link")); resetClans() }
    scenario("clan-83", "discord: zu wenige aktive Mitglieder") { on(PAPER, admin); createClan(admin); note(reply(admin, "clan discord https://discord.gg/abc", "mindestens 30 aktive Mitglieder")); resetClans() }
    notAutomatable("clan-84", "Braucht einen Clan mit ≥30 aktiven Mitgliedern; Aktivität kommt aus surf-core-Logins und lässt sich mit drei Bot-Konten nicht herstellen.")
    notAutomatable("clan-85", "Braucht einen Clan mit ≥30 aktiven Mitgliedern (siehe clan-84).")
    scenario("clan-86", "discord remove") {
        on(PAPER, admin); createClan(admin)
        note(reply(admin, "clan discord remove", "Der Discord Link wurde erfolgreich"))
        note(reply(admin, "clan discord NULL", "Der Discord Link wurde erfolgreich")); resetClans()
    }
    scenario("clan-87", "Tagfarbe Vordergrund") {
        on(PAPER, admin); createClan(admin)
        note(reply(admin, "clan color foreground #FF6B6B", "Die Farbe des Clan-Tags wurde erfolgreich geändert", Prefix.Success))
        note(reply(admin, "clan color foreground FF6B6B", "Die Farbe des Clan-Tags wurde erfolgreich geändert", Prefix.Success)); resetClans()
    }
    scenario("clan-88", "Tagfarbe Hintergrund") { on(PAPER, admin); createClan(admin); note(reply(admin, "clan color background 1A1A2E", "erfolgreich geändert")); resetClans() }
    scenario("clan-89", "Tagfarbe Schatten") {
        on(PAPER, admin); createClan(admin)
        note(reply(admin, "clan color shadow #FF6B6BCC", "erfolgreich geändert"))
        val db = net.sql("""SELECT clan_tag_shadow_color FROM "surf-clan".clan_clans WHERE tag = '${CT}'""")
        note("DB shadow=${db.firstOrNull()?.firstOrNull()}"); resetClans()
    }
    scenario("clan-90", "Tagfarbe ungültiger Hex") {
        on(PAPER, admin); createClan(admin)
        try {
            note(reply(admin, "clan color foreground ZZZZZZ", "'ZZZZZZ' ist keine gültige Farbe. Gib einen Hex-Farbcode mit 6 Stellen an"))
            note(reply(admin, "clan color shadow FF6B6B", "'FF6B6B' ist keine gültige Farbe. Gib einen Hex-Farbcode mit 8 Stellen an"))
        } finally { resetClans() }
    }
    scenario("clan-91", "Tagfarbe Vorschläge mit #") {
        on(PAPER, admin); createClan(admin)
        val sugg = admin.suggestions("/clan color foreground ")
        val shadow = admin.suggestions("/clan color shadow ")
        val first = sugg.firstOrNull { it.startsWith("#") } ?: run { resetClans(); bad("Keine #-Vorschläge: $sugg") }
        note("Vorschläge ${sugg.take(4)}, Schatten ${shadow.take(3)}")
        note(reply(admin, "clan color foreground $first", "erfolgreich geändert")); resetClans()
        if (shadow.any { it.removePrefix("#").length != 8 }) bad("Schatten-Vorschläge nicht 8-stellig: $shadow")
    }
    scenario("clan-92", "Tagfarbe ohne Clanrecht") {
        on(PAPER, admin, p1); clanWithMember("LEADER")
        withPermission(p1, "surf.player.clan.color.foreground") {
            on(LOBBY, p1); on(PAPER, p1)
            note(reply(p1, "clan color foreground FF0000", "Du hast keine Berechtigung, die Farbe des Clan-Tags zu ändern"))
        }
        resetClans()
    }
    scenario("clan-93", "Tagfarbe wird zurückgesetzt ohne Permission") {
        on(PAPER, admin); on(LOBBY, p1); clanWithMember()
        admin.cmd("clan color foreground FF6B6B"); sleep(1500)
        withPermission(admin, "surf.player.clan.color.foreground", false) {
            val since = java.time.Instant.now()
            on(PAPER, p1)
            sleep(4000)
            val log = net.logsSince("paper", since)
            val db = net.sql("""SELECT clan_tag_foreground_color FROM "surf-clan".clan_clans WHERE tag = '${CT}'""")
            val line = log.lines().firstOrNull { it.contains("Reset tag color") }
            note("Log: ${line?.trim() ?: "keine Reset-Meldung"}; DB-Vordergrundfarbe: ${db.firstOrNull()?.firstOrNull()}")
            if (line == null) bad("Keine „Reset tag color …“-Meldung beim Join eines Mitglieds; ${notes.last()}")
        }
        resetClans()
    }
    scenario("clan-96", "/clan leave Bestätigung") {
        on(PAPER, admin, p1); clanWithMember()
        val m = p1.run("clan leave")
        val r = p1.expect("Möchtest du den Clan ${CN} wirklich verlassen", since = m)
        check(r.hovers.any { it.second.contains("nicht rückgängig") }) { "Hover: ${r.hovers.map { it.second }}" }
        note("„${r.plain.trim()}“"); resetClans()
    }
    scenario("clan-97", "leave: HIER klicken") {
        on(PAPER, admin, p1); clanWithMember()
        val m = p1.run("clan leave")
        val r = p1.expect("wirklich verlassen", since = m)
        val ma = admin.mark(); val m2 = p1.mark()
        p1.clickIn(r, "HIER")
        val a = p1.expect("Du hast den Clan verlassen", since = m2)
        val b = admin.expect("${P1} hat den Clan verlassen", since = ma)
        note("„${a.plain.trim()}“ / „${b.plain.trim()}“")
        if (!hasPrefix(a, Prefix.Success)) odd("„Du hast den Clan verlassen.“ ohne Erfolgs-Präfix")
        resetClans()
    }
    scenario("clan-98", "leave: Klick nach Ablauf") {
        on(PAPER, admin, p1); clanWithMember()
        val m = p1.run("clan leave")
        val r = p1.expect("wirklich verlassen", since = m)
        sleep(65_000)
        val m2 = p1.mark(); p1.clickIn(r, "HIER"); sleep(2500)
        val members = net.sql("""SELECT count(*) FROM "surf-clan".clan_members WHERE uuid = '${p1.uuid}'""")
        check(members[0][0] == "1") { "${P1} hat den Clan nach >1 min trotzdem verlassen (${observed(p1, m2)})" }
        note("Klick nach 65 s wirkungslos (${observed(p1, m2)})"); resetClans()
    }
    scenario("clan-99", "leave: Klick nach Rauswurf") {
        on(PAPER, admin, p1); clanWithMember()
        val m = p1.run("clan leave")
        val r = p1.expect("wirklich verlassen", since = m)
        admin.cmd("clan kick ${p1.uuid}"); sleep(1500)
        val m2 = p1.mark(); p1.clickIn(r, "HIER")
        val e = p1.expect("nicht mehr in dem Clan", since = m2)
        note("„${e.plain.trim()}“"); resetClans()
    }
    scenario("clan-100", "leave als Besitzer") { on(PAPER, admin); createClan(admin); note(reply(admin, "clan leave", "Du bist der Besitzer des Clans und kannst ihn nicht verlassen. Nutze /clan delete, um den Clan aufzulösen.")); resetClans() }
    scenario("clan-101", "leave ohne Clan") { on(PAPER, p1); resetClans(); note(reply(p1, "clan leave", "keinem Clan")) }
    scenario("clan-102", "/clan delete Bestätigung") {
        on(PAPER, admin); createClan(admin)
        val m = admin.run("clan delete")
        val r = admin.expect("Bist du dir sicher, dass du den Clan ${CN} auflösen möchtest", since = m, prefix = Prefix.Warning)
        check(r.hovers.any { it.second.contains("Support") }) { "Hover: ${r.hovers.map { it.second }}" }
        note("„${r.plain.trim()}“"); resetClans()
    }
    scenario("clan-103", "delete: HIER klicken") {
        on(PAPER, admin, p1); clanWithMember()
        val m = admin.run("clan delete")
        val r = admin.expect("auflösen möchtest", since = m)
        val m1 = p1.mark(); admin.clickIn(r, "HIER")
        val e = p1.expect("${CN} wurde erfolgreich aufgelöst", since = m1)
        sleep(800)
        val db = net.sql("""SELECT count(*) FROM "surf-clan".clan_clans WHERE tag = '${CT}'""")
        check(db[0][0] == "0") { "Clan noch in der DB" }
        note("„${e.plain.trim()}“; Clan aus DB entfernt"); resetClans()
    }
    scenario("clan-104", "delete ohne Clanrecht") { on(PAPER, admin, p1); clanWithMember("LEADER"); note(reply(p1, "clan delete", "keine Berechtigung, den Clan aufzulösen")); resetClans() }
    scenario("clan-105", "delete >50 Mitglieder") {
        on(PAPER, admin); createClan(admin)
        net.sql("""INSERT INTO "surf-clan".clan_members (uuid, role, clan_id) SELECT gen_random_uuid(), 'MEMBER', id FROM "surf-clan".clan_clans, generate_series(1, 51) WHERE tag = '${CT}'""")
        net.console(PAPER, "surfplayer clan invalidatecaches", 2000)
        try {
            val m = admin.run("clan delete")
            val r = admin.firstMessage(m) ?: bad("Keine Antwort")
            check(r.plain.contains("mehr als")) { "„${r.plain.trim()}“" }
            note("„${r.plain.trim()}“")
        } finally {
            net.sql("""DELETE FROM "surf-clan".clan_members WHERE clan_id IN (SELECT id FROM "surf-clan".clan_clans WHERE tag = '${CT}') AND uuid NOT IN ($DEV_UUIDS_SQL)""")
            resetClans()
        }
    }
    scenario("clan-106", "delete ohne Clan") { on(PAPER, p1); resetClans(); note(reply(p1, "clan delete", "keinem Clan")) }
    scenario("clan-107", "/clan chat") {
        on(PAPER, admin, p1); clanWithMember()
        val t = uniq("Hallo"); val ma = admin.mark()
        p1.cmd("clan chat $t")
        val r = admin.expect("Clan", "${P1}", t, since = ma)
        check(r.clicks.any { it.value == "/clan chat " }) { "Klick schlägt nicht „/clan chat “ vor: ${r.clicks.map { it.value }}" }
        note("„${r.plain.trim()}“"); resetClans()
    }
    scenario("clan-108", "/cc") {
        on(PAPER, admin, p1); clanWithMember()
        val t = uniq(); val ma = admin.mark(); p1.cmd("cc $t")
        admin.expect(t, since = ma)
        resetClans()
        p1.expectCommandUnavailable("clanchat")
        note("/cc funktioniert; /clanchat unbekannt")
    }
    scenario("clan-109", "Clanchat ohne Clan") { on(PAPER, p1); resetClans(); note(reply(p1, "cc Hi", "keinem Clan")) }
    scenario("clan-110", "Clanchat serverübergreifend") {
        on(PAPER, admin); on(LOBBY, p1); clanWithMember()
        val t = uniq(); val ma = admin.mark(); p1.cmd("cc $t")
        admin.expect(t, since = ma)
        note("Clanchat aus der Lobby kommt auf Paper an"); resetClans()
    }
    scenario("clan-111", "Offene Einladungen beim Netzwerk-Login") {
        on(PAPER, admin); createClan(admin); quit(p1)
        admin.cmd("clan invite ${P1}"); sleep(1500)
        join(p1)
        val r = p1.expect("Du hast noch 1 offene Clan-Einladung", since = 0, timeout = 6000)
        sleep(500)
        val buttons = p1.since(0).flatMap { it.clicks }.map { it.value }.filter { it.contains("/clan accept") || it.contains("/clan deny") }
        note("„${r.plain.trim().replace("\n", " ⏎ ")}“; Buttons $buttons")
        if (!buttons.any { it.contains("accept $CT") } || !buttons.any { it.contains("deny $CT") }) { resetClans(); bad("Buttons führen nicht /clan accept|deny $CT aus: $buttons") }
        val m = p1.mark(); on(PAPER, p1)
        p1.expectNone("offene Clan-Einladung", since = m, within = 2500)
        resetClans()
    }
    scenario("clan-112", "Clan-Hover Discord-Klick") {
        on(PAPER, admin); resetClans()
        val m = admin.run("clan create ${CN} ${CT}")
        val r = admin.expect("erfolgreich erstellt", since = m)
        val discord = r.hovers.firstOrNull { it.second.contains("Discord") }
        note("Hovers: ${r.hovers.map { it.second.replace("\n", " ") }.take(3)}; Klicks: ${r.clicks.map { it.action + ":" + it.value }}")
        if (discord == null && r.clicks.none { it.value.contains("discord") }) odd("Kein Discord-Hinweis/-Klick am Clannamen; ${notes.last()}")
        resetClans()
    }
    scenario("clan-113", "/surfplayer clan reload") { on(PAPER, admin); note(reply(admin, "surfplayer clan reload", "Die Konfiguration von clan wurde neu geladen")) }
    scenario("clan-114", "/surfplayer clan invalidatecaches") {
        on(PAPER, admin)
        val m = admin.run("surfplayer clan invalidatecaches")
        val r = admin.expect("Die Clan-Caches wurden geleert", since = m)
        note("„${r.plain.trim()}“")
    }
    scenario("clan-115", "/surfplayer clan delete <Tag>") {
        on(PAPER, admin, p1); resetClans()
        val m = p1.run("clan create ${CN} ${CT}"); p1.expect("erfolgreich erstellt", since = m)
        note(reply(admin, "surfplayer clan delete ${CT}", "erfolgreich gelöscht")); resetClans()
    }
    scenario("clan-116", "Clan-Sync über Server") {
        on(PAPER, admin); createClan(admin); on(LOBBY, admin)
        note(reply(admin, "clan info", "${CN}")); resetClans()
    }
    scenario("clan-117", "Permission-Gates Clan (DevPlayer1)") {
        on(PAPER, p1)
        p1.expectCommandUnavailable("surfplayer")
        val sugg = p1.suggestions("/clan color ")
        check(sugg.none { it in listOf("foreground", "background", "shadow") }) { "/clan color bietet DevPlayer1 Unterbefehle an: $sugg" }
        note("/surfplayer fehlt; /clan color ohne Unterbefehle (Vorschläge: $sugg)")
    }
    scenario("clan-118", "Permission-Gate /clan Grundbefehl") {
        on(PAPER, p1)
        withPermission(p1, "surf.player.command.clan", false) {
            on(LOBBY, p1); on(PAPER, p1)
            p1.expectCommandUnavailable("clan")
            note("/clan fehlt bei surf.player.command.clan=false")
        }
    }
    scenario("clan-119", "/clan Hilfe") {
        on(PAPER, p1)
        note(helpContains(p1, "clan", "create", "info", "whois", "list", "members", "chat", "/cc", "invite", "revoke", "accept", "deny", "promote", "demote", "kick", "discord", "color", "leave", "delete"))
    }
    scenario("clan-120", "/clan revoke") {
        on(PAPER, admin, p1); createClan(admin)
        admin.cmd("clan invite ${P1}"); sleep(1200)
        note(reply(admin, "clan revoke ${P1}", "Die Einladung an ${P1} wurde zurückgezogen"))
        note(reply(p1, "clan accept ${CT}", "Du hast keine offene Einladung vom Clan '${CT}'.")); resetClans()
    }
    scenario("clan-121", "revoke ohne Einladung") {
        on(PAPER, admin, p1); createClan(admin)
        note(reply(admin, "clan revoke ${P1}", "'${P1}' hat keine offene Einladung in deinen Clan.")); resetClans()
    }
    scenario("clan-122", "revoke ohne Clanrecht") {
        on(PAPER, admin, p1, p2); clanWithMember()
        admin.cmd("clan invite ${P2}"); sleep(1200)
        note(reply(p1, "clan revoke ${P2}", "Du hast keine Berechtigung, Spieler in den Clan einzuladen")); resetClans()
    }
    scenario("clan-123", "revoke ohne Clan") { on(PAPER, p1); resetClans(); note(reply(p1, "clan revoke ${A}", "Du bist in keinem Clan")) }
    scenario("clan-124", "revoke Vorschläge") {
        on(PAPER, admin, p1, p2); createClan(admin)
        admin.cmd("clan invite ${P1}"); sleep(1200)
        val s = admin.suggestions("/clan revoke ")
        resetClans()
        check(s == listOf(P1)) { "Vorschläge: $s" }
        note("Vorschläge $s")
    }
    scenario("clan-125", "invite sich selbst") { on(PAPER, admin); createClan(admin); note(reply(admin, "clan invite ${A}", "Das kannst du nicht mit dir selbst tun.")); resetClans() }
    scenario("clan-126", "accept per Clanname") {
        on(PAPER, admin, p1); createClan(admin)
        admin.cmd("clan invite ${P1}"); sleep(1200)
        val s = p1.suggestions("/clan accept ")
        note(reply(p1, "clan accept ${CN.lowercase()}", "Du bist dem Clan ${CN} beigetreten"))
        note("Vorschläge $s"); resetClans()
    }
    scenario("clan-127", "[Annehmen] nach Rücknahme") {
        on(PAPER, admin, p1); createClan(admin)
        val m1 = p1.mark(); admin.cmd("clan invite ${P1}")
        val inv = p1.expect("eingeladen", since = m1)
        admin.cmd("clan revoke ${P1}"); sleep(1200)
        val m2 = p1.mark(); p1.clickIn(inv, "Annehmen")
        val r = p1.expect("Du hast keine offene Einladung vom Clan '${CT}'.", since = m2)
        note("„${r.plain.trim()}“"); resetClans()
    }
    scenario("clan-128", "discord NULL weiterhin akzeptiert") { on(PAPER, admin); createClan(admin); note(reply(admin, "clan discord NULL", "Der Discord Link wurde erfolgreich entfernt")); resetClans() }
    scenario("clan-129", "discord Vorschläge") {
        on(PAPER, admin); createClan(admin)
        val s = admin.suggestions("/clan discord ")
        resetClans()
        check(s.containsAll(listOf("https://discord.gg/castcrafter", "https://discord.com/invite/castcrafter", "remove"))) { "Vorschläge: $s" }
        note("Vorschläge $s")
    }
    scenario("clan-130", "color ohne Clan") { on(PAPER, admin); resetClans(); note(reply(admin, "clan color foreground FF0000", "Du bist in keinem Clan")) }
    scenario("clan-131", "delete: Klick nach Ablauf") {
        on(PAPER, admin); createClan(admin)
        val m = admin.run("clan delete")
        val r = admin.expect("auflösen möchtest", since = m)
        sleep(65_000)
        admin.clickIn(r, "HIER"); sleep(2500)
        val db = net.sql("""SELECT count(*) FROM "surf-clan".clan_clans WHERE tag = '${CT}'""")
        resetClans()
        check(db[0][0] == "1") { "Clan nach Klick >1 min trotzdem gelöscht" }
        note("Klick nach 65 s wirkungslos")
    }
    scenario("clan-132", "delete: Klick nach Clanwechsel") {
        on(PAPER, admin); createClan(admin)
        val m = admin.run("clan delete")
        val r = admin.expect("auflösen möchtest", since = m)
        net.console(PAPER, "surfplayer clan delete ${CT}", 1500)
        val m1 = admin.run("clan create ${CN2} ${CT2}"); admin.expect("erfolgreich erstellt", since = m1)
        val m2 = admin.mark(); admin.clickIn(r, "HIER")
        val e = admin.expect("Du bist nicht mehr in dem Clan, den du auflösen wolltest", since = m2)
        note("„${e.plain.trim()}“"); resetClans()
    }
    scenario("clan-133", "leave: Klick nach Clanwechsel") {
        on(PAPER, admin, p1, p2); clanWithMember()
        val m = p1.run("clan leave")
        val r = p1.expect("wirklich verlassen", since = m)
        admin.cmd("clan kick ${p1.uuid}"); sleep(1200)
        val m1 = p2.run("clan create ${CN2} ${CT2}"); p2.expect("erfolgreich erstellt", since = m1)
        joinClan(p2, p1, CN2, CT2)
        val m2 = p1.mark(); p1.clickIn(r, "HIER")
        val e = p1.expect("Der Clan, den du verlassen wolltest, hat sich geändert", since = m2)
        note("„${e.plain.trim()}“"); resetClans()
    }
    scenario("clan-134", "Mitgliedschaft sofort netzwerkweit") {
        on(PAPER, p1); on(LOBBY, admin); createClan(admin)
        joinClan(admin, p1)
        val a = answer(admin, "clan members")
        val b = answer(admin, "clan whois ${P1}")
        check(a.contains(P1) && b.contains("ist im Clan")) { "members „${a.take(200)}“, whois „$b“" }
        admin.cmd("clan kick ${p1.uuid}"); sleep(1000)
        val c = answer(admin, "clan whois ${P1}")
        resetClans()
        check(c.contains("ist in keinem Clan")) { "Nach Kick: „$c“" }
        note("Beitritt und Kick sofort in der Lobby sichtbar")
    }
    scenario("clan-135", "/surfplayer clan Aktionsliste") { on(PAPER, admin); note(helpContains(admin, "surfplayer clan", "reload", "invalidatecaches", "delete")) }
    scenario("clan-136", "/surfplayer clan delete unbekannter Tag") { on(LOBBY, admin); note(reply(admin, "surfplayer clan delete XXXX", "Es gibt keinen Clan mit dem Tag 'XXXX'.")) }
    scenario("clan-137", "/surfplayervelocity clan reload") { on(LOBBY, admin); note(reply(admin, "surfplayervelocity clan reload", "Die Konfiguration von clan wurde neu geladen")) }
    scenario("clan-138", "Präfixe der Clan-Meldungen") {
        on(PAPER, admin, p1); resetClans()
        val m = admin.run("clan create ${CN} ${CT}"); val ok = admin.expect("erfolgreich erstellt", since = m)
        val m2 = p1.run("clan info"); val err = p1.expect("keinem Clan", since = m2)
        val m3 = p1.run("clan whois ${A}"); val info = p1.expect("ist im Clan", since = m3)
        val m4 = admin.run("clan delete"); val warn = admin.expect("auflösen möchtest", since = m4)
        resetClans()
        val issues = listOfNotNull(
            if (!hasPrefix(ok, Prefix.Success)) "Erfolg ohne Erfolgs-Präfix" else null,
            if (!hasPrefix(err, Prefix.Error)) "Fehler ohne Fehler-Präfix" else null,
            if (!hasPrefix(info, Prefix.Info)) "whois ohne Info-Präfix" else null,
            if (!hasPrefix(warn, Prefix.Warning)) "Bestätigung ohne Warn-Präfix" else null,
        )
        if (issues.isNotEmpty()) bad(issues.joinToString("; "))
        note("Erfolg/Fehler/Info/Warnung mit passenden Präfixen")
    }
}
