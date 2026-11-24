package dev.slne.surf.player.paper.friends

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.friends.FriendsClientInstance
import dev.slne.surf.player.paper.plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

@AutoService(FriendsClientInstance::class)
class PaperFriendsClientInstance : FriendsClientInstance() {
    override val dataPath: Path get() = PaperFriendsFeature.dataPath

    override val mainDispatcher: CoroutineContext
        get() = plugin.globalRegionDispatcher

    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ) = plugin.launch(
        context = context,
        start = start,
        block = block
    )
}

