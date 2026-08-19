package dev.slne.surf.content.creator.paper.api

import com.google.auto.service.AutoService
import dev.slne.surf.content.creator.api.ContentCreatorApi
import dev.slne.surf.content.creator.core.CoreContentCreatorApi
import dev.slne.surf.content.creator.paper.plugin
import java.nio.file.Path

@AutoService(ContentCreatorApi::class)
class PaperContentCreatorApi : CoreContentCreatorApi() {
    override val dataPath: Path
        get() = plugin.dataPath
}
