package gg.deepsite.pewpew.modules.weapons.lore;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.enums.AttachmentType;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.attachment.PewpewAttachment;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.ammo.AmmoUtil;
import gg.deepsite.pewpew.modules.weapons.attachment.AttachmentUtil;
import gg.deepsite.pewpew.utils.ChatUtils;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class GunLoreRenderer {

	private static final int SEGMENTS = 10;
	private static final String FILLED = "█";

	private static final double DAMAGE_MAX = 15.0;
	private static final double RANGE_MAX = 100.0;
	private static final double FIRE_RATE_REF = 2.0;

	public static void refresh(@NotNull ItemStack stack) {
		ItemsModule items = PewpewPlugin.getModuleManager().get(ItemsModule.class);
		PewPewItem item = items.fromItemStack(stack);
		if (item instanceof PewpewGunItem gun) apply(stack, gun);
	}

	public static void apply(@NotNull ItemStack stack, @NotNull PewpewGunItem gun) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return;

		List<Component> lore = new ArrayList<>();

		if (gun.getLore() != null) {
			for (String line : gun.getLore()) lore.add(line(line));
		}

		if (statDisplayEnabled()) {
			lore.add(Component.empty());
			double damage = AttachmentUtil.effectiveDamage(gun, stack);
			double range = AttachmentUtil.effectiveRange(gun, stack);
			lore.add(bar("Damage", damage / DAMAGE_MAX, format(damage)));
			lore.add(bar("Range", range / RANGE_MAX, format(range)));
			lore.add(bar("Fire Rate", FIRE_RATE_REF / Math.max(1, gun.getFireRate()),
					Math.max(1, gun.getFireRate()) + "t"));
			if (gun.getBulletCount() > 1) {
				lore.add(line("<gray>Pellets <dark_gray>┃ <color>" + gun.getBulletCount()));
			}
			if (gun.getBulletDrop() > 0) {
				lore.add(line("<gray>Bullet Drop <dark_gray>┃ <color>" + format(gun.getBulletDrop())));
			}
			if (gun.getHeadshotMultiplier() > 1.0) {
				lore.add(line("<gray>Headshot <dark_gray>┃ <color>x" + format(gun.getHeadshotMultiplier())));
			}
			int actionTicks = gun.getActionOpenTime() + gun.getActionCloseTime();
			if (actionTicks > 0) {
				lore.add(line("<gray>Action <dark_gray>┃ <color>" + actionTicks + "t"));
			}
			if (gun.getCritChance() > 0) {
				lore.add(line("<gray>Crit <dark_gray>┃ <color>" + format(gun.getCritChance() * 100) + "% x" + format(gun.getCritMultiplier())));
			}
			if (gun.getShieldDisableTime() > 0) {
				lore.add(line("<gray>Shield Break <dark_gray>┃ <color>" + gun.getShieldDisableTime() + "t"));
			}
			if (gun.getFalloffEnd() > gun.getFalloffStart()) {
				lore.add(line("<gray>Falloff <dark_gray>┃ <color>" + format(gun.getFalloffStart()) + "-"
						+ format(gun.getFalloffEnd()) + "m <dark_gray>→ <color>x" + format(gun.getFalloffMinMultiplier())));
			}
		}

		lore.add(Component.empty());
		lore.add(ammoLine(stack, gun));

		applyAmmoDurability(meta, stack, gun);

		List<AttachmentType> slots = gun.getAllowedAttachmentSlots();
		if (slots != null && !slots.isEmpty()) {
			lore.add(Component.empty());
			lore.add(line("<color>Attachments"));
			lore.add(line("<dark_gray>Press <color><key:key.swapOffhand> <dark_gray>to edit"));
			for (AttachmentType slot : slots) {
				PewpewAttachment fitted = AttachmentUtil.get(stack, slot);
				String value = fitted != null ? fitted.getName() : "<dark_gray>Empty";
				lore.add(line("<dark_gray>• <gray>" + label(slot) + ": " + value));
			}
		}

		meta.lore(lore);
		stack.setItemMeta(meta);
	}

	private static Component bar(String label, double ratio, String value) {
		double clamped = Math.max(0.0, Math.min(1.0, ratio));
		int filled = (int) Math.round(clamped * SEGMENTS);
		String color = clamped >= 0.66 ? "success" : clamped >= 0.33 ? "warning" : "error";
		String graph = "<" + color + ">" + FILLED.repeat(filled)
				+ "<dark_gray>" + FILLED.repeat(SEGMENTS - filled);
		return line("<gray>" + label + " <dark_gray>┃ " + graph + " <gray>" + value);
	}

	private static void applyAmmoDurability(ItemMeta meta, ItemStack stack, PewpewGunItem gun) {
		if (!AmmoUtil.usesAmmo(gun) || !(meta instanceof Damageable damageable)) return;
		int max = AttachmentUtil.effectiveMaxAmmo(gun, stack);
		int ammo = Math.max(0, Math.min(max, AmmoUtil.get(stack)));
		damageable.setMaxDamage(max);
		damageable.setDamage(max - ammo);
	}

	private static Component ammoLine(ItemStack stack, PewpewGunItem gun) {
		if (!AmmoUtil.usesAmmo(gun)) {
			return line("<gray>Ammo <dark_gray>┃ <color>∞");
		}
		return ChatUtils.format("<gray>Ammo <dark_gray>┃ <color>%1<gray>/%2",
				ChatUtils.PRIMARY, AmmoUtil.get(stack), AttachmentUtil.effectiveMaxAmmo(gun, stack));
	}

	private static Component line(String miniMessage) {
		return ChatUtils.format(miniMessage).decoration(TextDecoration.ITALIC, false);
	}

	private static String format(double value) {
		return value == Math.floor(value) ? String.valueOf((int) value) : String.format("%.1f", value);
	}

	private static String label(AttachmentType type) {
		String name = type.name();
		return name.charAt(0) + name.substring(1).toLowerCase();
	}

	private static boolean statDisplayEnabled() {
		return PewpewPlugin.getDefaultConfiguration() != null
				&& PewpewPlugin.getDefaultConfiguration().isStatDisplayEnabled();
	}
}
