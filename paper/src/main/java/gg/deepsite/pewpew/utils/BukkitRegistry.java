package gg.deepsite.pewpew.utils;

import gg.deepsite.pewpew.api.objects.PewpewEffect;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.damage.DamageType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class BukkitRegistry {

	@Nullable
	public static Material item(@Nullable Key key) {
		if (key == null) return null;
		Material material = Registry.MATERIAL.get(bukkit(key));
		return material != null && material.isItem() ? material : null;
	}

	@Nullable
	public static Particle particle(@Nullable Key key) {
		return key == null ? null : Registry.PARTICLE_TYPE.get(bukkit(key));
	}

	@Nullable
	public static DamageType damageType(@Nullable Key key) {
		return key == null ? null : Registry.DAMAGE_TYPE.get(bukkit(key));
	}

	@Nullable
	public static PotionEffectType effectType(@Nullable Key key) {
		return key == null ? null : Registry.MOB_EFFECT.get(bukkit(key));
	}

	@NotNull
	public static List<PotionEffect> effects(@Nullable List<PewpewEffect> effects) {
		if (effects == null || effects.isEmpty()) return List.of();
		List<PotionEffect> resolved = new ArrayList<>(effects.size());
		for (PewpewEffect effect : effects) {
			PotionEffectType type = effectType(effect.type());
			if (type != null) resolved.add(new PotionEffect(type, effect.duration(), effect.amplifier(), false, true));
		}
		return resolved;
	}

	@NotNull
	private static NamespacedKey bukkit(@NotNull Key key) {
		return new NamespacedKey(key.namespace(), key.value());
	}
}
