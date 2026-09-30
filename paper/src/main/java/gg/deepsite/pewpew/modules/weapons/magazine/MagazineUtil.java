package gg.deepsite.pewpew.modules.weapons.magazine;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewpewAmmoItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.PewpewMagazineItem;
import gg.deepsite.pewpew.magazine.AmmoStack;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.ammo.AmmoUtil;
import gg.deepsite.pewpew.modules.weapons.lore.MagazineLoreRenderer;
import gg.deepsite.pewpew.utils.item.ItemFactory;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class MagazineUtil {

	public static final NamespacedKey ROUNDS_KEY = new NamespacedKey("pewpew", "mag_rounds");
	public static final NamespacedKey MAG_AMMO_KEY = new NamespacedKey("pewpew", "mag_ammo");
	public static final NamespacedKey GUN_MAG_KEY = new NamespacedKey("pewpew", "gun_mag");
	/** {@link AmmoStack} of the rounds in the magazine, kept in sync with {@link #ROUNDS_KEY}. */
	public static final NamespacedKey MAG_STACK_KEY = new NamespacedKey("pewpew", "mag_stack");

	public static boolean enabled() {
		return PewpewPlugin.getDefaultConfiguration().isMagazinesEnabled();
	}

	private static ItemsModule items() {
		return PewpewPlugin.getModuleManager().get(ItemsModule.class);
	}

	@Nullable
	public static PewpewMagazineItem defOf(@Nullable ItemStack stack) {
		if (stack == null) return null;
		return items().fromItemStack(stack) instanceof PewpewMagazineItem magazine ? magazine : null;
	}

	public static int rounds(@NotNull ItemStack magStack) {
		ItemMeta meta = magStack.getItemMeta();
		if (meta == null) return 0;
		Integer value = meta.getPersistentDataContainer().get(ROUNDS_KEY, PersistentDataType.INTEGER);
		return value != null ? Math.max(0, value) : 0;
	}

	@Nullable
	public static String ammoId(@NotNull ItemStack magStack) {
		ItemMeta meta = magStack.getItemMeta();
		if (meta == null) return null;
		return meta.getPersistentDataContainer().get(MAG_AMMO_KEY, PersistentDataType.STRING);
	}

	/** The magazine's rounds, bottom first; the last one comes out next. Old single-ammo magazines read as one id. */
	@NotNull
	public static List<String> stack(@NotNull ItemStack magStack) {
		ItemMeta meta = magStack.getItemMeta();
		String encoded = meta == null ? null : meta.getPersistentDataContainer().get(MAG_STACK_KEY, PersistentDataType.STRING);
		String next = ammoId(magStack);
		List<String> ids = encoded == null ? AmmoStack.legacy(next, rounds(magStack)) : AmmoStack.decode(encoded);
		return AmmoUtil.fit(ids, rounds(magStack), next);
	}

	public static void write(@NotNull ItemStack magStack, @NotNull PewpewMagazineItem def, int rounds,
	                         @NotNull List<String> ids) {
		ItemMeta meta = magStack.getItemMeta();
		if (meta == null) return;
		int clamped = Math.max(0, Math.min(def.getCapacity(), rounds));
		AmmoUtil.fit(ids, clamped, null);
		String encoded = AmmoStack.encode(ids);
		meta.getPersistentDataContainer().set(ROUNDS_KEY, PersistentDataType.INTEGER, clamped);
		if (encoded == null) {
			meta.getPersistentDataContainer().remove(MAG_STACK_KEY);
			meta.getPersistentDataContainer().remove(MAG_AMMO_KEY);
		} else {
			meta.getPersistentDataContainer().set(MAG_STACK_KEY, PersistentDataType.STRING, encoded);
			meta.getPersistentDataContainer().set(MAG_AMMO_KEY, PersistentDataType.STRING, ids.get(ids.size() - 1));
		}
		magStack.setItemMeta(meta);
		MagazineLoreRenderer.apply(magStack, def);
	}

	@Nullable
	public static String insertedId(@NotNull ItemStack gunStack) {
		ItemMeta meta = gunStack.getItemMeta();
		if (meta == null) return null;
		return meta.getPersistentDataContainer().get(GUN_MAG_KEY, PersistentDataType.STRING);
	}

	@Nullable
	public static PewpewMagazineItem inserted(@NotNull ItemStack gunStack) {
		String id = insertedId(gunStack);
		if (id == null) return null;
		return items().get(id) instanceof PewpewMagazineItem magazine ? magazine : null;
	}

	private static void setInserted(@NotNull ItemStack gunStack, @Nullable String magId) {
		ItemMeta meta = gunStack.getItemMeta();
		if (meta == null) return;
		if (magId == null) meta.getPersistentDataContainer().remove(GUN_MAG_KEY);
		else meta.getPersistentDataContainer().set(GUN_MAG_KEY, PersistentDataType.STRING, magId);
		gunStack.setItemMeta(meta);
	}

	public static int load(@NotNull ItemStack magStack, @NotNull PewpewMagazineItem def,
	                       @NotNull ItemStack ammoStack, @NotNull PewpewAmmoItem ammo) {
		int rounds = rounds(magStack);
		if (rounds >= def.getCapacity()) return 0;

		int perItem = ammo.getRoundsPerItem() <= 0 ? def.getCapacity() : ammo.getRoundsPerItem();
		int consumed = itemsToFill(rounds, def.getCapacity(), perItem, ammoStack.getAmount());
		if (consumed <= 0) return 0;

		int filled = Math.min(def.getCapacity(), rounds + consumed * perItem);
		List<String> ids = stack(magStack);
		ids.addAll(Collections.nCopies(filled - rounds, ammo.getId()));
		ammoStack.setAmount(ammoStack.getAmount() - consumed);
		write(magStack, def, filled, ids);
		return consumed;
	}

	public static int itemsToFill(int rounds, int capacity, int perItem, int available) {
		int consumed = 0;
		while (consumed < available && rounds < capacity) {
			rounds = Math.min(capacity, rounds + perItem);
			consumed++;
		}
		return consumed;
	}

	public static int findBetter(@NotNull Inventory inventory, @NotNull PewpewGunItem gun,
	                             @NotNull ItemStack gunStack) {
		int best = -1;
		int bestRounds = inserted(gunStack) == null ? 0 : AmmoUtil.pool(gunStack);
		ItemStack[] contents = inventory.getStorageContents();
		for (int i = 0; i < contents.length; i++) {
			PewpewMagazineItem def = defOf(contents[i]);
			if (def == null || !def.getAmmoType().equals(gun.getAmmoType())
					|| !gun.acceptsMagazine(def.getId())) continue;
			int rounds = rounds(contents[i]);
			if (rounds > bestRounds) {
				bestRounds = rounds;
				best = i;
			}
		}
		return best;
	}

	public static boolean swap(@NotNull Player player, @NotNull ItemStack gunStack, @NotNull PewpewGunItem gun) {
		Inventory inventory = player.getInventory();
		int slot = findBetter(inventory, gun, gunStack);
		if (slot < 0) return false;

		ItemStack[] contents = inventory.getStorageContents();
		ItemStack found = contents[slot];
		PewpewMagazineItem incoming = defOf(found);
		if (incoming == null) return false;

		ItemStack taken = found.clone();
		taken.setAmount(1);
		if (found.getAmount() > 1) found.setAmount(found.getAmount() - 1);
		else contents[slot] = null;
		inventory.setStorageContents(contents);

		eject(player, gunStack);

		List<String> loaded = stack(taken);
		loaded.addAll(AmmoUtil.rounds(gunStack)); // the chambered round stays on top
		setInserted(gunStack, incoming.getId());
		if (ammoId(taken) != null) AmmoUtil.setLoadedAmmo(gunStack, ammoId(taken));
		AmmoUtil.set(gunStack, rounds(taken) + AmmoUtil.chamber(gunStack));
		AmmoUtil.setRounds(gunStack, loaded);
		return true;
	}

	public static void eject(@NotNull Player player, @NotNull ItemStack gunStack) {
		PewpewMagazineItem old = inserted(gunStack);
		if (old == null) return;

		List<String> rounds = AmmoUtil.rounds(gunStack);
		int chambered = Math.min(AmmoUtil.chamber(gunStack), rounds.size());
		List<String> chamber = new ArrayList<>(rounds.subList(rounds.size() - chambered, rounds.size()));
		rounds.subList(rounds.size() - chambered, rounds.size()).clear();

		ItemStack ejected = ItemFactory.build(old);
		write(ejected, old, AmmoUtil.pool(gunStack), rounds);
		for (ItemStack leftover : player.getInventory().addItem(ejected).values()) {
			player.getWorld().dropItemNaturally(player.getLocation(), leftover);
		}
		setInserted(gunStack, null);
		AmmoUtil.set(gunStack, AmmoUtil.chamber(gunStack));
		AmmoUtil.setRounds(gunStack, chamber);
	}
}
