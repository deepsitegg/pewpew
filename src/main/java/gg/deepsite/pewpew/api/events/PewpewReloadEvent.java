package gg.deepsite.pewpew.api.events;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player starts reloading a gun, after the "already full" and
 * "no ammo in inventory" checks pass but before the reload timer starts.
 * <p>
 * Cancelling stops the reload from starting.
 */
@Getter
public class PewpewReloadEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final PewpewGunItem gun;
    private final ItemStack weapon;
    @Setter
    private boolean cancelled;

    public PewpewReloadEvent(@NotNull Player player, @NotNull PewpewGunItem gun, @NotNull ItemStack weapon) {
        this.player = player;
        this.gun = gun;
        this.weapon = weapon;
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
