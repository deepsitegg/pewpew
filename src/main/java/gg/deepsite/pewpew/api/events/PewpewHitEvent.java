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

/**
 * Fired when a gun shot lands on a living entity, before damage and effects are
 * applied. The final damage is mutable — listeners may scale or override it.
 * <p>
 * Cancelling skips all damage, shield-disable, potion effects and hit feedback
 * for this hit.
 */
@Getter
public class PewpewHitEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    /** The shooter, or {@code null} if a projectile shot lost its player owner. */
    @Nullable
    private final Player shooter;
    private final PewpewGunItem gun;
    private final LivingEntity target;
    private final boolean headshot;
    private final boolean critical;
    /** Distance in blocks the shot travelled before hitting the target. */
    private final double distance;
    /** Final damage after headshot, crit and falloff multipliers. Mutable. */
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
