package dev.slne.surf.player.minestom.contentcreator.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.contentcreator.ContentCreatorApi
import dev.slne.surf.player.core.client.contentcreator.CoreContentCreatorApi
import dev.slne.surf.player.minestom.contentcreator.ContentCreatorMinestomFeature
import java.nio.file.Path

@AutoService(ContentCreatorApi::class)
class MinestomContentCreatorApi : CoreContentCreatorApi() {
    override val dataPath: Path
        get() = ContentCreatorMinestomFeature.dataDirectory
}
