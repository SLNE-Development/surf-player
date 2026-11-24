package dev.slne.surf.player.minestom.friends

import com.google.auto.service.AutoService
import dev.slne.surf.api.minestom.coroutine.MinestomDispatchers
import dev.slne.surf.api.minestom.coroutine.minestomScope
import dev.slne.surf.player.core.client.friends.FriendsClientInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

@AutoService(FriendsClientInstance::class)
class MinestomFriendsClientInstance : FriendsClientInstance() {
    override val dataPath: Path get() = FriendsMinestomFeature.dataDirectory

    override val mainDispatcher: CoroutineContext
        get() = MinestomDispatchers.Main

    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ) = minestomScope.launch(
        context = context,
        start = start,
        block = block
    )
}
