package dev.slne.surf.player.minestom.clan

import com.google.auto.service.AutoService
import dev.slne.surf.player.core.common.clan.ClanInstance
import java.nio.file.Path

@AutoService(ClanInstance::class)
class MinestomClanInstance : ClanInstance {
    override val dataPath: Path get() = ClanMinestomFeature.dataDirectory
}
