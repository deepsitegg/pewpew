package gg.deepsite.pewpew.api.events;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PewpewHitEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    @Nullable
    private final Player shooter;
    private final PewpewGunItem gun;
    private final LivingEntity target;
    private final boolean headshot;
    private final boolean critical;
    private final double distance;
    @Setter
    private double damage;
    @Setter
    private boolean cancelled;

    public PewpewHitEvent(@Nullable Player shooter, @NotNull PewpewGunItem gun, @NotNull LivingEntity target,
                          double damage, boolean headshot, boolean critical, double distance) {
        this.shooter = shooter;
        this.gun = gun;
        this.target = target;
        this.damage = damage;
        this.headshot = headshot;
        this.critical = critical;
        this.distance = distance;
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
