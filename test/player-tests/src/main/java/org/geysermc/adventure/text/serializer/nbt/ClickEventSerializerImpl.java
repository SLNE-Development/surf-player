package org.geysermc.adventure.text.serializer.nbt;

import dev.slne.surf.player.tests.bot.CustomClickPayloads;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.event.ClickEvent;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;

/**
 * Replaces MCProtocolLib's (text-serializer-nbt 0.2.1) click event serializer on the test
 * harness' class path: the original drops the payload of {@code custom} click events, but Paper's
 * {@code ClickEvent.callback} (id {@code paper:click_callback}) needs it sent back. The payload is
 * kept in {@link CustomClickPayloads} and referenced from the adventure event by a token.
 * Everything else behaves like the original.
 */
final class ClickEventSerializerImpl {
    private ClickEventSerializerImpl() {
    }

    static ClickEvent<?> deserialize(NbtMap tag) {
        String action = tag.getString("action");
        switch (action) {
            case "open_url":
                return ClickEvent.openUrl(tag.getString("url"));
            case "open_file":
                return ClickEvent.openFile(tag.getString("path"));
            case "run_command":
                return ClickEvent.runCommand(tag.getString("command"));
            case "suggest_command":
                return ClickEvent.suggestCommand(tag.getString("command"));
            case "change_page":
                return ClickEvent.changePage(tag.getInt("page"));
            case "copy_to_clipboard":
                return ClickEvent.copyToClipboard(tag.getString("value"));
            case "show_dialog": {
                Object dialog = tag.get("dialog");
                if (dialog instanceof String key) {
                    return ClickEvent.showDialog(new NbtDialog(Key.key(key)));
                }
                if (dialog instanceof NbtMap map) {
                    return ClickEvent.showDialog(new NbtDialog(map));
                }
                throw new IllegalArgumentException("Unknown dialog " + dialog);
            }
            case "custom": {
                Key id = Key.key(tag.getString("id"));
                Object payload = tag.get("payload");
                if (payload == null) {
                    return ClickEvent.custom(id);
                }
                return ClickEvent.custom(id, BinaryTagHolder.binaryTagHolder(CustomClickPayloads.store(payload)));
            }
            default:
                throw new IllegalArgumentException("Unknown click event action " + action);
        }
    }

    static NbtMap serialize(ClickEvent<?> event) {
        NbtMapBuilder builder = NbtMap.builder();
        String action = event.action().toString();
        builder.putString("action", action);
        ClickEvent.Payload payload = event.payload();
        switch (action) {
            case "open_url" -> builder.putString("url", ((ClickEvent.Payload.Text) payload).value());
            case "open_file" -> builder.putString("path", ((ClickEvent.Payload.Text) payload).value());
            case "run_command", "suggest_command" -> builder.putString("command", ((ClickEvent.Payload.Text) payload).value());
            case "copy_to_clipboard" -> builder.putString("value", ((ClickEvent.Payload.Text) payload).value());
            case "change_page" -> builder.putInt("page", ((ClickEvent.Payload.Int) payload).integer());
            case "custom" -> {
                ClickEvent.Payload.Custom custom = (ClickEvent.Payload.Custom) payload;
                builder.putString("id", custom.key().asString());
                BinaryTagHolder nbt = custom.nbt();
                Object raw = nbt == null ? null : CustomClickPayloads.get(nbt.string());
                if (raw != null) {
                    builder.put("payload", raw);
                }
            }
            default -> {
            }
        }
        return builder.build();
    }
}
