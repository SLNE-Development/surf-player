package dev.slne.surf.friends.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class FriendRequestSendEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private UUID player;
    private UUID target;
    private boolean canceled;

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {
        canceled = cancel;
    }
}
