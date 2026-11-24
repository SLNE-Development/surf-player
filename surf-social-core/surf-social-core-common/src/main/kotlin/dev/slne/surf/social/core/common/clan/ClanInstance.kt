package dev.slne.surf.social.core.common.clan

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.social.core.common.clan.clan.CoreClanService
import org.jetbrains.annotations.MustBeInvokedByOverriders
import java.nio.file.Path

private val instance = requiredService<ClanInstance>()

interface ClanInstance {
    val dataPath: Path

    @MustBeInvokedByOverriders
    suspend fun load() {
        CoreClanService.init()
    }

    @MustBeInvokedByOverriders
    suspend fun enable() {
    }

    @MustBeInvokedByOverriders
    suspend fun disable() {
    }

    companion object : ClanInstance by instance {
        val INSTANCE get() = instance
    }
}