package gg.deepsite.pewpew.api.events;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PewpewHitBlockEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	@Nullable
	private final Player shooter;
	private final PewpewGunItem gun;
	@Nullable
	private final Block block;
	private final Location location;
	private final double distance;

	public PewpewHitBlockEvent(@Nullable Player shooter, @NotNull PewpewGunItem gun, @Nullable Block block,
	                           @NotNull Location location, double distance) {
		this.shooter = shooter;
		this.gun = gun;
		this.block = block;
		this.location = location;
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
