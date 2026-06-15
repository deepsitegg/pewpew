package gg.deepsite.pewpew.api.events;

import gg.deepsite.pewpew.api.objects.PewpewThrowableItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
public class PewpewThrowEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final PewpewThrowableItem throwable;
    private final ItemStack item;
    @Setter
    private boolean cancelled;

    public PewpewThrowEvent(@NotNull Player player, @NotNull PewpewThrowableItem throwable, @NotNull ItemStack item) {
        this.player = player;
        this.throwable = throwable;
        this.item = item;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
