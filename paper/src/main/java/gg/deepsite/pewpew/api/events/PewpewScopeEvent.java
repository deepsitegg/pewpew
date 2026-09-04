package gg.deepsite.pewpew.api.events;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.attachment.PewpewScopeAttachment;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PewpewScopeEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	@Nullable
	private final PewpewGunItem gun;
	@Nullable
	private final PewpewScopeAttachment scope;
	private final boolean scopingIn;

	@Setter
	private boolean cancelled;

	public PewpewScopeEvent(@NotNull Player player, @Nullable PewpewGunItem gun,
	                        @Nullable PewpewScopeAttachment scope, boolean scopingIn) {
		this.player = player;
		this.gun = gun;
		this.scope = scope;
		this.scopingIn = scopingIn;
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
