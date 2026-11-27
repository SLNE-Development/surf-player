package dev.slne.surf.player.minestom.profile.menu.friends

import dev.slne.surf.api.minestom.builder.ItemDsl
import dev.slne.surf.api.minestom.inventory.framework.dsl.onItemClick
import dev.slne.surf.api.minestom.inventory.framework.dsl.openForPlayer
import dev.slne.surf.api.minestom.inventory.framework.dsl.slot
import dev.slne.surf.api.minestom.inventory.framework.dsl.withItem
import dev.slne.surf.api.minestom.inventory.framework.open
import dev.slne.surf.api.minestom.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.minestom.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.minestom.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.minestom.inventory.framework.view.layoutTarget
import dev.slne.surf.api.minestom.inventory.framework.view.onFirstRender
import dev.slne.surf.api.minestom.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.minestom.inventory.framework.view.pagination.AbstractPaginatedSurfView
import dev.slne.surf.api.minestom.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.minestom.inventory.framework.view.settings
import dev.slne.surf.api.minestom.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.api.minestom.inventory.framework.view.state.get
import dev.slne.surf.api.minestom.inventory.framework.view.state.initialState
import dev.slne.surf.api.minestom.inventory.framework.view.surfView
import dev.slne.surf.api.minestom.permission.hasPermission
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions
import dev.slne.surf.player.core.client.friends.platform.FriendsPlatform
import dev.slne.surf.player.core.client.profile.menu.friends.FriendMenuEntry
import dev.slne.surf.player.core.client.profile.menu.friends.FriendRequestMenuEntry
import dev.slne.surf.player.core.client.profile.menu.friends.FriendsMenuContent
import dev.slne.surf.player.core.client.profile.menu.friends.SentFriendRequestMenuEntry
import dev.slne.surf.player.minestom.profile.menu.own.ownProfileMenu
import dev.slne.surf.player.minestom.profile.menu.playClickSound
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Player
import net.minestom.server.item.Material
import net.minestom.server.item.component.TooltipDisplay
import net.minestom.server.network.player.ResolvableProfile
import java.util.*

private const val FRIENDS_STATE = "friends"
private const val REQUEST_COUNT_STATE = "requestCount"
private const val REQUESTS_STATE = "requests"
private const val SENT_COUNT_STATE = "sentCount"
private const val SENT_REQUESTS_STATE = "sentRequests"
private const val FRIEND_STATE = "friend"

/** The friends of the viewer, as a list of player heads. */
val friendsView: AbstractPaginatedSurfView = paginatedSurfView(FriendsMenuContent.TITLE) {
    settings {
        paginationViewRows(PaginationViewRows.TWO)
        cancelAllInteractions()
    }

    layoutTarget('I')

    val friendsState = initialState<List<FriendMenuEntry>>(FRIENDS_STATE)
    val requestCountState = initialState<Int>(REQUEST_COUNT_STATE)
    val sentCountState = initialState<Int>(SENT_COUNT_STATE)

    onFirstRender {
        slot(3, 1) {
            withItem(ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
                displayName(FriendsMenuContent.backDisplayName)
            })
            onItemClick {
                playClickSound()
                openForPlayer(ownProfileMenu)
            }
        }

        if (player.hasPermission(FriendPermissions.COMMAND_REVOKE)) {
            slot(3, 8) {
                withItem(Material.PAPER) {
                    displayName(FriendsMenuContent.sentRequestsButtonDisplayName(sentCountState[this@onFirstRender]))
                    lore(*FriendsMenuContent.sentRequestsButtonLore)
                }
                onItemClick {
                    playClickSound()
                    player.openSentFriendRequestsMenu()
                }
            }
        }

        if (player.hasPermission(FriendPermissions.COMMAND_REQUESTS)) {
            slot(3, 9) {
                withItem(Material.WRITABLE_BOOK) {
                    displayName(FriendsMenuContent.requestsButtonDisplayName(requestCountState[this@onFirstRender]))
                    lore(*FriendsMenuContent.requestsButtonLore)
                }
                onItemClick {
                    playClickSound()
                    player.openFriendRequestsMenu()
                }
            }
        }
    }

    pagination {
        computedSource { context -> friendsState[context] }

        itemFactory { entry ->
            withItem(Material.PLAYER_HEAD) {
                displayName(FriendsMenuContent.friendDisplayName(entry))
                headOf(entry.uuid)
                lore(*FriendsMenuContent.friendLore(entry))
            }

            onClick { click ->
                click.playClickSound()
                click.player.openFriendMenu(entry)
            }
        }
    }
}

/** The friend requests the viewer received; a click answers one. */
val friendRequestsView: AbstractPaginatedSurfView = paginatedSurfView(FriendsMenuContent.REQUESTS_TITLE) {
    settings {
        paginationViewRows(PaginationViewRows.TWO)
        cancelAllInteractions()
    }

    layoutTarget('I')

    val requestsState = initialState<List<FriendRequestMenuEntry>>(REQUESTS_STATE)

    onFirstRender {
        slot(3, 1) {
            withItem(ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
                displayName(FriendsMenuContent.backDisplayName)
            })
            onItemClick {
                playClickSound()
                player.openFriendsMenu()
            }
        }
    }

    pagination {
        computedSource { context -> requestsState[context] }

        itemFactory { entry ->
            withItem(Material.PLAYER_HEAD) {
                displayName(FriendsMenuContent.requestDisplayName(entry))
                headOf(entry.senderUuid)
                lore(*FriendsMenuContent.requestLore(entry))
            }

            onClick { click ->
                val accept = click.isLeftClick
                val permission = if (accept) FriendPermissions.COMMAND_ACCEPT else FriendPermissions.COMMAND_DENY
                if (!click.player.hasPermission(permission)) return@onClick

                click.playClickSound()
                click.player.answerFriendRequest(entry, accept)
            }
        }
    }
}

/**
 * Closes the open inventory of this player and opens the friends menu with their current friends.
 */
fun Player.openFriendsMenu() {
    openMenu {
        val friends = FriendsMenuContent.friends(uuid)
        FriendSkins.preload(friends.map { it.uuid })

        friendsView.open(
            this,
            mapOf(
                FRIENDS_STATE to friends,
                REQUEST_COUNT_STATE to FriendsMenuContent.requests(uuid).size,
                SENT_COUNT_STATE to FriendsMenuContent.sentRequests(uuid).size,
            )
        )
    }
}

/**
 * Closes the open inventory of this player and opens the menu of the friend requests they received.
 */
fun Player.openFriendRequestsMenu() {
    openMenu {
        val requests = FriendsMenuContent.requests(uuid)
        FriendSkins.preload(requests.map { it.senderUuid })
        friendRequestsView.open(this, mapOf(REQUESTS_STATE to requests))
    }
}

private fun Player.answerFriendRequest(entry: FriendRequestMenuEntry, accept: Boolean) {
    FriendsPlatform.INSTANCE.launch {
        FriendsMenuContent.answerRequest(uuid, entry.senderUuid, accept, this@answerFriendRequest)
        openFriendRequestsMenu()
    }
}

/** Loads the data of a menu off the click handler, then opens it for this player. */
private fun Player.openMenu(open: suspend Player.() -> Unit) {
    FriendsPlatform.INSTANCE.launch {
        closeInventory()
        open()
    }
}

/** Shows the skin of the player identified by [uuid] on this head; offline players keep the default head. */
private fun ItemDsl.headOf(uuid: UUID) {
    val skin = FriendSkins.skin(uuid) ?: return

    builder.set(DataComponents.PROFILE, ResolvableProfile(skin))
    builder.set(
        DataComponents.TOOLTIP_DISPLAY,
        TooltipDisplay(false, setOf(DataComponents.PROFILE))
    )
}

/** The friend requests the viewer sent; a click revokes one. */
val sentFriendRequestsView: AbstractPaginatedSurfView = paginatedSurfView(FriendsMenuContent.SENT_REQUESTS_TITLE) {
    settings {
        paginationViewRows(PaginationViewRows.TWO)
        cancelAllInteractions()
    }

    layoutTarget('I')

    val sentState = initialState<List<SentFriendRequestMenuEntry>>(SENT_REQUESTS_STATE)

    onFirstRender {
        slot(3, 1) {
            withItem(ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
                displayName(FriendsMenuContent.backDisplayName)
            })
            onItemClick {
                playClickSound()
                player.openFriendsMenu()
            }
        }
    }

    pagination {
        computedSource { context -> sentState[context] }

        itemFactory { entry ->
            withItem(Material.PLAYER_HEAD) {
                displayName(FriendsMenuContent.sentRequestDisplayName(entry))
                headOf(entry.targetUuid)
                lore(*FriendsMenuContent.sentRequestLore(entry))
            }

            onClick { click ->
                if (!click.player.hasPermission(FriendPermissions.COMMAND_REVOKE)) return@onClick

                click.playClickSound()
                click.player.revokeFriendRequest(entry)
            }
        }
    }
}

/**
 * Closes the open inventory of this player and opens the menu of the friend requests they sent.
 */
fun Player.openSentFriendRequestsMenu() {
    openMenu {
        val sent = FriendsMenuContent.sentRequests(uuid)
        FriendSkins.preload(sent.map { it.targetUuid })
        sentFriendRequestsView.open(this, mapOf(SENT_REQUESTS_STATE to sent))
    }
}

private fun Player.revokeFriendRequest(entry: SentFriendRequestMenuEntry) {
    FriendsPlatform.INSTANCE.launch {
        FriendsMenuContent.revokeRequest(uuid, entry.targetUuid, this@revokeFriendRequest)
        openSentFriendRequestsMenu()
    }
}

/** A single friend: their details and the way to end the friendship. */
val friendDetailView = surfView(FriendsMenuContent.FRIEND_TITLE) {
    settings {
        rows(3)
        cancelAllInteractions()
    }

    val friendState = initialState<FriendMenuEntry>(FRIEND_STATE)

    onFirstRender {
        val entry = friendState[this]

        slot(2, 5) {
            withItem(Material.PLAYER_HEAD) {
                displayName(FriendsMenuContent.friendDisplayName(entry))
                headOf(entry.uuid)
                lore(*FriendsMenuContent.friendDetailLore(entry))
            }
        }

        if (player.hasPermission(FriendPermissions.COMMAND_REMOVE)) {
            slot(2, 7) {
                withItem(Material.BARRIER) {
                    displayName(FriendsMenuContent.removeButtonDisplayName)
                    lore(*FriendsMenuContent.removeButtonLore)
                }
                onItemClick {
                    playClickSound()
                    player.openFriendRemoveMenu(entry)
                }
            }
        }

        slot(3, 1) {
            withItem(ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
                displayName(FriendsMenuContent.backDisplayName)
            })
            onItemClick {
                playClickSound()
                player.openFriendsMenu()
            }
        }
    }
}

/** Asks to confirm ending the friendship with a friend. */
val friendRemoveView = surfView(FriendsMenuContent.REMOVE_TITLE) {
    settings {
        rows(3)
        cancelAllInteractions()
    }

    val friendState = initialState<FriendMenuEntry>(FRIEND_STATE)

    onFirstRender {
        val entry = friendState[this]

        slot(2, 5) {
            withItem(Material.PLAYER_HEAD) {
                displayName(FriendsMenuContent.removeQuestionDisplayName(entry))
                headOf(entry.uuid)
                lore(*FriendsMenuContent.removeQuestionLore)
            }
        }

        slot(2, 3) {
            withItem(Material.RED_CONCRETE) {
                displayName(FriendsMenuContent.removeConfirmDisplayName)
            }
            onItemClick {
                if (!player.hasPermission(FriendPermissions.COMMAND_REMOVE)) return@onItemClick

                playClickSound()
                player.removeFriend(entry)
            }
        }

        slot(2, 7) {
            withItem(Material.LIME_CONCRETE) {
                displayName(FriendsMenuContent.removeCancelDisplayName)
            }
            onItemClick {
                playClickSound()
                player.openFriendMenu(entry)
            }
        }
    }
}

/**
 * Closes the open inventory of this player and opens the menu of the friend [entry].
 */
fun Player.openFriendMenu(entry: FriendMenuEntry) {
    openMenu {
        FriendSkins.preload(listOf(entry.uuid))
        friendDetailView.open(this, mapOf(FRIEND_STATE to entry))
    }
}

/**
 * Closes the open inventory of this player and asks them to confirm ending the friendship with [entry].
 */
fun Player.openFriendRemoveMenu(entry: FriendMenuEntry) {
    openMenu { friendRemoveView.open(this, mapOf(FRIEND_STATE to entry)) }
}

private fun Player.removeFriend(entry: FriendMenuEntry) {
    FriendsPlatform.INSTANCE.launch {
        FriendsMenuContent.removeFriend(uuid, entry.uuid, this@removeFriend)
        openFriendsMenu()
    }
}
