package gg.deepsite.pewpew.api.events;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PewpewGunExplodeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player shooter;
    private final PewpewGunItem gun;
    private final Location location;
    @Setter
    private double blastRadius;
    @Setter
    private double explosionDamage;
    @Setter
    private boolean cancelled;

    public PewpewGunExplodeEvent(@Nullable Player shooter, @NotNull PewpewGunItem gun, @NotNull Location location,
                                 double blastRadius, double explosionDamage) {
        this.shooter = shooter;
        this.gun = gun;
        this.location = location;
        this.blastRadius = blastRadius;
        this.explosionDamage = explosionDamage;
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
