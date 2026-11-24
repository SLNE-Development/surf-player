package dev.slne.surf.social.minestom.clan

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.social.core.client.clan.ClientClanInstance
import java.nio.file.Path

@AutoService(ClanInstance::class)
class MinestomClanInstance : ClientClanInstance() {
    override val dataPath: Path get() = ClanMinestomPlugin.dataDirectory
}
