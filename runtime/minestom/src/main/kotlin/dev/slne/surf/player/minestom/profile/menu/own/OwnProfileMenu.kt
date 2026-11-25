package dev.slne.surf.player.minestom.profile.menu.own

import dev.slne.surf.api.minestom.builder.ItemDsl
import dev.slne.surf.api.minestom.inventory.framework.dsl.slot
import dev.slne.surf.api.minestom.inventory.framework.dsl.withItem
import dev.slne.surf.api.minestom.inventory.framework.view.onFirstRender
import dev.slne.surf.api.minestom.inventory.framework.view.settings
import dev.slne.surf.api.minestom.inventory.framework.view.surfView
import dev.slne.surf.core.api.minestom.util.toSurfPlayer
import dev.slne.surf.api.minestom.permission.hasPermission
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions
import dev.slne.surf.player.core.client.profile.menu.own.OwnProfileMenuContent
import dev.slne.surf.player.minestom.profile.menu.friends.openFriendsMenu
import dev.slne.surf.player.minestom.settings.menu.openSettingsMenu
import dev.slne.surf.player.minestom.profile.menu.playClickSound
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Player
import net.minestom.server.item.Material
import net.minestom.server.item.component.TooltipDisplay
import net.minestom.server.network.player.ResolvableProfile

val ownProfileMenu = surfView(OwnProfileMenuContent.TITLE) {
    settings {
        rows(3)
        cancelAllInteractions()
    }

    onFirstRender {
        val player = this.player
        val surfPlayer = player.toSurfPlayer()

        slot(2, 5) {
            withItem(Material.PLAYER_HEAD) {
                displayName(OwnProfileMenuContent.headDisplayName(player.username))
                headOf(player)
                lore(*OwnProfileMenuContent.headLore(surfPlayer))
            }
        }

        slot(2, 3) {
            withItem(Material.POPPY) {
                displayName(OwnProfileMenuContent.friendsDisplayName)
                lore(*OwnProfileMenuContent.friendsLore)
            }

            onClick { click ->
                if (!click.player.hasPermission(FriendPermissions.COMMAND_LIST)) return@onClick

                click.playClickSound()
                click.player.openFriendsMenu()
            }
        }

        slot(2, 7) {
            withItem(Material.REPEATER) {
                displayName(OwnProfileMenuContent.settingsDisplayName)
                lore(*OwnProfileMenuContent.settingsLore)
            }

            onClick { click ->
                click.playClickSound()
                click.player.openSettingsMenu()
            }
        }
    }
}

private fun ItemDsl.headOf(player: Player) {
    val skin = player.skin ?: return

    builder.set(DataComponents.PROFILE, ResolvableProfile(skin))
    builder.set(
        DataComponents.TOOLTIP_DISPLAY,
        TooltipDisplay(false, setOf(DataComponents.PROFILE))
    )
}
