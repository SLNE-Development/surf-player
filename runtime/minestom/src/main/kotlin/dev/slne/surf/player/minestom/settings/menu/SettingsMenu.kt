package dev.slne.surf.player.minestom.settings.menu

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.minestom.builder.buildItem
import dev.slne.surf.api.minestom.inventory.framework.dsl.onItemClick
import dev.slne.surf.api.minestom.inventory.framework.dsl.onItemRender
import dev.slne.surf.api.minestom.inventory.framework.dsl.openForPlayer
import dev.slne.surf.api.minestom.inventory.framework.dsl.slot
import dev.slne.surf.api.minestom.inventory.framework.modifyConfig
import dev.slne.surf.api.minestom.inventory.framework.open
import dev.slne.surf.api.minestom.inventory.framework.view.AbstractSurfView
import dev.slne.surf.api.minestom.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.minestom.inventory.framework.view.containerDefaults
import dev.slne.surf.api.minestom.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.minestom.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.minestom.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.minestom.inventory.framework.view.layoutTarget
import dev.slne.surf.api.minestom.inventory.framework.view.onClose
import dev.slne.surf.api.minestom.inventory.framework.view.onFirstRender
import dev.slne.surf.api.minestom.inventory.framework.view.onOpen
import dev.slne.surf.api.minestom.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.minestom.inventory.framework.view.pagination.AbstractPaginatedSurfView
import dev.slne.surf.api.minestom.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.minestom.inventory.framework.view.settings
import dev.slne.surf.api.minestom.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.api.minestom.inventory.framework.view.state.get
import dev.slne.surf.api.minestom.inventory.framework.view.state.initialState
import dev.slne.surf.api.minestom.inventory.framework.view.surfView
import dev.slne.surf.player.core.client.settings.menu.SettingCategory
import dev.slne.surf.player.core.client.settings.menu.SettingsMenuDrafts
import dev.slne.surf.player.core.client.settings.menu.settingKeyMappings
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.entity.Player
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEventKeys

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
                        type(SoundEventKeys.UI_BUTTON_CLICK.key())
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
                    type(SoundEventKeys.UI_BUTTON_CLICK.key())
                }

                openForPlayer(settingsView)
            }
        }
    }

    pagination {
        computedSource { context ->
            SettingsMenuDrafts.categorySettings(context.player.uuid, categoryState[context])
        }

        itemFactory { playerSetting ->
            val display = settingKeyMappings[playerSetting.setting.name]

            if (display == null) {
                withItem(ViewIcon(ViewIconType.QUESTION_MARK, ViewIconColor.RED).build {
                    displayName {
                        error("Unbekannte Einstellung", TextDecoration.BOLD)
                    }

                    lore {
                        spacer(playerSetting.setting.name)
                    }
                })
                return@itemFactory
            }

            onItemRender {
                val currentValue = SettingsMenuDrafts.currentValue(player.uuid, playerSetting)

                item = buildItem(display.icon.toMaterial()) {
                    displayName {
                        white(display.displayName.toSmallCaps())
                    }

                    val lines = listOf(
                        Component.empty(),
                        buildText {
                            spacer(display.description)
                        },
                        Component.empty(),
                        buildText {
                            darkSpacer("▪")
                            appendSpace()
                            variableValue("Aktiviert")
                            if (currentValue) decorate(TextDecoration.BOLD)
                        },
                        buildText {
                            darkSpacer("▪")
                            appendSpace()
                            variableValue("Deaktiviert")
                            if (!currentValue) decorate(TextDecoration.BOLD)
                        }
                    ).map { it.decoration(TextDecoration.ITALIC, false) }

                    lore(*lines.toTypedArray())
                }
            }
            onClick { click ->
                SettingsMenuDrafts.toggle(click.player.uuid, playerSetting)

                click.player.playSound(true) {
                    type(SoundEventKeys.UI_BUTTON_CLICK.key())
                }

                click.update()
            }
        }
    }

    onClose {
        SettingsMenuDrafts.save(player.uuid, player)
    }
}

/**
 * Closes the open inventory of this player and opens the settings menu instead.
 */
fun Player.openSettingsMenu() {
    closeInventory()
    settingsView.open(this)
}

private fun Key.toMaterial(): Material = Material.fromKey(this) ?: error("Unknown item type $this")

private fun buildCategoryItem(category: SettingCategory) = buildItem(category.icon.toMaterial()) {
    displayName {
        white(category.displayName.toSmallCaps())
    }

    val settingLines = settingKeyMappings.values
        .filter { it.category == category }
        .map { display ->
            buildText {
                darkSpacer("▪")
                appendSpace()
                variableValue(display.displayName)
            }
        }

    val lines = listOf(
        Component.empty(),
        buildText {
            spacer(category.description)
        },
        Component.empty(),
    ).plus(settingLines).map { it.decoration(TextDecoration.ITALIC, false) }

    lore(*lines.toTypedArray())
}
