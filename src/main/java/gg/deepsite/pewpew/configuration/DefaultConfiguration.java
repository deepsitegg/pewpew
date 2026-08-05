package gg.deepsite.pewpew.configuration;

import gg.deepsite.pewpew.utils.configuration.ConfigurateConfig;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.util.List;

public class DefaultConfiguration extends ConfigurateConfig {

	public DefaultConfiguration(File file) {
		super(file, "config.yml", "config.yml", true);
	}

	public boolean isStatDisplayEnabled() {
		return getRootNode().node("lore", "stat-display").getBoolean(true);
	}

	public boolean isWorldGuardEnabled() {
		return getRootNode().node("integrations", "worldguard", "enabled").getBoolean(true);
	}

	public boolean isCombatTagEnabled() {
		return getRootNode().node("integrations", "combattagplus", "enabled").getBoolean(true);
	}

	public boolean isOpenMinetopiaEnabled() {
		return getRootNode().node("integrations", "openminetopia", "enabled").getBoolean(true);
	}

	public boolean isOpenMinetopiaBlockHandcuffed() {
		return getRootNode().node("integrations", "openminetopia", "block-handcuffed").getBoolean(true);
	}

	public List<String> getOpenMinetopiaBannedPlaces() {
		try {
			return getRootNode().node("integrations", "openminetopia", "banned-places").getList(String.class, List.of());
		} catch (SerializationException e) {
			return List.of();
		}
	}

}
