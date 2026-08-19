package gg.deepsite.pewpew.api.events;

import gg.deepsite.pewpew.api.enums.AttachmentType;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.attachment.PewpewAttachment;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PewpewAttachmentEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	@Nullable
	private final PewpewGunItem gun;
	private final ItemStack weapon;
	private final PewpewAttachment attachment;
	private final AttachmentType slot;
	private final boolean installing;

	@Setter
	private boolean cancelled;

	public PewpewAttachmentEvent(@NotNull Player player, @Nullable PewpewGunItem gun, @NotNull ItemStack weapon,
	                             @NotNull PewpewAttachment attachment, @NotNull AttachmentType slot,
	                             boolean installing) {
		this.player = player;
		this.gun = gun;
		this.weapon = weapon;
		this.attachment = attachment;
		this.slot = slot;
		this.installing = installing;
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
