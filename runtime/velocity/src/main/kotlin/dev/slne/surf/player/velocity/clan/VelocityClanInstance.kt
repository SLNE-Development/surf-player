package dev.slne.surf.player.velocity.clan

import com.google.auto.service.AutoService
import dev.slne.surf.player.core.common.clan.ClanInstance
import java.nio.file.Path

@AutoService(ClanInstance::class)
class VelocityClanInstance : ClanInstance {
    override val dataPath: Path get() = ClanVelocityFeature.dataDirectory
}
