package gg.deepsite.pewpew.api.events;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
public class PewpewReloadCompleteEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final PewpewGunItem gun;
	private final ItemStack weapon;
	private final int ammo;
	private final int loaded;

	public PewpewReloadCompleteEvent(@NotNull Player player, @NotNull PewpewGunItem gun, @NotNull ItemStack weapon,
	                                 int ammo, int loaded) {
		this.player = player;
		this.gun = gun;
		this.weapon = weapon;
		this.ammo = ammo;
		this.loaded = loaded;
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
