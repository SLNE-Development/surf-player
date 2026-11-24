package dev.slne.surf.social.minestom.clan

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.common.clan.ClanInstance
import java.nio.file.Path

@AutoService(ClanInstance::class)
class MinestomClanInstance : ClanInstance {
    override val dataPath: Path get() = ClanMinestomFeature.dataDirectory
}
