package dev.slne.surf.player.paper.contentcreator.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.contentcreator.ContentCreatorApi
import dev.slne.surf.player.core.client.contentcreator.CoreContentCreatorApi
import dev.slne.surf.player.paper.contentcreator.PaperContentCreatorFeature
import java.nio.file.Path

@AutoService(ContentCreatorApi::class)
class PaperContentCreatorApi : CoreContentCreatorApi() {
    override val dataPath: Path
        get() = PaperContentCreatorFeature.dataPath
}
