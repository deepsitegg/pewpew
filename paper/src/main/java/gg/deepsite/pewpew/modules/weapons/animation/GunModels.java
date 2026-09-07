package gg.deepsite.pewpew.modules.weapons.animation;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.PewpewMagazineItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.WeaponsModule;
import gg.deepsite.pewpew.modules.weapons.magazine.MagazineUtil;
import gg.deepsite.pewpew.modules.weapons.shooting.ScopeState;
import gg.deepsite.pewpew.utils.PersistentDataUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class GunModels {

	@NotNull
	public static String compose(@NotNull String base, @Nullable String magazineSuffix, @Nullable String aimSuffix,
	                             @Nullable String frame) {
		if (frame != null && !frame.startsWith("_")) return frame;
		StringBuilder model = new StringBuilder(base);
		if (magazineSuffix != null) model.append(magazineSuffix);
		if (aimSuffix != null) model.append(aimSuffix);
		if (frame != null) model.append(frame);
		return model.toString();
	}

	public static int composeData(int base, int magazineData, int aimData, int frameData) {
		return base + magazineData + aimData + frameData;
	}

	@NotNull
	public static String resolve(@NotNull PewpewGunItem gun, @NotNull ItemStack gunStack, boolean scoped,
	                             @Nullable String frame) {
		return compose(gun.getItemModel(), magazineSuffix(gunStack), scoped ? gun.getAimModelSuffix() : null, frame);
	}

	public static int resolveData(@NotNull PewpewGunItem gun, @NotNull ItemStack gunStack, boolean scoped,
	                              int frameData) {
		return composeData(gun.getCustomModelData(), magazineData(gunStack),
				scoped ? gun.getAimModelData() : 0, frameData);
	}

	public static void apply(@NotNull ItemStack gunStack, @NotNull PewpewGunItem gun, boolean scoped,
	                         @Nullable String frame, int frameData) {
		NamespacedKey key = NamespacedKey.fromString(resolve(gun, gunStack, scoped, frame));
		int data = resolveData(gun, gunStack, scoped, frameData);
		gunStack.editMeta(meta -> {
			if (key != null) meta.setItemModel(key);
			meta.setCustomModelData(data == 0 ? null : data);
		});
	}

	@Nullable
	private static String magazineSuffix(@NotNull ItemStack gunStack) {
		PewpewMagazineItem magazine = MagazineUtil.enabled() ? MagazineUtil.inserted(gunStack) : null;
		return magazine == null ? null : magazine.getModelSuffix();
	}

	private static int magazineData(@NotNull ItemStack gunStack) {
		PewpewMagazineItem magazine = MagazineUtil.enabled() ? MagazineUtil.inserted(gunStack) : null;
		return magazine == null ? 0 : magazine.getGunModelData();
	}

	public static void refresh(@NotNull Player player) {
		ItemStack held = player.getInventory().getItemInMainHand();
		if (held.getType().isAir()) return;

		String id = PersistentDataUtil.getPewpew(held, ItemsModule.PDC_KEY);
		if (id == null) return;
		if (!(PewpewPlugin.getModuleManager().get(ItemsModule.class).get(id) instanceof PewpewGunItem gun)) return;

		WeaponsModule weapons = PewpewPlugin.getModuleManager().get(WeaponsModule.class);
		if (weapons != null && weapons.getAnimationManager().isActive(player.getUniqueId())) return;

		apply(held, gun, ScopeState.isScoped(player), null, 0);
		player.getInventory().setItemInMainHand(held);
	}
}
