package gg.deepsite.pewpew.modules.weapons.attachment.menu;

import com.jazzkuh.inventorylib.objects.Menu;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.enums.AttachmentType;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.utils.item.ItemFactory;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

abstract class AbstractGunMenu extends Menu {

	protected final Player viewer;
	protected final int gunSlot;

	protected AbstractGunMenu(Component title, InventoryType type, Player viewer, int gunSlot) {
		super(title, 1, type);
		this.viewer = viewer;
		this.gunSlot = gunSlot;
	}

	protected static ItemsModule itemsModule() {
		return PewpewPlugin.getModuleManager().get(ItemsModule.class);
	}

	protected ItemStack gunStack() {
		ItemStack stack = viewer.getInventory().getItem(gunSlot);
		if (stack == null) return null;
		PewPewItem item = itemsModule().fromItemStack(stack);
		return item instanceof PewpewGunItem ? stack : null;
	}

	protected void writeBack(ItemStack gun) {
		viewer.getInventory().setItem(gunSlot, gun);
	}

	protected void giveBack(PewPewItem attachment) {
		ItemStack stack = ItemFactory.build(attachment);
		viewer.getInventory().addItem(stack).values()
				.forEach(left -> viewer.getWorld().dropItem(viewer.getLocation(), left));
	}

	protected static String label(AttachmentType type) {
		String name = type.name();
		return name.charAt(0) + name.substring(1).toLowerCase();
	}
}
