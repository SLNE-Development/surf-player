package dev.slne.surf.social.paper.contentcreator.api

import com.google.auto.service.AutoService
import dev.slne.surf.social.api.contentcreator.ContentCreatorApi
import dev.slne.surf.social.core.client.contentcreator.CoreContentCreatorApi
import dev.slne.surf.social.paper.contentcreator.PaperContentCreatorFeature
import java.nio.file.Path

@AutoService(ContentCreatorApi::class)
class PaperContentCreatorApi : CoreContentCreatorApi() {
    override val dataPath: Path
        get() = PaperContentCreatorFeature.dataPath
}
