package gg.deepsite.pewpew.configuration;

import gg.deepsite.pewpew.utils.configuration.ConfigurateConfig;

import java.io.File;

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

}
