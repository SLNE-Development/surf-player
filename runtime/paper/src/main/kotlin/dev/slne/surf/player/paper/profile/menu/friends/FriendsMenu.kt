package dev.slne.surf.player.paper.profile.menu.friends

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.builder.lore
import dev.slne.surf.api.paper.inventory.framework.dsl.onItemClick
import dev.slne.surf.api.paper.inventory.framework.dsl.openForPlayer
import dev.slne.surf.api.paper.inventory.framework.dsl.slot
import dev.slne.surf.api.paper.inventory.framework.dsl.withItem
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.layoutTarget
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.AbstractPaginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.view.surfView
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions
import dev.slne.surf.player.core.client.profile.menu.friends.FriendMenuEntry
import dev.slne.surf.player.core.client.profile.menu.friends.FriendRequestMenuEntry
import dev.slne.surf.player.core.client.profile.menu.friends.FriendsMenuContent
import dev.slne.surf.player.core.client.profile.menu.friends.SentFriendRequestMenuEntry
import dev.slne.surf.player.paper.plugin
import dev.slne.surf.player.paper.profile.menu.own.ownProfileMenu
import dev.slne.surf.player.paper.profile.menu.playClickSound
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemType

private const val FRIENDS_STATE = "friends"
private const val REQUEST_COUNT_STATE = "requestCount"
private const val REQUESTS_STATE = "requests"
private const val SENT_COUNT_STATE = "sentCount"
private const val SENT_REQUESTS_STATE = "sentRequests"
private const val FRIEND_STATE = "friend"

/** The friends of the viewer, as a list of player heads. */
@Suppress("UnstableApiUsage")
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
                withItem(ItemType.PAPER) {
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
                withItem(ItemType.WRITABLE_BOOK) {
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
            withItem(ItemType.PLAYER_HEAD) {
                displayName(FriendsMenuContent.friendDisplayName(entry))
                setData(
                    DataComponentTypes.PROFILE,
                    FriendHeads.head(entry.uuid, entry.name)
                )
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
@Suppress("UnstableApiUsage")
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
            withItem(ItemType.PLAYER_HEAD) {
                displayName(FriendsMenuContent.requestDisplayName(entry))
                setData(
                    DataComponentTypes.PROFILE,
                    FriendHeads.head(entry.senderUuid, entry.name)
                )
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

/** The friend requests the viewer sent; a click revokes one. */
@Suppress("UnstableApiUsage")
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
            withItem(ItemType.PLAYER_HEAD) {
                displayName(FriendsMenuContent.sentRequestDisplayName(entry))
                setData(
                    DataComponentTypes.PROFILE,
                    FriendHeads.head(entry.targetUuid, entry.name)
                )
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
        val sent = FriendsMenuContent.sentRequests(uniqueId)
        FriendHeads.preload(sent.associate { it.targetUuid to it.name })
        sentFriendRequestsView.open(this, mapOf(SENT_REQUESTS_STATE to sent))
    }
}

private fun Player.revokeFriendRequest(entry: SentFriendRequestMenuEntry) {
    plugin.launch(plugin.entityDispatcher(this)) {
        FriendsMenuContent.revokeRequest(uniqueId, entry.targetUuid, this@revokeFriendRequest)
        openSentFriendRequestsMenu()
    }
}

/**
 * Closes the open inventory of this player and opens the friends menu with their current friends.
 */
fun Player.openFriendsMenu() {
    openMenu { friendsView.open(this, friendsMenuState()) }
}

/**
 * Closes the open inventory of this player and opens the menu of the friend requests they received.
 */
fun Player.openFriendRequestsMenu() {
    openMenu {
        val requests = FriendsMenuContent.requests(uniqueId)
        FriendHeads.preload(requests.associate { it.senderUuid to it.name })
        friendRequestsView.open(this, mapOf(REQUESTS_STATE to requests))
    }
}

private suspend fun Player.friendsMenuState(): Map<String, Any> {
    val friends = FriendsMenuContent.friends(uniqueId)
    FriendHeads.preload(friends.associate { it.uuid to it.name })

    return mapOf(
        FRIENDS_STATE to friends,
        REQUEST_COUNT_STATE to FriendsMenuContent.requests(uniqueId).size,
        SENT_COUNT_STATE to FriendsMenuContent.sentRequests(uniqueId).size,
    )
}

private fun Player.answerFriendRequest(entry: FriendRequestMenuEntry, accept: Boolean) {
    plugin.launch(plugin.entityDispatcher(this)) {
        FriendsMenuContent.answerRequest(uniqueId, entry.senderUuid, accept, this@answerFriendRequest)
        openFriendRequestsMenu()
    }
}

/** Loads the data of a menu off the click handler, then opens it on the thread of this player. */
private fun Player.openMenu(open: suspend Player.() -> Unit) {
    plugin.launch(plugin.entityDispatcher(this)) {
        closeInventory()
        open()
    }
}


/** A single friend: their details and the way to end the friendship. */
@Suppress("UnstableApiUsage")
val friendDetailView = surfView(FriendsMenuContent.FRIEND_TITLE) {
    settings {
        rows(3)
        cancelAllInteractions()
    }

    val friendState = initialState<FriendMenuEntry>(FRIEND_STATE)

    onFirstRender {
        val entry = friendState[this]

        slot(2, 5) {
            withItem(ItemType.PLAYER_HEAD) {
                displayName(FriendsMenuContent.friendDisplayName(entry))
                setData(DataComponentTypes.PROFILE, FriendHeads.head(entry.uuid, entry.name))
                lore(*FriendsMenuContent.friendDetailLore(entry))
            }
        }

        if (player.hasPermission(FriendPermissions.COMMAND_REMOVE)) {
            slot(2, 7) {
                withItem(ItemType.BARRIER) {
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
@Suppress("UnstableApiUsage")
val friendRemoveView = surfView(FriendsMenuContent.REMOVE_TITLE) {
    settings {
        rows(3)
        cancelAllInteractions()
    }

    val friendState = initialState<FriendMenuEntry>(FRIEND_STATE)

    onFirstRender {
        val entry = friendState[this]

        slot(2, 5) {
            withItem(ItemType.PLAYER_HEAD) {
                displayName(FriendsMenuContent.removeQuestionDisplayName(entry))
                setData(DataComponentTypes.PROFILE, FriendHeads.head(entry.uuid, entry.name))
                lore(*FriendsMenuContent.removeQuestionLore)
            }
        }

        slot(2, 3) {
            withItem(ItemType.RED_CONCRETE) {
                displayName(FriendsMenuContent.removeConfirmDisplayName)
            }
            onItemClick {
                if (!player.hasPermission(FriendPermissions.COMMAND_REMOVE)) return@onItemClick

                playClickSound()
                player.removeFriend(entry)
            }
        }

        slot(2, 7) {
            withItem(ItemType.LIME_CONCRETE) {
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
        FriendHeads.preload(mapOf(entry.uuid to entry.name))
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
    plugin.launch(plugin.entityDispatcher(this)) {
        FriendsMenuContent.removeFriend(uniqueId, entry.uuid, this@removeFriend)
        openFriendsMenu()
    }
}
