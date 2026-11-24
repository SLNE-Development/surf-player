package dev.slne.surf.player.tests.bot

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.geysermc.mcprotocollib.network.Session
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter
import org.geysermc.mcprotocollib.network.factory.ClientNetworkSessionFactory
import org.geysermc.mcprotocollib.network.packet.Packet
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol
import org.geysermc.mcprotocollib.protocol.data.ProtocolState
import org.geysermc.mcprotocollib.protocol.data.game.entity.EntityEvent
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.HandPreference
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerAction
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerActionType
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ClickItemAction
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandType
import org.geysermc.mcprotocollib.protocol.data.game.scoreboard.TeamAction
import org.geysermc.mcprotocollib.protocol.data.game.setting.ChatVisibility
import org.geysermc.mcprotocollib.protocol.data.game.setting.ParticleStatus
import org.geysermc.mcprotocollib.protocol.data.game.setting.SkinPart
import org.geysermc.mcprotocollib.protocol.data.game.entity.`object`.Direction
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundDisconnectPacket
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundClientInformationPacket
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomClickActionPacket
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundFinishConfigurationPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.*
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityDataPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundAddEntityPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundSetHeldSlotPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerClosePacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetSlotPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundSetPlayerInventoryPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundPlayerInputPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSoundPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetPlayerTeamPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetActionBarTextPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetSubtitleTextPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.ClientboundSetTitleTextPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundCommandSuggestionPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundPlayerLoadedPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClickPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClosePacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundAcceptTeleportationPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerRotPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerActionPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerCommandPacket
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSetCarriedItemPacket
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerState
import org.cloudburstmc.math.vector.Vector3i
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PositionElement

import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/** What a client received, in arrival order. */
data class Received(
    val seq: Int,
    val kind: Kind,
    val component: Component?,
    val plain: String,
    /** Sound key for [Kind.SOUND], sender name for [Kind.CHAT]. */
    val extra: String = "",
    val at: Long = System.currentTimeMillis(),
) {
    enum class Kind { SYSTEM, CHAT, ACTIONBAR, TITLE, SUBTITLE, SOUND, DELETE_CHAT, DISCONNECT, DIALOG }

    val prefix: String get() = component?.let { Text.prefix(it) } ?: ""
    val clicks: List<Text.Click> get() = component?.let { Text.clicks(it) } ?: emptyList()
    val hovers: List<Pair<String, String>> get() = component?.let { Text.hovers(it) } ?: emptyList()
    override fun toString(): String = "$kind: $plain" + if (extra.isNotEmpty() && kind == Kind.SOUND) " ($extra)" else ""
}

data class TabEntry(
    val uuid: UUID,
    var name: String? = null,
    var displayName: Component? = null,
    var listed: Boolean = false,
    var listOrder: Int = 0,
    var gameMode: String = "",
) {
    val display: String get() = displayName?.let { Text.plain(it) } ?: (name ?: uuid.toString())
}

data class Team(
    val name: String,
    var prefix: Component? = null,
    var suffix: Component? = null,
    var visibility: String = "",
    var collision: String = "",
    var color: String = "",
    var seeFriendlyInvisibles: Boolean = false,
    val players: MutableSet<String> = ConcurrentHashMap.newKeySet(),
)

data class Item(val id: Int, val amount: Int, val name: Component?, val lore: List<Component>) {
    val nameText: String get() = Text.plain(name)
    val loreText: List<String> get() = lore.map { Text.plain(it) }
    override fun toString() = "$nameText x$amount ${loreText}"

    companion object {
        val EMPTY = Item(0, 0, null, emptyList())
    }
}

class Window(val id: Int, val type: String, val title: Component) {
    @Volatile var stateId: Int = 0
    val items: MutableMap<Int, Item> = ConcurrentHashMap()
    val titleText: String get() = Text.plain(title)
    fun item(slot: Int): Item? = items[slot]?.takeIf { it.id != 0 }
    fun nonEmpty(): Map<Int, Item> = items.filterValues { it.id != 0 }.toSortedMap()
}

/**
 * One headless Minecraft 26.3 client (MCProtocolLib) connected to the offline-mode bot proxy.
 * Thread-safe collections hold everything the tests assert on.
 */
class Bot(val name: String, private val host: String, private val port: Int) {
    val uuid: UUID = UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray(StandardCharsets.UTF_8))

    val events = CopyOnWriteArrayList<Received>()
    private val seq = AtomicInteger()
    val tab = ConcurrentHashMap<UUID, TabEntry>()
    @Volatile var header: Component? = null
    @Volatile var footer: Component? = null
    val teams = ConcurrentHashMap<String, Team>()
    /** Player entities in view: uuid → entity id. */
    val players = ConcurrentHashMap<UUID, Int>()
    val glowing = ConcurrentHashMap.newKeySet<Int>()
    @Volatile var window: Window? = null
    val inventory = ConcurrentHashMap<Int, Item>()
    @Volatile var heldSlot = 0
    @Volatile var commandRoots: Set<String> = emptySet()
    @Volatile var loginCount = 0
    @Volatile var disconnectReason: String? = null
    @Volatile var entityId = -1
    @Volatile var x = 0.0
    @Volatile var y = 0.0
    @Volatile var z = 0.0
    @Volatile var yaw = 0f
    @Volatile var pitch = 0f
    val customPayloads = CopyOnWriteArrayList<Pair<String, ByteArray>>()
    /** Number of server-side teleports (position packets) received. */
    val teleports = AtomicInteger()
    val deaths = AtomicInteger()
    private val suggestionReplies = ConcurrentHashMap<Int, ClientboundCommandSuggestionsPacket>()
    private val suggestionIds = AtomicInteger(1)
    private var session: Session? = null

    val connected: Boolean get() = session?.isConnected == true && loginCount > 0

    fun connect(timeoutMs: Long = 30_000) {
        disconnectReason = null
        val before = loginCount
        val protocol = MinecraftProtocol(name)
        val s = ClientNetworkSessionFactory.factory()
            .setAddress(host, port)
            .setProtocol(protocol)
            .create()
        s.addListener(Listener())
        session = s
        s.connect(true)
        waitUntil(timeoutMs, "login of $name") { loginCount > before || disconnectReason != null }
        disconnectReason?.let { throw IllegalStateException("$name could not join: $it") }
    }

    fun disconnect() {
        session?.disconnect("bye")
        session = null
        loginCount = 0
        players.clear()
        tab.clear()
        teams.clear()
        window = null
    }

    fun send(packet: Packet) {
        val s = session ?: error("$name is not connected")
        s.send(packet)
    }

    // ------------------------------------------------------------------------------------------
    // actions
    // ------------------------------------------------------------------------------------------

    fun chat(message: String) {
        send(ServerboundChatPacket(message, System.currentTimeMillis(), 0L, null, 0, BitSet(), 0))
    }

    /** Runs a command; a leading "/" is optional. */
    fun cmd(command: String) {
        send(ServerboundChatCommandPacket(command.removePrefix("/")))
    }

    /** Tab completion of [text] (as typed in the chat box, including the leading "/"). */
    fun suggestions(text: String, timeoutMs: Long = 5_000): List<String> {
        val id = suggestionIds.getAndIncrement()
        send(ServerboundCommandSuggestionPacket(id, text))
        val reply = waitFor(timeoutMs) { suggestionReplies.remove(id) } ?: return emptyList()
        return reply.matches.toList()
    }

    /** Executes a click event the way a vanilla client would. */
    fun click(event: ClickEvent<*>) {
        val payload = event.payload()
        when (event.action()) {
            ClickEvent.Action.RUN_COMMAND -> {
                val value = (payload as ClickEvent.Payload.Text).value()
                if (value.startsWith("/")) cmd(value) else chat(value)
            }
            ClickEvent.Action.CUSTOM -> {
                // Paper's ClickEvent.callback: id "paper:callback" with the callback id in the NBT
                // payload, which has to be sent back as it came.
                val custom = payload as ClickEvent.Payload.Custom
                val token = runCatching { custom.nbt()?.string() }.getOrNull()
                // The stored payload is an NbtMap or, depending on how the component was decoded,
                // its SNBT text; the server only accepts the compound.
                val raw = when (val stored = token?.let { CustomClickPayloads.get(it) } ?: token) {
                    is String -> runCatching { Snbt.parseCompound(stored) }.getOrNull()
                    else -> stored
                }
                if (System.getenv("BOT_DEBUG") != null) println("[$name] custom click ${custom.key()} payload=${raw?.javaClass?.simpleName} $raw")
                send(ServerboundCustomClickActionPacket(custom.key(), raw as? org.cloudburstmc.nbt.NbtMap))
            }
            else -> error("$name cannot execute click action ${event.action()} (${Text.payloadString(event)})")
        }
    }

    fun look(yaw: Float, pitch: Float) {
        this.yaw = yaw
        this.pitch = pitch
        send(ServerboundMovePlayerRotPacket(true, false, yaw, pitch))
    }

    fun moveTo(nx: Double, ny: Double, nz: Double) {
        x = nx; y = ny; z = nz
        send(ServerboundMovePlayerPosRotPacket(true, false, x, y, z, yaw, pitch))
    }

    fun sneak(on: Boolean) {
        send(ServerboundPlayerInputPacket(false, false, false, false, false, on, false))
    }

    fun swapHands() {
        send(ServerboundPlayerActionPacket(PlayerAction.SWAP_HANDS, Vector3i.ZERO, Direction.DOWN, 0))
    }

    fun selectHotbar(slot: Int) {
        heldSlot = slot
        send(ServerboundSetCarriedItemPacket(slot))
    }

    fun clickSlot(slot: Int, button: Int = 0) {
        val w = window ?: error("$name has no open window")
        send(
            ServerboundContainerClickPacket(
                w.id, w.stateId, slot, ContainerActionType.CLICK_ITEM,
                if (button == 0) ClickItemAction.LEFT_CLICK else ClickItemAction.RIGHT_CLICK,
                null, it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap(),
            )
        )
    }

    fun closeWindow() {
        val w = window ?: return
        send(ServerboundContainerClosePacket(w.id))
        window = null
    }

    fun sendPluginMessage(channel: String, data: ByteArray) {
        send(ServerboundCustomPayloadPacket(Key.key(channel), data))
    }

    // ------------------------------------------------------------------------------------------
    // observation
    // ------------------------------------------------------------------------------------------

    fun mark(): Int = seq.get()

    fun since(mark: Int): List<Received> = events.filter { it.seq > mark }

    fun messagesSince(mark: Int): List<Received> =
        since(mark).filter { it.kind == Received.Kind.SYSTEM || it.kind == Received.Kind.CHAT }

    fun await(timeoutMs: Long, since: Int, predicate: (Received) -> Boolean): Received? =
        waitFor(timeoutMs) { events.firstOrNull { it.seq > since && predicate(it) } }

    fun listedNames(): List<String> = tab.values.filter { it.listed }.mapNotNull { it.name }

    fun teamOf(player: String): Team? = teams.values.firstOrNull { player in it.players }

    fun sees(other: Bot): Boolean = players.containsKey(other.uuid)

    private fun record(kind: Received.Kind, component: Component?, plain: String = Text.plain(component), extra: String = "") {
        events += Received(seq.incrementAndGet(), kind, component, plain, extra)
        if (events.size > 5000) events.subList(0, 1000).clear()
    }

    fun resendClientInformation() = send(clientInformation())

    private fun clientInformation() = ServerboundClientInformationPacket(
        "de_de", 4, ChatVisibility.FULL, true, SkinPart.entries.toList(),
        HandPreference.RIGHT_HAND, false, true, ParticleStatus.ALL,
    )

    private fun item(stack: ItemStack?): Item {
        if (stack == null) return Item.EMPTY
        val comps = stack.dataComponentsPatch
        val name = comps?.get(DataComponentTypes.CUSTOM_NAME) ?: comps?.get(DataComponentTypes.ITEM_NAME)
        val lore = comps?.get(DataComponentTypes.LORE) ?: emptyList()
        return Item(stack.id, stack.amount, name, lore)
    }

    private inner class Listener : SessionAdapter() {
        override fun packetReceived(session: Session, packet: Packet) {
            try {
                handle(session, packet)
            } catch (t: Throwable) {
                System.err.println("[$name] failed to handle ${packet.javaClass.simpleName}: $t")
            }
        }

        override fun disconnected(event: DisconnectedEvent) {
            disconnectReason = Text.plain(event.reason)
            record(Received.Kind.DISCONNECT, event.reason)
            loginCount = 0
        }
    }

    private fun handle(session: Session, packet: Packet) {
        when (packet) {
            is ClientboundLoginPacket -> {
                entityId = packet.entityId
                players.clear()
                glowing.clear()
                teams.clear()
                window = null
                loginCount++
                session.send(clientInformation())
            }
            // Client options must already reach the backend during configuration; otherwise
            // Paper may keep chat visibility "hidden" and answer with chat.disabled.options.
            is org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundSelectKnownPacks ->
                session.send(clientInformation())
            // Bots stand still at spawn and get killed by mobs; a dead (removed) player cannot chat
            // ("chat.disabled.options"), so respawn at once like the vanilla auto-respawn.
            is org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundSetHealthPacket ->
                if (packet.health <= 0f) {
                    deaths.incrementAndGet()
                    session.send(
                        org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundClientCommandPacket(
                            org.geysermc.mcprotocollib.protocol.data.game.ClientCommand.PERFORM_RESPAWN
                        )
                    )
                }
            is ClientboundRespawnPacket -> {
                players.clear()
                glowing.clear()
            }
            is ClientboundPlayerPositionPacket -> {
                teleports.incrementAndGet()
                val rel = packet.relatives
                x = packet.position.x + if (PositionElement.X in rel) x else 0.0
                y = packet.position.y + if (PositionElement.Y in rel) y else 0.0
                z = packet.position.z + if (PositionElement.Z in rel) z else 0.0
                yaw = packet.yRot + if (PositionElement.Y_ROT in rel) yaw else 0f
                pitch = packet.xRot + if (PositionElement.X_ROT in rel) pitch else 0f
                if (System.getenv("BOT_DEBUG") != null) println("[$name] position $packet -> $x $y $z")
                session.send(ServerboundAcceptTeleportationPacket(packet.id, x, y, z, yaw, pitch))
                // No move packet here: Paper 26.3 kicks bots for "Invalid move player packet" when one follows the accept.
                session.send(ServerboundPlayerLoadedPacket.INSTANCE)
            }
            is ClientboundSystemChatPacket ->
                record(if (packet.isOverlay) Received.Kind.ACTIONBAR else Received.Kind.SYSTEM, packet.content)
            is ClientboundPlayerChatPacket -> {
                val component = packet.unsignedContent
                    ?: Component.text().append(packet.name).append(Component.text(": ")).append(Component.text(packet.content)).build()
                record(Received.Kind.CHAT, component, extra = Text.plain(packet.name))
            }
            is ClientboundDisguisedChatPacket -> {
                record(Received.Kind.CHAT, packet.message, extra = Text.plain(packet.name))
            }
            is ClientboundDeleteChatPacket -> record(Received.Kind.DELETE_CHAT, null, "deleted")
            is ClientboundSetActionBarTextPacket -> record(Received.Kind.ACTIONBAR, packet.text)
            is ClientboundSetTitleTextPacket -> record(Received.Kind.TITLE, packet.text)
            is ClientboundSetSubtitleTextPacket -> record(Received.Kind.SUBTITLE, packet.text)
            is ClientboundSoundPacket -> record(Received.Kind.SOUND, null, packet.sound.name, packet.sound.name)
            is ClientboundSoundEntityPacket -> record(Received.Kind.SOUND, null, packet.sound.name, packet.sound.name)
            is ClientboundShowDialogGamePacket -> record(Received.Kind.DIALOG, null, packet.dialog.toString())
            is ClientboundTabListPacket -> {
                header = packet.header
                footer = packet.footer
            }
            is ClientboundPlayerInfoUpdatePacket -> {
                for (e in packet.entries) {
                    val entry = tab.getOrPut(e.profileId) { TabEntry(e.profileId) }
                    for (action in packet.actions) when (action) {
                        PlayerListEntryAction.ADD_PLAYER -> entry.name = e.profile?.name
                        PlayerListEntryAction.UPDATE_LISTED -> entry.listed = e.isListed
                        PlayerListEntryAction.UPDATE_DISPLAY_NAME -> entry.displayName = e.displayName
                        PlayerListEntryAction.UPDATE_LIST_ORDER -> entry.listOrder = e.listOrder
                        PlayerListEntryAction.UPDATE_GAME_MODE -> entry.gameMode = e.gameMode?.name ?: ""
                        else -> {}
                    }
                }
            }
            is ClientboundPlayerInfoRemovePacket -> packet.profileIds.forEach { tab.remove(it) }
            is ClientboundSetPlayerTeamPacket -> {
                when (packet.action) {
                    TeamAction.REMOVE -> teams.remove(packet.teamName)
                    TeamAction.ADD_PLAYER -> packet.players?.filterNotNull()?.let { teams.getOrPut(packet.teamName) { Team(packet.teamName) }.players.addAll(it) }
                    TeamAction.REMOVE_PLAYER -> packet.players?.filterNotNull()?.let { teams[packet.teamName]?.players?.removeAll(it.toSet()) }
                    TeamAction.CREATE, TeamAction.UPDATE -> {
                        val t = teams.getOrPut(packet.teamName) { Team(packet.teamName) }
                        t.prefix = packet.playerPrefix
                        t.suffix = packet.playerSuffix
                        t.visibility = packet.nameTagVisibility?.name ?: ""
                        t.collision = packet.collisionRule?.name ?: ""
                        t.color = packet.color?.name ?: ""
                        t.seeFriendlyInvisibles = packet.isSeeFriendlyInvisibles
                        packet.players?.filterNotNull()?.let { t.players.addAll(it) }
                    }
                }
            }
            is ClientboundAddEntityPacket -> if (packet.type == EntityType.PLAYER) players[packet.uuid] = packet.entityId
            is ClientboundRemoveEntitiesPacket -> {
                val ids = packet.entityIds.toSet()
                players.entries.removeIf { it.value in ids }
            }
            is ClientboundSetEntityDataPacket -> {
                for (m in packet.metadata) if (m.id == 0) {
                    val flags = (m.value as? Byte)?.toInt() ?: continue
                    if (flags and 0x40 != 0) glowing += packet.entityId else glowing -= packet.entityId
                }
            }
            is ClientboundOpenScreenPacket -> window = Window(packet.containerId, packet.type.name, packet.title)
            is ClientboundContainerSetContentPacket -> {
                if (packet.containerId == 0) {
                    packet.items.forEachIndexed { i, s -> inventory[i] = item(s) }
                } else window?.takeIf { it.id == packet.containerId }?.let { w ->
                    w.stateId = packet.stateId
                    packet.items.forEachIndexed { i, s -> w.items[i] = item(s) }
                }
            }
            is ClientboundContainerSetSlotPacket -> {
                if (packet.containerId == 0) inventory[packet.slot] = item(packet.item)
                else window?.takeIf { it.id == packet.containerId }?.let { w ->
                    w.stateId = packet.stateId
                    w.items[packet.slot] = item(packet.item)
                }
            }
            is ClientboundSetPlayerInventoryPacket -> inventory[packet.slot] = item(packet.contents)
            is ClientboundContainerClosePacket -> window = null
            is ClientboundSetHeldSlotPacket -> heldSlot = packet.slot
            is ClientboundCommandSuggestionsPacket -> suggestionReplies[packet.transactionId] = packet
            is ClientboundCommandsPacket -> {
                val root = packet.nodes[packet.firstNodeIndex]
                commandRoots = root.childIndices.toList().mapNotNull { idx -> packet.nodes[idx].name }.toSet()
            }
            is ClientboundCustomPayloadPacket -> customPayloads += packet.channel.asString() to packet.data
            is ClientboundDisconnectPacket -> disconnectReason = Text.plain(packet.reason)
            else -> {}
        }
    }

    override fun toString() = name
}

fun waitUntil(timeoutMs: Long, what: String, condition: () -> Boolean) {
    val end = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < end) {
        if (condition()) return
        Thread.sleep(50)
    }
    throw IllegalStateException("Timed out after ${timeoutMs}ms waiting for $what")
}

fun <T : Any> waitFor(timeoutMs: Long, supplier: () -> T?): T? {
    val end = System.currentTimeMillis() + timeoutMs
    while (true) {
        supplier()?.let { return it }
        if (System.currentTimeMillis() >= end) return null
        Thread.sleep(50)
    }
}
