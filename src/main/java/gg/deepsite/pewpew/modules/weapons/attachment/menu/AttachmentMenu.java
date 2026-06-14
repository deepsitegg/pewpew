package gg.deepsite.pewpew.modules.weapons.attachment.menu;

import com.jazzkuh.inventorylib.objects.icon.Icon;
import gg.deepsite.pewpew.api.enums.AttachmentType;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.attachment.PewpewAttachment;
import gg.deepsite.pewpew.modules.weapons.attachment.AttachmentUtil;
import gg.deepsite.pewpew.modules.weapons.lore.GunLoreRenderer;
import gg.deepsite.pewpew.utils.ChatUtils;
import gg.deepsite.pewpew.utils.item.ItemBuilder;
import gg.deepsite.pewpew.utils.item.ItemFactory;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AttachmentMenu extends AbstractGunMenu {

	private static final List<AttachmentType> ORDER = List.of(
			AttachmentType.SCOPE, AttachmentType.BARREL, AttachmentType.GRIP, AttachmentType.MAGAZINE);

	public AttachmentMenu(Player viewer, int gunSlot) {
		super(ChatUtils.format("<color>Attachments").decoration(TextDecoration.ITALIC, false),
				InventoryType.HOPPER, viewer, gunSlot);
		build();
	}

	private void build() {
		clearItems();
		ItemStack gun = gunStack();
		if (gun == null) return;
		PewpewGunItem gunItem = (PewpewGunItem) itemsModule().fromItemStack(gun);
		List<AttachmentType> allowed = gunItem.getAllowedAttachmentSlots();

		addItem(new Icon(0, gun.clone()));

		for (int i = 0; i < ORDER.size(); i++) {
			AttachmentType type = ORDER.get(i);
			int slot = i + 1;
			if (allowed != null && allowed.contains(type)) {
				PewpewAttachment fitted = AttachmentUtil.get(gun, type);
				if (fitted != null) {
					addItem(new Icon(slot, ItemFactory.build(fitted), true, event -> detach(type)));
				} else {
					addItem(new Icon(slot, emptyPane(type)));
				}
			} else {
				addItem(new Icon(slot, filler()));
			}
		}
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		if (!event.isShiftClick()) return;
		Inventory clicked = event.getClickedInventory();
		if (clicked == null || clicked.equals(getInventory())) return;

		ItemStack stack = event.getCurrentItem();
		if (stack == null) return;
		PewPewItem item = itemsModule().fromItemStack(stack);
		if (item instanceof PewpewAttachment attachment) fit(attachment);
	}

	private void fit(PewpewAttachment attachment) {
		ItemStack gun = gunStack();
		if (gun == null) {
			viewer.closeInventory();
			viewer.sendMessage(ChatUtils.prefix("<error>You must hold the gun to edit its attachments."));
			return;
		}

		PewpewGunItem gunItem = (PewpewGunItem) itemsModule().fromItemStack(gun);
		AttachmentType type = attachment.getSlot();
		if (gunItem.getAllowedAttachmentSlots() == null || !gunItem.getAllowedAttachmentSlots().contains(type)) {
			viewer.sendMessage(ChatUtils.prefix("<error>This gun has no " + label(type) + " slot."));
			return;
		}

		if (!consumeOne(attachment.getId())) return;

		PewpewAttachment existing = AttachmentUtil.get(gun, type);
		if (existing != null) giveBack(existing);

		AttachmentUtil.set(gun, type, attachment.getId());
		GunLoreRenderer.refresh(gun);
		writeBack(gun);
		viewer.playSound(viewer.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.7f, 1.4f);
		refresh();
	}

	private void detach(AttachmentType type) {
		ItemStack gun = gunStack();
		if (gun == null) {
			viewer.closeInventory();
			viewer.sendMessage(ChatUtils.prefix("<error>You must hold the gun to edit its attachments."));
			return;
		}

		PewpewAttachment fitted = AttachmentUtil.get(gun, type);
		if (fitted == null) return;

		AttachmentUtil.clear(gun, type);
		GunLoreRenderer.refresh(gun);
		writeBack(gun);
		giveBack(fitted);
		viewer.playSound(viewer.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.7f, 1.2f);
		refresh();
	}

	private void refresh() {
		build();
		update();
	}

	private boolean consumeOne(String attachmentId) {
		ItemStack[] contents = viewer.getInventory().getStorageContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack stack = contents[i];
			if (stack == null) continue;
			PewPewItem item = itemsModule().fromItemStack(stack);
			if (item instanceof PewpewAttachment attachment && attachment.getId().equals(attachmentId)) {
				stack.setAmount(stack.getAmount() - 1);
				if (stack.getAmount() <= 0) contents[i] = null;
				viewer.getInventory().setStorageContents(contents);
				return true;
			}
		}
		return false;
	}

	private ItemStack emptyPane(AttachmentType type) {
		return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
				.setName("<color>Empty " + label(type) + " Slot")
				.addLoreLine("<dark_gray>Shift-click a " + label(type).toLowerCase() + " from")
				.addLoreLine("<dark_gray>your inventory to fit it.")
				.toItemStack();
	}

	private ItemStack filler() {
		return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("<dark_gray>—").toItemStack();
	}
}
