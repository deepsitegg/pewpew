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
 * Fired right before a gun fires a single shot, after ammo/cooldown checks pass
 * but before ammo is consumed and the shot is executed. For burst weapons this
 * fires once per shot in the burst.
 * <p>
 * Cancelling prevents the shot: no ammo is consumed, no projectile/ray is fired.
 */
@Getter
public class PewpewShootEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player shooter;
    private final PewpewGunItem gun;
    /** The gun ItemStack in the shooter's hand. */
    private final ItemStack weapon;
    @Setter
    private boolean cancelled;

    public PewpewShootEvent(@NotNull Player shooter, @NotNull PewpewGunItem gun, @NotNull ItemStack weapon) {
        this.shooter = shooter;
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
