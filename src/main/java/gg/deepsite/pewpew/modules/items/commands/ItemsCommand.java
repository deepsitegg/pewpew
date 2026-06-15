package gg.deepsite.pewpew.modules.items.commands;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewAmmoItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.PewpewThrowableItem;
import gg.deepsite.pewpew.api.objects.attachment.PewpewAttachment;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.CooldownUtil;
import gg.deepsite.pewpew.modules.weapons.ammo.AmmoUtil;
import gg.deepsite.pewpew.modules.weapons.lore.GunLoreRenderer;
import gg.deepsite.pewpew.utils.ChatUtils;
import gg.deepsite.pewpew.utils.item.ItemFactory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ItemsCommand {

	private static ItemsModule module() {
		return PewpewPlugin.getModuleManager().get(ItemsModule.class);
	}

	private static String typeOf(PewPewItem item) {
		if (item instanceof PewpewGunItem) return "GUN";
		if (item instanceof PewpewThrowableItem) return "THROWABLE";
		if (item instanceof PewpewAttachment) return "ATTACHMENT";
		if (item instanceof PewpewAmmoItem) return "AMMO";
		return "ITEM";
	}

	public static void list(CommandSender sender) {
		var all = module().getAll();
		if (all.isEmpty()) {
			sender.sendMessage(ChatUtils.prefix("<warning>No items are registered."));
			return;
		}
		sender.sendMessage(ChatUtils.prefix("<color>Registered items (" + all.size() + "):</color>"));
		for (PewPewItem item : all) {
			sender.sendMessage(ChatUtils.format(
					"  <dark_gray>- <gray>" + item.getId()
					+ " <dark_gray>[<primary>" + typeOf(item) + "<dark_gray>]"
					+ " <dark_gray>| <gray>" + item.getName()
			));
		}
	}

	public static void give(Player sender, String id, Integer amount) {
		int count = Math.max(1, amount != null ? amount : 1);
		ItemStack stack = createItem(id, count);
		if (stack == null) {
			sender.sendMessage(ChatUtils.prefix("<error>Unknown item id: <gray>" + id));
			return;
		}

		sender.getInventory().addItem(stack);
		sender.sendMessage(ChatUtils.prefix("<success>Given <gray>" + count + "x " + id + "<success>."));
	}

	/**
	 * Builds a ready-to-give ItemStack for the registered item with the given id,
	 * fully initialised (ammo, cooldown, lore). Returns {@code null} for unknown ids.
	 */
	@org.jetbrains.annotations.Nullable
	public static ItemStack createItem(String id, int amount) {
		PewPewItem item = module().get(id);
		if (item == null) return null;

		ItemStack stack = ItemFactory.build(item);
		if (item instanceof PewpewGunItem gun) {
			AmmoUtil.init(stack, gun);
			CooldownUtil.stamp(stack, gun);
			GunLoreRenderer.apply(stack, gun);
		}
		if (item instanceof PewpewAmmoItem ammo) AmmoUtil.stampAmmo(stack, ammo.getAmmoType(), ammo.getRoundsPerItem());
		stack.setAmount(Math.max(1, amount));
		return stack;
	}
}
