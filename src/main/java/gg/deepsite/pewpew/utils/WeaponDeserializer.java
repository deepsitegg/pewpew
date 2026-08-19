package gg.deepsite.pewpew.utils;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.enums.*;
import gg.deepsite.pewpew.api.objects.*;
import gg.deepsite.pewpew.api.objects.attachment.*;
import gg.deepsite.pewpew.utils.configuration.ConfigMigrator;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.damage.DamageType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@UtilityClass
public class WeaponDeserializer {

	public static final String EXTENDS_KEY = "extends";
	public static final String ABSTRACT_KEY = "abstract";

	@NotNull
	public static List<PewPewItem> deserializeAll(String fileName, ConfigurationNode root) {
		return deserializeAll(fileName, root, Map.of(), false, false);
	}

	@NotNull
	public static List<PewPewItem> deserializeAll(String fileName, ConfigurationNode root,
	                                              @NotNull Map<String, ConfigurationNode> templates,
	                                              boolean allowExtends, boolean allowAbstract) {
		Logger log = PewpewPlugin.getInstance().getLogger();
		List<PewPewItem> result = new ArrayList<>();

		for (var entry : root.childrenMap().entrySet()) {
			String id = entry.getKey().toString();
			if (ConfigMigrator.VERSION_KEY.equals(id)) continue;

			ConfigurationNode raw = entry.getValue();

			if (!allowExtends && !raw.node(EXTENDS_KEY).virtual()) {
				log.warning("[WeaponDeserializer] '" + id + "' in " + fileName + " uses 'extends', which is off."
						+ " Set advanced.extends to true in config.yml to use it.");
			}
			if (!allowAbstract && raw.node(ABSTRACT_KEY).getBoolean(false)) {
				log.warning("[WeaponDeserializer] '" + id + "' in " + fileName + " is marked 'abstract', which is off."
						+ " Set advanced.abstract to true in config.yml to use it.");
			}

			if (allowAbstract && raw.node(ABSTRACT_KEY).getBoolean(false)) continue;

			ConfigurationNode node = allowExtends ? resolveExtends(id, raw, templates, log) : raw;
			PewPewItem item = deserializeOne(fileName, id, node);
			if (item != null) result.add(item);
		}
		return result;
	}

	@NotNull
	public static Map<String, ConfigurationNode> index(@NotNull Collection<ConfigurationNode> roots) {
		Map<String, ConfigurationNode> templates = new HashMap<>();
		for (ConfigurationNode root : roots) {
			for (var entry : root.childrenMap().entrySet()) {
				String id = entry.getKey().toString();
				if (ConfigMigrator.VERSION_KEY.equals(id)) continue;
				templates.putIfAbsent(id, entry.getValue());
			}
		}
		return templates;
	}

	@NotNull
	public static ConfigurationNode resolveExtends(@NotNull String id, @NotNull ConfigurationNode node,
	                                               @NotNull Map<String, ConfigurationNode> templates,
	                                               @NotNull Logger log) {
		return resolve(id, node, templates, new LinkedHashSet<>(), log);
	}

	@NotNull
	private static ConfigurationNode resolve(String id, ConfigurationNode node,
	                                         Map<String, ConfigurationNode> templates,
	                                         Set<String> chain, Logger log) {
		String parentId = node.node(EXTENDS_KEY).getString();
		if (parentId == null || parentId.isBlank()) return node;

		if (!chain.add(id)) {
			log.warning("[WeaponDeserializer] '" + id + "' has a circular 'extends' chain ("
					+ String.join(" -> ", chain) + " -> " + id + "); ignoring the rest of it.");
			return withoutExtends(node);
		}
		if (chain.contains(parentId)) {
			log.warning("[WeaponDeserializer] '" + id + "' extends '" + parentId
					+ "', which extends back into itself; ignoring 'extends'.");
			return withoutExtends(node);
		}

		ConfigurationNode parent = templates.get(parentId);
		if (parent == null) {
			log.warning("[WeaponDeserializer] '" + id + "' extends unknown item '" + parentId + "'; ignoring 'extends'.");
			return withoutExtends(node);
		}

		ConfigurationNode merged = withoutExtends(node);
		try {
			merged.mergeFrom(asTemplate(resolve(parentId, parent, templates, chain, log)));
		} catch (Exception e) {
			log.warning("[WeaponDeserializer] Could not apply 'extends: " + parentId + "' to '" + id + "': " + e.getMessage());
		}
		return merged;
	}

	@NotNull
	private static ConfigurationNode withoutExtends(ConfigurationNode node) {
		ConfigurationNode copy = node.copy();
		copy.removeChild(EXTENDS_KEY);
		return copy;
	}

	@NotNull
	private static ConfigurationNode asTemplate(ConfigurationNode node) {
		ConfigurationNode copy = node.copy();
		copy.removeChild(EXTENDS_KEY);
		copy.removeChild(ABSTRACT_KEY);
		return copy;
	}

	private static final Set<String> COMMON_KEYS = Set.of(
			"type", "name", "lore", "itemModel", "hideItemFlags", "customModelData", "maxStack", EXTENDS_KEY, ABSTRACT_KEY);

	private static final Map<ItemType, Set<String>> TYPE_KEYS = Map.of(
			ItemType.GUN, Set.of(
					"actionCloseTime", "actionOpenTime", "allowedAttachmentSlots", "ammoType", "automatic",
					"baseDamage", "bulletCount", "bulletDrop", "burstCount", "burstDelay", "consumesAmmo",
					"critChance", "critMultiplier", "damageType", "deathMessage", "defaultAttachments", "explosive",
					"falloffEnd", "falloffMinMultiplier", "falloffStart", "fireRate", "fireSound", "firingMode",
					"headshotMultiplier", "hitMessage", "hitSound", "impactParticle", "knockback", "maxAmmo",
					"payload", "projectileModel", "projectileSpeed", "range", "recoil", "recoilProfile",
					"reloadTime", "reloadType", "selfKnockback", "shieldDisableTime", "shooterEffects", "spread",
					"spreadModifiers", "bloomPerShot", "bloomMax", "bloomDecay",
					"trailParticle", "trajectory", "victimEffects"),
			ItemType.AMMO, Set.of(
					"ammoType", "roundsPerItem", "damageMultiplier", "velocityMultiplier", "penetration"),
			ItemType.THROWABLE, Set.of(
					"blastRadius", "effect", "effectAmplifier", "effectDuration", "explosionDamage",
					"explosionKnockback", "fireTicks", "fuseTime", "throwForce"),
			ItemType.ATTACHMENT, Set.of(
					"adsSpeedModifier", "aimRecoilMultiplier", "aimSpreadMultiplier", "ammoBonus", "attachmentType",
					"damageModifier", "rangeModifier", "recoilModifier", "reloadModifier", "zoom"));

	@NotNull
	public static List<String> unknownKeys(@NotNull ItemType type, @NotNull ConfigurationNode node) {
		Set<String> known = TYPE_KEYS.getOrDefault(type, Set.of());
		List<String> unknown = new ArrayList<>();
		for (Object key : node.childrenMap().keySet()) {
			String name = key.toString();
			if (!COMMON_KEYS.contains(name) && !known.contains(name)) unknown.add(name);
		}
		return unknown;
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

		for (String unknown : unknownKeys(type, node)) {
			PewpewPlugin.getInstance().getLogger().warning("[WeaponDeserializer] '" + id + "' in " + fileName
					+ " has an unknown field '" + unknown + "', which does nothing. Check it for typos.");
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
		double damageMultiplier = Math.max(0.0, node.node("damageMultiplier").getDouble(1.0));
		double velocityMultiplier = Math.max(0.0, node.node("velocityMultiplier").getDouble(1.0));
		int penetration = Math.max(0, node.node("penetration").getInt(0));

		return PewpewAmmoItem.builder()
				.id(id)
				.name(name)
				.lore(lore)
				.hideItemFlags(hideItemFlags)
				.customModelData(customModelData)
				.itemModel(itemModel)
				.ammoType(ammoType)
				.roundsPerItem(roundsPerItem)
				.damageMultiplier(damageMultiplier)
				.velocityMultiplier(velocityMultiplier)
				.penetration(penetration)
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

		double fireRate = node.node("fireRate").getDouble(Double.MIN_VALUE);
		if (fireRate == Double.MIN_VALUE) {
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
		SpreadModifiers spreadModifiers = parseSpreadModifiers(node.node("spreadModifiers"));
		double bloomPerShot = Math.max(0.0, node.node("bloomPerShot").getDouble(0.0));
		double bloomMax = Math.max(0.0, node.node("bloomMax").getDouble(0.0));
		double bloomDecay = Math.max(0.0, node.node("bloomDecay").getDouble(0.0));
		double recoil = node.node("recoil").getDouble(0.0);
		RecoilProfile recoilProfile = parseRecoilProfile(node.node("recoilProfile"));
		double knockback = Math.max(0.0, node.node("knockback").getDouble(0.0));
		double selfKnockback = Math.max(0.0, node.node("selfKnockback").getDouble(0.0));
		int bulletCount = Math.max(1, node.node("bulletCount").getInt(1));
		double bulletDrop = node.node("bulletDrop").getDouble(0.0);
		int burstDelay = Math.max(1, node.node("burstDelay").getInt(2));
		double headshotMultiplier = node.node("headshotMultiplier").getDouble(1.0);
		DamageType damageType = parseDamageType(fileName, id, node.node("damageType").getString());
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
				.spreadModifiers(spreadModifiers)
				.bloomPerShot(bloomPerShot)
				.bloomMax(bloomMax)
				.bloomDecay(bloomDecay)
				.burstDelay(burstDelay)
				.headshotMultiplier(headshotMultiplier)
				.damageType(damageType)
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
	private static DamageType parseDamageType(String fileName, String id, @Nullable String name) {
		if (name == null || name.isBlank()) return null;
		NamespacedKey key = NamespacedKey.fromString(name.trim().toLowerCase());
		DamageType type = key == null ? null : Registry.DAMAGE_TYPE.get(key);
		if (type == null) {
			warn(fileName, id, "unknown damageType '" + name + "', using the default");
			return null;
		}
		return type;
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
				.pattern(parseRecoilPattern(node.node("pattern")))
				.patternLoop(node.node("patternLoop").getBoolean(defaults.isPatternLoop()))
				.patternReset(Math.max(1, node.node("patternReset").getInt(defaults.getPatternReset())))
				.build();
	}

	@Nullable
	private static SpreadModifiers parseSpreadModifiers(ConfigurationNode node) {
		if (node.virtual()) return null;
		SpreadModifiers defaults = SpreadModifiers.DEFAULT;
		return SpreadModifiers.builder()
				.sprinting(Math.max(0.0, node.node("sprinting").getDouble(defaults.getSprinting())))
				.walking(Math.max(0.0, node.node("walking").getDouble(defaults.getWalking())))
				.sneaking(Math.max(0.0, node.node("sneaking").getDouble(defaults.getSneaking())))
				.standing(Math.max(0.0, node.node("standing").getDouble(defaults.getStanding())))
				.midair(Math.max(0.0, node.node("midair").getDouble(defaults.getMidair())))
				.inWater(Math.max(0.0, node.node("inWater").getDouble(defaults.getInWater())))
				.build();
	}

	@Nullable
	private static List<float[]> parseRecoilPattern(ConfigurationNode node) {
		if (node.virtual() || node.childrenList().isEmpty()) return null;

		List<float[]> pattern = new ArrayList<>();
		for (ConfigurationNode step : node.childrenList()) {
			List<? extends ConfigurationNode> pair = step.childrenList();
			if (pair.size() < 2) continue;
			pattern.add(new float[]{(float) pair.get(0).getDouble(0.0), (float) pair.get(1).getDouble(0.0)});
		}
		return pattern.isEmpty() ? null : pattern;
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
