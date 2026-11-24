package dev.slne.surf.social.core.common.clan.clan

import dev.slne.surf.api.core.util.mutableObjectSetOf

object ClanTagRules {
    private val prohibitedTags = ProhibitedTagCategory.entries
        .flatMap { it.tags }
        .mapTo(mutableObjectSetOf()) { it.uppercase() }

    /**
     * Brings [tag] into the canonical form clan tags are stored and compared in.
     *
     * The contract every caller owes: normalize before storing a tag and before comparing one.
     * Storing a raw tag while looking it up normalized is what once made clans unreachable by their
     * tag, so the repository normalizes on insert and compares against `UPPER(tag)` rather than
     * trusting its callers.
     *
     * [uppercase] is deliberate over a locale-aware variant: it maps via [java.util.Locale.ROOT], so
     * the canonical form does not shift with the server's locale.
     */
    fun normalize(tag: String): String = tag.trim().uppercase()

    fun isValid(tag: String): Boolean =
        normalize(tag) !in prohibitedTags
}
