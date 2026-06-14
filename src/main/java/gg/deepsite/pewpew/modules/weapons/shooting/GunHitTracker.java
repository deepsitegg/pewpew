package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class GunHitTracker {

	private static final long EXPIRY_MS = 10_000L;

	public record Hit(@NotNull String gunId, @NotNull UUID killerId, long time) {}

	private static final Map<UUID, Hit> HITS = new ConcurrentHashMap<>();

	public static void record(@NotNull LivingEntity victim, @NotNull Player killer, @NotNull PewpewGunItem gun) {
		HITS.put(victim.getUniqueId(), new Hit(gun.getId(), killer.getUniqueId(), System.currentTimeMillis()));
	}

	@Nullable
	public static Hit consume(@NotNull UUID victimId) {
		Hit hit = HITS.remove(victimId);
		if (hit == null || System.currentTimeMillis() - hit.time() > EXPIRY_MS) return null;
		return hit;
	}
}
