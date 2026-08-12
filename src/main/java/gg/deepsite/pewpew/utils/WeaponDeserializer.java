package gg.deepsite.pewpew.utils;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.enums.*;
import gg.deepsite.pewpew.api.objects.*;
import gg.deepsite.pewpew.api.objects.attachment.*;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@UtilityClass
public class WeaponDeserializer {

	@NotNull
	public static List<PewPewItem> deserializeAll(String fileName, ConfigurationNode root) {
		List<PewPewItem> result = new ArrayList<>();
		for (var entry : root.childrenMap().entrySet()) {
			String id = entry.getKey().toString();
			PewPewItem item = deserializeOne(fileName, id, entry.getValue());
			if (item != null) result.add(item);
		}
		return result;
	}

	@Nullable
	private static PewPewItem deserializeOne(String fileName, String id, ConfigurationNode node) {
		String typeRaw = node.node("type").getString();
		if (typeRaw == null) {
			warn(fileName, id, "missing required field 'type'");
			return null;
		}
		ItemType type;
		try {
			type = ItemType.valueOf(typeRaw.toUpperCase());
		} catch (IllegalArgumentException e) {
			warn(fileName, id, "unknown type '" + typeRaw + "'");
			return null;
		}

		String name = node.node("name").getString();
		if (name == null) {
			warn(fileName, id, "missing required field 'name'");
			return null;
		}
		String itemModel = node.node("itemModel").getString();
		if (itemModel == null) {
			warn(fileName, id, "missing required field 'itemModel'");
			return null;
		}

		List<String> lore = new ArrayList<>();
		for (ConfigurationNode loreNode : node.node("lore").childrenList()) {
			String line = loreNode.getString();
			if (line != null) lore.add(line);
		}

		boolean hideItemFlags = node.node("hideItemFlags").getBoolean(false);
		int customModelData = node.node("customModelData").getInt(0);
		int maxStack = node.node("maxStack").getInt(0);

		PewPewItem item = switch (type) {
			case GUN -> deserializeGun(fileName, id, node, name, lore, hideItemFlags, customModelData, itemModel);
			case THROWABLE ->
					deserializeThrowable(fileName, id, node, name, lore, hideItemFlags, customModelData, itemModel);
			case ATTACHMENT ->
					deserializeAttachment(fileName, id, node, name, lore, hideItemFlags, customModelData, itemModel);
			case AMMO -> deserializeAmmo(fileName, id, node, name, lore, hideItemFlags, customModelData, itemModel);
		};
		if (item != null) item.setMaxStack(maxStack);
		return item;
	}

	@Nullable
	private static PewpewAmmoItem deserializeAmmo(String fileName, String id, ConfigurationNode node,
	                                              String name, List<String> lore, boolean hideItemFlags,
	                                              int customModelData, String itemModel) {
		String ammoType = node.node("ammoType").getString();
		if (ammoType == null) {
			warn(fileName, id, "missing required field 'ammoType'");
			return null;
		}

		int roundsPerItem = node.node("roundsPerItem").getInt(1);

		return PewpewAmmoItem.builder()
				.id(id)
				.name(name)
				.lore(lore)
				.hideItemFlags(hideItemFlags)
				.customModelData(customModelData)
				.itemModel(itemModel)
				.ammoType(ammoType)
				.roundsPerItem(roundsPerItem)
				.build();
	}

	@Nullable
	private static PewpewGunItem deserializeGun(String fileName, String id, ConfigurationNode node,
	                                            String name, List<String> lore, boolean hideItemFlags,
	                                            int customModelData, String itemModel) {
		double baseDamage = node.node("baseDamage").getDouble(Double.MIN_VALUE);
		if (baseDamage == Double.MIN_VALUE) {
			warn(fileName, id, "missing required field 'baseDamage'");
			return null;
		}

		int fireRate = node.node("fireRate").getInt(Integer.MIN_VALUE);
		if (fireRate == Integer.MIN_VALUE) {
			warn(fileName, id, "missing required field 'fireRate'");
			return null;
		}

		int reloadTime = node.node("reloadTime").getInt(Integer.MIN_VALUE);
		if (reloadTime == Integer.MIN_VALUE) {
			warn(fileName, id, "missing required field 'reloadTime'");
			return null;
		}

		int maxAmmo = node.node("maxAmmo").getInt(Integer.MIN_VALUE);
		if (maxAmmo == Integer.MIN_VALUE) {
			warn(fileName, id, "missing required field 'maxAmmo'");
			return null;
		}

		double range = node.node("range").getDouble(Double.MIN_VALUE);
		if (range == Double.MIN_VALUE) {
			warn(fileName, id, "missing required field 'range'");
			return null;
		}

		String firingModeRaw = node.node("firingMode").getString();
		if (firingModeRaw == null) {
			warn(fileName, id, "missing required field 'firingMode'");
			return null;
		}
		FiringMode firingMode;
		try {
			firingMode = FiringMode.valueOf(firingModeRaw.toUpperCase());
		} catch (IllegalArgumentException e) {
			warn(fileName, id, "unknown firingMode '" + firingModeRaw + "'");
			return null;
		}

		double projectileSpeed = 0.0;
		if (firingMode == FiringMode.PROJECTILE) {
			if (node.node("projectileSpeed").virtual()) {
				warn(fileName, id, "firingMode is PROJECTILE but 'projectileSpeed' is missing");
				return null;
			}
			projectileSpeed = node.node("projectileSpeed").getDouble(0.0);
		}

		org.bukkit.Material projectileModel = null;
		String projectileModelRaw = node.node("projectileModel").getString();
		if (projectileModelRaw != null && !projectileModelRaw.isBlank()) {
			projectileModel = org.bukkit.Material.matchMaterial(projectileModelRaw.trim());
			if (projectileModel == null || !projectileModel.isItem()) {
				warn(fileName, id, "invalid projectileModel '" + projectileModelRaw + "', using default snowball");
				projectileModel = null;
			}
		}

		Trajectory trajectory = null;
		String trajectoryRaw = node.node("trajectory").getString();
		if (trajectoryRaw != null && !trajectoryRaw.isBlank()) {
			try {
				trajectory = Trajectory.valueOf(trajectoryRaw.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				warn(fileName, id, "unknown trajectory '" + trajectoryRaw + "', ignoring");
			}
		}

		ExplosiveConfig explosive = null;
		ConfigurationNode explosiveNode = node.node("explosive");
		if (!explosiveNode.virtual()) {
			ConfigurationNode rebuild = explosiveNode.node("rebuild");
			explosive = new ExplosiveConfig(
					Math.max(0.0, explosiveNode.node("blastRadius").getDouble(4.0)),
					Math.max(0.0, explosiveNode.node("explosionDamage").getDouble(12.0)),
					Math.max(0.0, explosiveNode.node("explosionKnockback").getDouble(1.2)),
					explosiveNode.node("damageBlocks").getBoolean(false),
					rebuild.node("enabled").getBoolean(false),
					Math.max(0, rebuild.node("delay").getInt(100)),
					Math.max(1, rebuild.node("blocksPerTick").getInt(2)));
		}

		String payload = node.node("payload").getString();

		String ammoType = node.node("ammoType").getString("default");
		boolean consumesAmmo = node.node("consumesAmmo").getBoolean(false);
		int burstCount = node.node("burstCount").getInt(1);
		double spread = node.node("spread").getDouble(1.5);
		double recoil = node.node("recoil").getDouble(0.0);
		RecoilProfile recoilProfile = parseRecoilProfile(node.node("recoilProfile"));
		double knockback = Math.max(0.0, node.node("knockback").getDouble(0.0));
		double selfKnockback = Math.max(0.0, node.node("selfKnockback").getDouble(0.0));
		int bulletCount = Math.max(1, node.node("bulletCount").getInt(1));
		double bulletDrop = node.node("bulletDrop").getDouble(0.0);
		double headshotMultiplier = node.node("headshotMultiplier").getDouble(1.0);
		boolean automatic = node.node("automatic").getBoolean(false);
		int actionOpenTime = Math.max(0, node.node("actionOpenTime").getInt(0));
		int actionCloseTime = Math.max(0, node.node("actionCloseTime").getInt(0));
		String deathMessage = node.node("deathMessage").getString();
		double critChance = Math.max(0.0, Math.min(1.0, node.node("critChance").getDouble(0.0)));
		double critMultiplier = node.node("critMultiplier").getDouble(1.5);
		int shieldDisableTime = Math.max(0, node.node("shieldDisableTime").getInt(0));
		List<PotionEffect> victimEffects = parsePotionEffects(fileName, id, node.node("victimEffects"));
		List<PotionEffect> shooterEffects = parsePotionEffects(fileName, id, node.node("shooterEffects"));
		double falloffStart = Math.max(0.0, node.node("falloffStart").getDouble(0.0));
		double falloffEnd = Math.max(0.0, node.node("falloffEnd").getDouble(0.0));
		double falloffMinMultiplier = Math.max(0.0, node.node("falloffMinMultiplier").getDouble(1.0));
		Particle trailParticle = parseParticle(fileName, id, node.node("trailParticle").getString(), Particle.CRIT);
		Particle impactParticle = parseParticle(fileName, id, node.node("impactParticle").getString(), null);
		List<PewpewSound> fireSound = parseSounds(fileName, id, node.node("fireSound"));
		List<PewpewSound> hitSound = parseSounds(fileName, id, node.node("hitSound"));
		String hitMessage = node.node("hitMessage").getString();

		String reloadTypeRaw = node.node("reloadType").getString("MAGAZINE");
		ReloadType reloadType;
		try {
			reloadType = ReloadType.valueOf(reloadTypeRaw.toUpperCase());
		} catch (IllegalArgumentException e) {
			warn(fileName, id, "unknown reloadType '" + reloadTypeRaw + "', defaulting to MAGAZINE");
			reloadType = ReloadType.MAGAZINE;
		}

		List<AttachmentType> allowedAttachmentSlots = new ArrayList<>();
		for (ConfigurationNode slotNode : node.node("allowedAttachmentSlots").childrenList()) {
			String slotRaw = slotNode.getString();
			if (slotRaw == null) continue;
			try {
				allowedAttachmentSlots.add(AttachmentType.valueOf(slotRaw.toUpperCase()));
			} catch (IllegalArgumentException e) {
				warn(fileName, id, "unknown attachmentSlot '" + slotRaw + "', skipping slot");
			}
		}

		List<DefaultAttachment> defaultAttachments = new ArrayList<>();
		for (var entry : node.node("defaultAttachments").childrenMap().entrySet()) {
			String slotRaw = String.valueOf(entry.getKey());
			AttachmentType slot;
			try {
				slot = AttachmentType.valueOf(slotRaw.toUpperCase());
			} catch (IllegalArgumentException e) {
				warn(fileName, id, "unknown defaultAttachment slot '" + slotRaw + "', skipping");
				continue;
			}
			if (!allowedAttachmentSlots.contains(slot)) {
				warn(fileName, id, "defaultAttachment slot '" + slot + "' is not in allowedAttachmentSlots, skipping");
				continue;
			}
			ConfigurationNode entryNode = entry.getValue();
			String attachmentId;
			boolean forced;
			if (entryNode.isMap()) {
				attachmentId = entryNode.node("id").getString();
				forced = entryNode.node("forced").getBoolean(false);
			} else {
				attachmentId = entryNode.getString();
				forced = false;
			}
			if (attachmentId == null || attachmentId.isBlank()) {
				warn(fileName, id, "defaultAttachment slot '" + slot + "' is missing an attachment id, skipping");
				continue;
			}
			defaultAttachments.add(new DefaultAttachment(slot, attachmentId, forced));
		}

		return PewpewGunItem.builder()
				.id(id)
				.name(name)
				.lore(lore)
				.hideItemFlags(hideItemFlags)
				.customModelData(customModelData)
				.itemModel(itemModel)
				.baseDamage(baseDamage)
				.fireRate(fireRate)
				.reloadTime(reloadTime)
				.ammoType(ammoType)
				.maxAmmo(maxAmmo)
				.consumesAmmo(consumesAmmo)
				.allowedAttachmentSlots(allowedAttachmentSlots)
				.firingMode(firingMode)
				.range(range)
				.projectileSpeed(projectileSpeed)
				.projectileModel(projectileModel)
				.trajectory(trajectory)
				.explosive(explosive)
				.payload(payload)
				.burstCount(burstCount)
				.reloadType(reloadType)
				.spread(spread)
				.recoil(recoil)
				.recoilProfile(recoilProfile)
				.knockback(knockback)
				.selfKnockback(selfKnockback)
				.bulletCount(bulletCount)
				.bulletDrop(bulletDrop)
				.headshotMultiplier(headshotMultiplier)
				.automatic(automatic)
				.actionOpenTime(actionOpenTime)
				.actionCloseTime(actionCloseTime)
				.deathMessage(deathMessage)
				.critChance(critChance)
				.critMultiplier(critMultiplier)
				.shieldDisableTime(shieldDisableTime)
				.victimEffects(victimEffects)
				.shooterEffects(shooterEffects)
				.falloffStart(falloffStart)
				.falloffEnd(falloffEnd)
				.falloffMinMultiplier(falloffMinMultiplier)
				.trailParticle(trailParticle)
				.impactParticle(impactParticle)
				.fireSound(fireSound)
				.hitSound(hitSound)
				.hitMessage(hitMessage)
				.defaultAttachments(defaultAttachments)
				.build();
	}

	@Nullable
	private static PewpewThrowableItem deserializeThrowable(String fileName, String id, ConfigurationNode node,
	                                                        String name, List<String> lore, boolean hideItemFlags,
	                                                        int customModelData, String itemModel) {
		int fuseTime = node.node("fuseTime").getInt(0);
		double blastRadius = node.node("blastRadius").getDouble(0.0);
		double throwForce = node.node("throwForce").getDouble(0.0);

		String effectRaw = node.node("effect").getString();
		if (effectRaw == null) {
			warn(fileName, id, "missing required field 'effect'");
			return null;
		}
		ThrowableEffect effect;
		try {
			effect = ThrowableEffect.valueOf(effectRaw.toUpperCase());
		} catch (IllegalArgumentException e) {
			warn(fileName, id, "unknown effect '" + effectRaw + "'");
			return null;
		}

		double explosionDamage = Math.max(0.0, node.node("explosionDamage").getDouble(12.0));
		double explosionKnockback = Math.max(0.0, node.node("explosionKnockback").getDouble(1.2));
		int effectDuration = node.node("effectDuration").getInt(-1);
		int effectAmplifier = node.node("effectAmplifier").getInt(-1);
		int fireTicks = Math.max(0, node.node("fireTicks").getInt(80));

		return PewpewThrowableItem.builder()
				.id(id)
				.name(name)
				.lore(lore)
				.hideItemFlags(hideItemFlags)
				.customModelData(customModelData)
				.itemModel(itemModel)
				.fuseTime(fuseTime)
				.blastRadius(blastRadius)
				.throwForce(throwForce)
				.effect(effect)
				.explosionDamage(explosionDamage)
				.explosionKnockback(explosionKnockback)
				.effectDuration(effectDuration)
				.effectAmplifier(effectAmplifier)
				.fireTicks(fireTicks)
				.build();
	}

	@Nullable
	private static PewpewAttachment deserializeAttachment(String fileName, String id, ConfigurationNode node,
	                                                      String name, List<String> lore, boolean hideItemFlags,
	                                                      int customModelData, String itemModel) {
		String attachmentTypeRaw = node.node("attachmentType").getString();
		if (attachmentTypeRaw == null) {
			warn(fileName, id, "missing required field 'attachmentType'");
			return null;
		}
		AttachmentType attachmentType;
		try {
			attachmentType = AttachmentType.valueOf(attachmentTypeRaw.toUpperCase());
		} catch (IllegalArgumentException e) {
			warn(fileName, id, "unknown attachmentType '" + attachmentTypeRaw + "'");
			return null;
		}

		return switch (attachmentType) {
			case SCOPE -> PewpewScopeAttachment.builder()
					.id(id).name(name).lore(lore).hideItemFlags(hideItemFlags)
					.customModelData(customModelData).itemModel(itemModel)
					.slot(AttachmentType.SCOPE)
					.zoom(node.node("zoom").getDouble(1.0))
					.adsSpeedModifier(node.node("adsSpeedModifier").getDouble(1.0))
					.aimSpreadMultiplier(node.node("aimSpreadMultiplier").getDouble(0.0))
					.aimRecoilMultiplier(node.node("aimRecoilMultiplier").getDouble(0.0))
					.build();
			case BARREL -> PewpewBarrelAttachment.builder()
					.id(id).name(name).lore(lore).hideItemFlags(hideItemFlags)
					.customModelData(customModelData).itemModel(itemModel)
					.slot(AttachmentType.BARREL)
					.damageModifier(node.node("damageModifier").getDouble(1.0))
					.rangeModifier(node.node("rangeModifier").getDouble(1.0))
					.build();
			case GRIP -> PewpewGripAttachment.builder()
					.id(id).name(name).lore(lore).hideItemFlags(hideItemFlags)
					.customModelData(customModelData).itemModel(itemModel)
					.slot(AttachmentType.GRIP)
					.recoilModifier(node.node("recoilModifier").getDouble(1.0))
					.build();
			case MAGAZINE -> PewpewMagazineAttachment.builder()
					.id(id).name(name).lore(lore).hideItemFlags(hideItemFlags)
					.customModelData(customModelData).itemModel(itemModel)
					.slot(AttachmentType.MAGAZINE)
					.ammoBonus(node.node("ammoBonus").getInt(0))
					.reloadModifier(node.node("reloadModifier").getDouble(1.0))
					.build();
		};
	}

	@NotNull
	private static List<PotionEffect> parsePotionEffects(String fileName, String id, ConfigurationNode node) {
		List<PotionEffect> effects = new ArrayList<>();
		for (ConfigurationNode entry : node.childrenList()) {
			String raw = entry.getString();
			if (raw == null || raw.isBlank()) continue;
			String[] parts = raw.split(":");
			PotionEffectType type = Registry.MOB_EFFECT.get(
					NamespacedKey.minecraft(parts[0].trim().toLowerCase()));
			if (type == null) {
				warn(fileName, id, "unknown potion effect '" + parts[0] + "', skipping");
				continue;
			}
			int duration = parts.length > 1 ? parseIntOr(parts[1], 20) : 20;
			int amplifier = parts.length > 2 ? parseIntOr(parts[2], 0) : 0;
			effects.add(new PotionEffect(type, duration, amplifier, false, true));
		}
		return effects;
	}

	@Nullable
	private static Particle parseParticle(String fileName, String id, @Nullable String name, @Nullable Particle fallback) {
		if (name == null || name.isBlank()) return fallback;
		try {
			return Particle.valueOf(name.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			warn(fileName, id, "unknown particle '" + name + "', using default");
			return fallback;
		}
	}

	@Nullable
	private static RecoilProfile parseRecoilProfile(ConfigurationNode node) {
		if (node.virtual()) return RecoilProfile.DEFAULT;
		RecoilProfile defaults = RecoilProfile.DEFAULT;
		return RecoilProfile.builder()
				.verticalMean((float) node.node("verticalMean").getDouble(defaults.getVerticalMean()))
				.verticalVariance((float) Math.max(0.0, node.node("verticalVariance").getDouble(defaults.getVerticalVariance())))
				.horizontalMean((float) node.node("horizontalMean").getDouble(defaults.getHorizontalMean()))
				.horizontalVariance((float) Math.max(0.0, node.node("horizontalVariance").getDouble(defaults.getHorizontalVariance())))
				.smoothing((float) clamp(node.node("smoothing").getDouble(defaults.getSmoothing()), 0.01, 1.0))
				.damping((float) clamp(node.node("damping").getDouble(defaults.getDamping()), 0.0, 1.0))
				.recovery((float) Math.max(0.0, node.node("recovery").getDouble(defaults.getRecovery())))
				.recoveryPenalty((float) clamp(node.node("recoveryPenalty").getDouble(defaults.getRecoveryPenalty()), 0.0, 1.0))
				.speed((float) Math.max(0.0, node.node("speed").getDouble(defaults.getSpeed())))
				.maxAccumulation((float) Math.max(0.0, node.node("maxAccumulation").getDouble(defaults.getMaxAccumulation())))
				.build();
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	private static List<PewpewSound> parseSounds(String fileName, String id, ConfigurationNode node) {
		if (node.virtual()) return null;
		if (node.isList()) {
			List<PewpewSound> sounds = new ArrayList<>();
			for (ConfigurationNode child : node.childrenList()) {
				PewpewSound sound = parseSound(fileName, id, child);
				if (sound != null) sounds.add(sound);
			}
			return sounds.isEmpty() ? null : sounds;
		}
		PewpewSound single = parseSound(fileName, id, node);
		return single == null ? null : List.of(single);
	}

	private static PewpewSound parseSound(String fileName, String id, ConfigurationNode node) {
		if (node.virtual()) return null;
		String key;
		float volume = 1.0f;
		float pitch = 1.0f;
		if (node.isMap()) {
			key = node.node("key").getString();
			volume = (float) node.node("volume").getDouble(1.0);
			pitch = (float) node.node("pitch").getDouble(1.0);
		} else {
			key = node.getString();
		}
		if (key == null || key.isBlank()) return null;
		try {
			return PewpewSound.of(key, volume, pitch);
		} catch (Exception e) {
			warn(fileName, id, "invalid sound '" + key + "', ignoring");
			return null;
		}
	}

	private static int parseIntOr(String value, int fallback) {
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static void warn(String fileName, String id, String reason) {
		Logger log = PewpewPlugin.getInstance().getLogger();
		log.warning("[WeaponDeserializer] Skipping '" + id + "' in " + fileName + ": " + reason);
	}
}
