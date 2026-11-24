package dev.slne.surf.player.paper.settings.menu

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.dsl.onItemClick
import dev.slne.surf.api.paper.inventory.framework.dsl.onItemRender
import dev.slne.surf.api.paper.inventory.framework.dsl.openForPlayer
import dev.slne.surf.api.paper.inventory.framework.dsl.slot
import dev.slne.surf.api.paper.inventory.framework.modifyConfig
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.api.paper.inventory.framework.view.AbstractSurfView
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.containerDefaults
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.layoutTarget
import dev.slne.surf.api.paper.inventory.framework.view.onClose
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.onOpen
import dev.slne.surf.api.paper.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.AbstractPaginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.view.surfView
import dev.slne.surf.api.paper.util.BukkitSound
import dev.slne.surf.player.core.client.settings.menu.SettingCategory
import dev.slne.surf.player.core.client.settings.menu.SettingsMenuDrafts
import dev.slne.surf.player.core.client.settings.menu.settingKeyMappings
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player

private const val CATEGORY_STATE = "category"

val settingsView: AbstractSurfView = surfView("Einstellungen") {
    settings {
        rows(3)
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(1)
        blockRow(2, exemptColumns = (1..7).toList().toIntArray())
        blockRow(3)
    }

    onFirstRender {
        SettingCategory.entries.forEachIndexed { index, category ->
            slot(2, 3 + index) {
                withItem(buildCategoryItem(category))
                onItemClick {
                    player.playSound(true) {
                        type(BukkitSound.UI_BUTTON_CLICK)
                    }

                    openForPlayer(settingsCategoryView, mapOf(CATEGORY_STATE to category))
                }
            }
        }
    }
}

val settingsCategoryView: AbstractPaginatedSurfView = paginatedSurfView("Einstellungen") {
    settings {
        paginationViewRows(PaginationViewRows.TWO)
    }

    layoutTarget('I')

    val categoryState = initialState<SettingCategory>(CATEGORY_STATE)

    onOpen {
        modifyConfig {
            title("Einstellungen - ${categoryState[this@onOpen].displayName}")
        }
    }

    onFirstRender {
        slot(3, 1) {
            withItem(ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
                displayName {
                    error("Zurück")
                }
            })
            onItemClick {
                player.playSound(true) {
                    type(BukkitSound.UI_BUTTON_CLICK)
                }

                openForPlayer(settingsView)
            }
        }
    }

    pagination {
        computedSource { context ->
            SettingsMenuDrafts.categorySettings(context.player.uniqueId, categoryState[context])
        }

        itemFactory { playerSetting ->
            val display = settingKeyMappings[playerSetting.setting.name]

            if (display == null) {
                withItem(ViewIcon(ViewIconType.QUESTION_MARK, ViewIconColor.RED).build {
                    displayName {
                        error("Unbekannte Einstellung", TextDecoration.BOLD)
                    }

                    buildLore {
                        line {
                            spacer(playerSetting.setting.name)
                        }
                    }
                })
                return@itemFactory
            }

            onItemRender {
                val currentValue = SettingsMenuDrafts.currentValue(player.uniqueId, playerSetting)

                item = buildItem(display.icon.toMaterial()) {
                    displayName {
                        white(display.displayName.toSmallCaps())
                    }

                    buildLore {
                        emptyLine()
                        line {
                            spacer(display.description)
                        }
                        emptyLine()
                        line {
                            darkSpacer("▪")
                            appendSpace()
                            variableValue("Aktiviert")
                            if (currentValue) decorate(TextDecoration.BOLD)
                        }
                        line {
                            darkSpacer("▪")
                            appendSpace()
                            variableValue("Deaktiviert")
                            if (!currentValue) decorate(TextDecoration.BOLD)
                        }
                    }
                }
            }
            onClick { click ->
                SettingsMenuDrafts.toggle(click.player.uniqueId, playerSetting)

                click.player.playSound(true) {
                    type(BukkitSound.UI_BUTTON_CLICK)
                }

                click.update()
            }
        }
    }

    onClose {
        SettingsMenuDrafts.save(player.uniqueId, player)
    }
}

/**
 * Closes the open inventory of this player and opens the settings menu instead.
 */
fun Player.openSettingsMenu() {
    closeInventory()
    settingsView.open(this)
}

private fun Key.toMaterial(): Material =
    Material.matchMaterial(asString()) ?: error("Unknown item type $this")

private fun buildCategoryItem(category: SettingCategory) = buildItem(category.icon.toMaterial()) {
    displayName {
        white(category.displayName.toSmallCaps())
    }

    buildLore {
        emptyLine()
        line {
            spacer(category.description)
        }
        emptyLine()
        settingKeyMappings.values
            .filter { it.category == category }
            .forEach { display ->
                line {
                    darkSpacer("▪")
                    appendSpace()
                    variableValue(display.displayName)
                }
            }
    }
}
