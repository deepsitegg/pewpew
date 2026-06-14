package gg.deepsite.pewpew.modules.weapons.ammo;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.modules.weapons.attachment.AttachmentUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class AmmoUtil {

	public static final NamespacedKey AMMO_KEY = new NamespacedKey("pewpew", "ammo");
	public static final NamespacedKey AMMO_TYPE_KEY = new NamespacedKey("pewpew", "ammo_type");
	public static final NamespacedKey AMMO_ROUNDS_KEY = new NamespacedKey("pewpew", "ammo_rounds");

	public static boolean usesAmmo(@NotNull PewpewGunItem gun) {
		return gun.getMaxAmmo() > 0;
	}

	public static boolean has(@NotNull ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		return meta != null && meta.getPersistentDataContainer().has(AMMO_KEY, PersistentDataType.INTEGER);
	}

	public static int get(@NotNull ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return 0;
		Integer value = meta.getPersistentDataContainer().get(AMMO_KEY, PersistentDataType.INTEGER);
		return value != null ? value : 0;
	}

	public static void set(@NotNull ItemStack stack, int rounds) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return;
		meta.getPersistentDataContainer().set(AMMO_KEY, PersistentDataType.INTEGER, Math.max(0, rounds));
		stack.setItemMeta(meta);
	}

	public static void init(@NotNull ItemStack stack, @NotNull PewpewGunItem gun) {
		if (usesAmmo(gun) && !has(stack)) {
			set(stack, AttachmentUtil.effectiveMaxAmmo(gun, stack));
		}
	}

	public static void stampAmmo(@NotNull ItemStack stack, @NotNull String ammoType, int roundsPerItem) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return;
		meta.getPersistentDataContainer().set(AMMO_TYPE_KEY, PersistentDataType.STRING, ammoType);
		meta.getPersistentDataContainer().set(AMMO_ROUNDS_KEY, PersistentDataType.INTEGER, roundsPerItem);
		stack.setItemMeta(meta);
	}

	@Nullable
	public static String ammoTypeOf(@NotNull ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return null;
		return meta.getPersistentDataContainer().get(AMMO_TYPE_KEY, PersistentDataType.STRING);
	}

	public static int roundsPerItem(@NotNull ItemStack stack, int maxAmmo) {
		ItemMeta meta = stack.getItemMeta();
		Integer stored = meta == null ? null
				: meta.getPersistentDataContainer().get(AMMO_ROUNDS_KEY, PersistentDataType.INTEGER);
		int rounds = stored != null ? stored : 1;
		return rounds <= 0 ? maxAmmo : rounds;
	}

	public static int countInInventory(@NotNull Inventory inventory, @NotNull String ammoType) {
		int total = 0;
		for (ItemStack stack : inventory.getStorageContents()) {
			if (stack != null && ammoType.equals(ammoTypeOf(stack))) total += stack.getAmount();
		}
		return total;
	}

	public static int loadMagazine(@NotNull Inventory inventory, @NotNull String ammoType, int current, int maxAmmo) {
		int ammo = current;
		ItemStack[] contents = inventory.getStorageContents();
		for (int i = 0; i < contents.length && ammo < maxAmmo; i++) {
			ItemStack stack = contents[i];
			if (stack == null || !ammoType.equals(ammoTypeOf(stack))) continue;

			int perItem = roundsPerItem(stack, maxAmmo);
			while (stack.getAmount() > 0 && ammo < maxAmmo) {
				ammo = Math.min(maxAmmo, ammo + perItem);
				stack.setAmount(stack.getAmount() - 1);
			}
			if (stack.getAmount() <= 0) contents[i] = null;
		}
		inventory.setStorageContents(contents);
		return ammo;
	}

	public static int loadOneItem(@NotNull Inventory inventory, @NotNull String ammoType, int current, int maxAmmo) {
		ItemStack[] contents = inventory.getStorageContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack stack = contents[i];
			if (stack == null || !ammoType.equals(ammoTypeOf(stack))) continue;

			int gained = Math.min(maxAmmo, current + roundsPerItem(stack, maxAmmo));
			stack.setAmount(stack.getAmount() - 1);
			if (stack.getAmount() <= 0) contents[i] = null;
			inventory.setStorageContents(contents);
			return gained;
		}
		return current;
	}
}
