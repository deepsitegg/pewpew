package gg.deepsite.pewpew.modules.weapons.shooting;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class ScopeState {

	private static final Set<UUID> SCOPED = ConcurrentHashMap.newKeySet();

	public static void setScoped(@NotNull Player player, boolean scoped) {
		if (scoped) SCOPED.add(player.getUniqueId());
		else SCOPED.remove(player.getUniqueId());
	}

	public static boolean isScoped(@NotNull Player player) {
		return SCOPED.contains(player.getUniqueId());
	}
}
