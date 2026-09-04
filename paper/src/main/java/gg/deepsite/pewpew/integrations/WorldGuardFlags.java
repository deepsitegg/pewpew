package gg.deepsite.pewpew.integrations;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;

final class WorldGuardFlags {

	private static StateFlag gunsFlag;

	private WorldGuardFlags() {
	}

	static void registerFlag() {
		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
		try {
			StateFlag flag = new StateFlag("pewpew-guns", true);
			registry.register(flag);
			gunsFlag = flag;
		} catch (FlagConflictException e) {
			Flag<?> existing = registry.get("pewpew-guns");
			if (existing instanceof StateFlag stateFlag) gunsFlag = stateFlag;
		}
	}

	static boolean allows(Player player) {
		if (gunsFlag == null) return true;
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		LocalPlayer local = WorldGuardPlugin.inst().wrapPlayer(player);
		com.sk89q.worldedit.util.Location location = BukkitAdapter.adapt(player.getLocation());
		return query.testState(location, local, gunsFlag);
	}
}
