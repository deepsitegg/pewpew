package gg.deepsite.pewpew.api.events;

import gg.deepsite.pewpew.api.enums.ThrowableEffect;
import gg.deepsite.pewpew.api.objects.PewpewThrowableItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PewpewThrowableDetonateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Item entity;
    private final PewpewThrowableItem throwable;
    private final Location location;
    @Setter
    private boolean cancelled;

    public PewpewThrowableDetonateEvent(@NotNull Item entity, @NotNull PewpewThrowableItem throwable,
                                        @NotNull Location location) {
        this.entity = entity;
        this.throwable = throwable;
        this.location = location;
    }

    @NotNull
    public ThrowableEffect getEffect() {
        return throwable.getEffect();
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
