package dev.slne.surf.player.core.client.command

import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.clan.permission.ClanPermissions
import dev.slne.surf.player.core.client.connection.permission.ConnectionPermissions
import dev.slne.surf.player.core.client.contentcreator.permission.ContentCreatorPermissions
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions
import dev.slne.surf.player.core.client.minimap.permission.MinimapPermissions
import dev.slne.surf.player.core.client.nametag.permission.NametagPermissions
import dev.slne.surf.player.core.client.playtime.permission.PlaytimePermissions
import dev.slne.surf.player.core.client.profile.permission.ProfilePermissions
import dev.slne.surf.player.core.client.queue.permission.QueuePermissions
import dev.slne.surf.player.core.client.settings.permission.SettingsPermissions
import dev.slne.surf.player.core.client.tab.permission.TabPermissions
import dev.slne.surf.player.core.client.vanish.permission.VanishPermissions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier

class PermissionSchemeTest {

    private val permissionObjects = listOf(
        ChatPermissions, ClanPermissions, ConnectionPermissions, ContentCreatorPermissions,
        FriendPermissions, MinimapPermissions, NametagPermissions, PlaytimePermissions,
        ProfilePermissions, QueuePermissions, SettingsPermissions, TabPermissions, VanishPermissions,
    )

    /** Every public node of [permissions], by constant name. */
    private fun nodesOf(permissions: Any): Map<String, String> =
        permissions.javaClass.declaredFields
            .filter { Modifier.isStatic(it.modifiers) && Modifier.isPublic(it.modifiers) && it.type == String::class.java }
            .associate { it.name to it.get(null) as String }

    private val allNodes get() = permissionObjects.flatMap { nodesOf(it).entries }

    @Test
    fun `every node follows the surf player scheme`() {
        val scheme = Regex("""surf\.player\.(command|team|admin|[a-z]+)\.[a-z0-9.-]+""")

        for ((name, node) in allNodes) {
            assertTrue(scheme.matches(node), "$name = $node")
        }
    }

    @Test
    fun `nodes are lowercase and unique`() {
        val nodes = allNodes.map { it.value }

        for (node in nodes) assertEquals(node.lowercase(), node)
        assertEquals(nodes.size, nodes.toSet().size)
    }

    @Test
    fun `admin action nodes are below the admin root`() {
        for ((name, node) in allNodes.filter { it.key.startsWith("ADMIN_") }) {
            assertTrue(node.startsWith("${AdminPermissions.ADMIN}."), "$name = $node")
        }
    }

    @Test
    fun `perks are not below the player command root`() {
        // The default group gets surf.player.command.*; perks must not be granted with it.
        val perks = listOf(
            ClanPermissions.TAG_COLOR_FOREGROUND,
            ClanPermissions.TAG_COLOR_BACKGROUND,
            ClanPermissions.TAG_COLOR_SHADOW,
            PlaytimePermissions.OTHERS,
        )

        for (perk in perks) assertTrue(!perk.startsWith("surf.player.command."), perk)
    }
}
