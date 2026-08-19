package gg.deepsite.pewpew.api.events;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PewpewKillEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player victim;
	private final OfflinePlayer killer;
	private final PewpewGunItem gun;

	@Setter
	@Nullable
	private String deathMessage;

	public PewpewKillEvent(@NotNull Player victim, @NotNull OfflinePlayer killer, @NotNull PewpewGunItem gun,
	                       @Nullable String deathMessage) {
		this.victim = victim;
		this.killer = killer;
		this.gun = gun;
		this.deathMessage = deathMessage;
	}

	@Nullable
	public Player getOnlineKiller() {
		return killer.getPlayer();
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
