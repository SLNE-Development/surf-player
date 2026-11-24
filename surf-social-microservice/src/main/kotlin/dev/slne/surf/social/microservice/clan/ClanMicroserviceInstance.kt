package dev.slne.surf.social.microservice.clan

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.common.clan.ClanInstance
import java.nio.file.Path

@AutoService(ClanInstance::class)
class ClanMicroserviceInstance : ClanInstance {
    override val dataPath: Path get() = clanMicroservice.dataPath
}