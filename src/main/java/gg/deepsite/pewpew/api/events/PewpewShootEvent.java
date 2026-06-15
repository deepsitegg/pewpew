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

@Getter
public class PewpewShootEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player shooter;
    private final PewpewGunItem gun;
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
