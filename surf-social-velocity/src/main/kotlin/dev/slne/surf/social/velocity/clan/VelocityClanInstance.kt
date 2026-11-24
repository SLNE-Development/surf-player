package dev.slne.surf.social.velocity.clan

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.common.clan.ClanInstance
import java.nio.file.Path

@AutoService(ClanInstance::class)
class VelocityClanInstance : ClanInstance {
    override val dataPath: Path get() = ClanVelocityFeature.dataDirectory
}
