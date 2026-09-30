package gg.deepsite.pewpew.modules.weapons.ammo;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewpewAmmoItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.magazine.AmmoStack;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.utils.PersistentDataUtil;
import gg.deepsite.pewpew.modules.weapons.attachment.AttachmentUtil;
import gg.deepsite.pewpew.modules.weapons.magazine.MagazineUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@UtilityClass
public class AmmoUtil {

	public static final NamespacedKey AMMO_KEY = new NamespacedKey("pewpew", "ammo");
	public static final NamespacedKey AMMO_TYPE_KEY = new NamespacedKey("pewpew", "ammo_type");
	public static final NamespacedKey AMMO_ROUNDS_KEY = new NamespacedKey("pewpew", "ammo_rounds");
	public static final NamespacedKey LOADED_AMMO_KEY = new NamespacedKey("pewpew", "loaded_ammo");
	public static final NamespacedKey CHAMBER_KEY = new NamespacedKey("pewpew", "chamber");
	/** {@link AmmoStack} of every round in the gun, chamber on top, so mixed magazines fire round by round. */
	public static final NamespacedKey GUN_STACK_KEY = new NamespacedKey("pewpew", "gun_stack");

	public static boolean usesAmmo(@NotNull PewpewGunItem gun) {
		return gun.getMaxAmmo() > 0;
	}

	public static boolean has(@NotNull ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		return meta != null && meta.getPersistentDataContainer().has(AMMO_KEY, PersistentDataType.INTEGER);
	}

	public static int get(@NotNull ItemStack stack) {
		return pool(stack) + chamber(stack);
	}

	public static int pool(@NotNull ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return 0;
		Integer value = meta.getPersistentDataContainer().get(AMMO_KEY, PersistentDataType.INTEGER);
		return value != null ? Math.max(0, value) : 0;
	}

	public static int chamber(@NotNull ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return 0;
		Integer value = meta.getPersistentDataContainer().get(CHAMBER_KEY, PersistentDataType.INTEGER);
		return value != null && value > 0 ? 1 : 0;
	}

	public static void set(@NotNull ItemStack stack, int rounds) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return;
		int total = Math.max(0, rounds);
		int chambered = MagazineUtil.enabled() ? Math.min(1, total) : 0;
		meta.getPersistentDataContainer().set(CHAMBER_KEY, PersistentDataType.INTEGER, chambered);
		meta.getPersistentDataContainer().set(AMMO_KEY, PersistentDataType.INTEGER, total - chambered);
		stack.setItemMeta(meta);
	}

	public static void init(@NotNull ItemStack stack, @NotNull PewpewGunItem gun) {
		if (!usesAmmo(gun) || has(stack)) return;
		boolean startEmpty = MagazineUtil.enabled() && gun.isConsumesAmmo();
		set(stack, startEmpty ? 0 : AttachmentUtil.effectiveMaxAmmo(gun, stack));
	}

	public static void stampAmmo(@NotNull ItemStack stack, @NotNull String ammoType, int roundsPerItem) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return;
		meta.getPersistentDataContainer().set(AMMO_TYPE_KEY, PersistentDataType.STRING, ammoType);
		meta.getPersistentDataContainer().set(AMMO_ROUNDS_KEY, PersistentDataType.INTEGER, roundsPerItem);
		stack.setItemMeta(meta);
	}

	@Nullable
	public static String ammoIdOf(@NotNull ItemStack stack) {
		return PersistentDataUtil.getPewpew(stack, ItemsModule.PDC_KEY);
	}

	@Nullable
	public static PewpewAmmoItem statsOf(@NotNull ItemStack gunStack) {
		if (!PewpewPlugin.getDefaultConfiguration().isAmmoStatsEnabled()) return null;

		String id = loadedAmmo(gunStack);
		if (id == null) return null;
		return PewpewPlugin.getModuleManager().get(ItemsModule.class).get(id) instanceof PewpewAmmoItem ammo
				? ammo : null;
	}

	public static double damageMultiplier(@Nullable PewpewAmmoItem ammo) {
		return ammo == null || ammo.getDamageMultiplier() <= 0 ? 1.0 : ammo.getDamageMultiplier();
	}

	public static double velocityMultiplier(@Nullable PewpewAmmoItem ammo) {
		return ammo == null || ammo.getVelocityMultiplier() <= 0 ? 1.0 : ammo.getVelocityMultiplier();
	}

	public static int penetration(@Nullable PewpewAmmoItem ammo) {
		return ammo == null ? 0 : Math.max(0, ammo.getPenetration());
	}

	public static int fireTicks(@Nullable PewpewAmmoItem ammo) {
		return ammo == null ? 0 : Math.max(0, ammo.getFireTicks());
	}

	/**
	 * Trims or pads a round list to {@code count}, working from the bottom so the next rounds stay intact. Padding
	 * uses {@code fallback}, which is how guns and magazines from before mixed ammo read: all one id.
	 */
	@NotNull
	public static List<String> fit(@NotNull List<String> rounds, int count, @Nullable String fallback) {
		while (rounds.size() > count) rounds.remove(0);
		if (fallback != null && rounds.size() < count) rounds.addAll(0, Collections.nCopies(count - rounds.size(), fallback));
		return rounds;
	}

	/** Every round in the gun, bottom first; the last one is chambered and fires next. */
	@NotNull
	public static List<String> rounds(@NotNull ItemStack gunStack) {
		return rounds(gunStack, get(gunStack));
	}

	@NotNull
	private static List<String> rounds(@NotNull ItemStack gunStack, int count) {
		ItemMeta meta = gunStack.getItemMeta();
		String encoded = meta == null ? null : meta.getPersistentDataContainer().get(GUN_STACK_KEY, PersistentDataType.STRING);
		return fit(AmmoStack.decode(encoded), count, loadedAmmo(gunStack));
	}

	public static void setRounds(@NotNull ItemStack gunStack, @NotNull List<String> rounds) {
		ItemMeta meta = gunStack.getItemMeta();
		if (meta == null) return;
		String encoded = AmmoStack.encode(rounds);
		if (encoded == null) meta.getPersistentDataContainer().remove(GUN_STACK_KEY);
		else meta.getPersistentDataContainer().set(GUN_STACK_KEY, PersistentDataType.STRING, encoded);
		gunStack.setItemMeta(meta);
	}

	/** Takes the next round off the gun's stack and makes it the loaded ammo, so the shot uses that round's stats. */
	public static void popRound(@NotNull ItemStack gunStack) {
		List<String> rounds = rounds(gunStack);
		if (rounds.isEmpty()) return;
		String next = rounds.remove(rounds.size() - 1);
		setRounds(gunStack, rounds);
		setLoadedAmmo(gunStack, next);
	}

	private static void pushRounds(@Nullable ItemStack gunStack, @Nullable String ammoId, int before, int after) {
		if (gunStack == null || ammoId == null || after <= before) return;
		List<String> rounds = rounds(gunStack, before);
		rounds.addAll(Collections.nCopies(after - before, ammoId));
		setRounds(gunStack, rounds);
	}

	@Nullable
	public static String loadedAmmo(@NotNull ItemStack gunStack) {
		ItemMeta meta = gunStack.getItemMeta();
		if (meta == null) return null;
		return meta.getPersistentDataContainer().get(LOADED_AMMO_KEY, PersistentDataType.STRING);
	}

	public static void setLoadedAmmo(@NotNull ItemStack gunStack, @Nullable String ammoId) {
		ItemMeta meta = gunStack.getItemMeta();
		if (meta == null) return;
		if (ammoId == null) meta.getPersistentDataContainer().remove(LOADED_AMMO_KEY);
		else meta.getPersistentDataContainer().set(LOADED_AMMO_KEY, PersistentDataType.STRING, ammoId);
		gunStack.setItemMeta(meta);
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

	public static int loadMagazine(@NotNull Inventory inventory, @NotNull String ammoType, int current, int maxAmmo,
	                               @Nullable ItemStack gunStack) {
		int ammo = current;
		ItemStack[] contents = inventory.getStorageContents();
		for (int i = 0; i < contents.length && ammo < maxAmmo; i++) {
			ItemStack stack = contents[i];
			if (stack == null || !ammoType.equals(ammoTypeOf(stack))) continue;

			int perItem = roundsPerItem(stack, maxAmmo);
			String id = ammoIdOf(stack);
			int before = ammo;
			while (stack.getAmount() > 0 && ammo < maxAmmo) {
				ammo = Math.min(maxAmmo, ammo + perItem);
				stack.setAmount(stack.getAmount() - 1);
			}
			pushRounds(gunStack, id, before, ammo);
			if (gunStack != null && ammo > before) setLoadedAmmo(gunStack, id);
			if (stack.getAmount() <= 0) contents[i] = null;
		}
		inventory.setStorageContents(contents);
		return ammo;
	}

	public static int loadOneItem(@NotNull Inventory inventory, @NotNull String ammoType, int current, int maxAmmo,
	                              @Nullable ItemStack gunStack) {
		ItemStack[] contents = inventory.getStorageContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack stack = contents[i];
			if (stack == null || !ammoType.equals(ammoTypeOf(stack))) continue;

			int gained = Math.min(maxAmmo, current + roundsPerItem(stack, maxAmmo));
			String id = ammoIdOf(stack);
			pushRounds(gunStack, id, current, gained);
			if (gunStack != null) setLoadedAmmo(gunStack, id);
			stack.setAmount(stack.getAmount() - 1);
			if (stack.getAmount() <= 0) contents[i] = null;
			inventory.setStorageContents(contents);
			return gained;
		}
		return current;
	}
}
