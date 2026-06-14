package gg.deepsite.pewpew.modules.weapons.attachment;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.enums.AttachmentType;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.attachment.PewpewAttachment;
import gg.deepsite.pewpew.api.objects.attachment.PewpewBarrelAttachment;
import gg.deepsite.pewpew.api.objects.attachment.PewpewGripAttachment;
import gg.deepsite.pewpew.api.objects.attachment.PewpewMagazineAttachment;
import gg.deepsite.pewpew.api.objects.attachment.PewpewScopeAttachment;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class AttachmentUtil {

	private static NamespacedKey key(AttachmentType slot) {
		return new NamespacedKey("pewpew", "attachment_" + slot.name().toLowerCase());
	}

	private static ItemsModule items() {
		return PewpewPlugin.getModuleManager().get(ItemsModule.class);
	}

	public static void set(@NotNull ItemStack gun, @NotNull AttachmentType slot, @NotNull String attachmentId) {
		ItemMeta meta = gun.getItemMeta();
		if (meta == null) return;
		meta.getPersistentDataContainer().set(key(slot), PersistentDataType.STRING, attachmentId);
		gun.setItemMeta(meta);
	}

	public static void clear(@NotNull ItemStack gun, @NotNull AttachmentType slot) {
		ItemMeta meta = gun.getItemMeta();
		if (meta == null) return;
		meta.getPersistentDataContainer().remove(key(slot));
		gun.setItemMeta(meta);
	}

	@Nullable
	public static String getId(@NotNull ItemStack gun, @NotNull AttachmentType slot) {
		ItemMeta meta = gun.getItemMeta();
		if (meta == null) return null;
		return meta.getPersistentDataContainer().get(key(slot), PersistentDataType.STRING);
	}

	@Nullable
	public static PewpewAttachment get(@NotNull ItemStack gun, @NotNull AttachmentType slot) {
		String id = getId(gun, slot);
		if (id == null) return null;
		PewPewItem item = items().get(id);
		if (item instanceof PewpewAttachment attachment && attachment.getSlot() == slot) {
			return attachment;
		}
		return null;
	}

	public static double effectiveDamage(@NotNull PewpewGunItem gun, @NotNull ItemStack stack) {
		double damage = gun.getBaseDamage();
		if (get(stack, AttachmentType.BARREL) instanceof PewpewBarrelAttachment barrel) {
			damage *= barrel.getDamageModifier();
		}
		return damage;
	}

	public static double effectiveRange(@NotNull PewpewGunItem gun, @NotNull ItemStack stack) {
		double range = gun.getRange();
		if (get(stack, AttachmentType.BARREL) instanceof PewpewBarrelAttachment barrel) {
			range *= barrel.getRangeModifier();
		}
		return range;
	}

	public static int effectiveMaxAmmo(@NotNull PewpewGunItem gun, @NotNull ItemStack stack) {
		int max = gun.getMaxAmmo();
		if (get(stack, AttachmentType.MAGAZINE) instanceof PewpewMagazineAttachment magazine) {
			max += magazine.getAmmoBonus();
		}
		return Math.max(1, max);
	}

	public static int effectiveReloadTime(@NotNull PewpewGunItem gun, @NotNull ItemStack stack) {
		double time = gun.getReloadTime();
		if (get(stack, AttachmentType.MAGAZINE) instanceof PewpewMagazineAttachment magazine
				&& magazine.getReloadModifier() > 0) {
			time *= magazine.getReloadModifier();
		}
		return Math.max(1, (int) Math.round(time));
	}

	public static double recoilMultiplier(@NotNull ItemStack stack) {
		if (get(stack, AttachmentType.GRIP) instanceof PewpewGripAttachment grip) {
			return grip.getRecoilModifier();
		}
		return 1.0;
	}

	public static double aimSpreadMultiplier(@NotNull ItemStack stack) {
		if (get(stack, AttachmentType.SCOPE) instanceof PewpewScopeAttachment scope) {
			return scope.getAimSpreadMultiplier();
		}
		return 0.0;
	}

	public static double aimRecoilMultiplier(@NotNull ItemStack stack) {
		if (get(stack, AttachmentType.SCOPE) instanceof PewpewScopeAttachment scope) {
			return scope.getAimRecoilMultiplier();
		}
		return 0.0;
	}
}
