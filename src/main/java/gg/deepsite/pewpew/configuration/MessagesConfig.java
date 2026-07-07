package gg.deepsite.pewpew.configuration;

import gg.deepsite.pewpew.utils.configuration.ConfigurateConfig;

import java.io.File;

public class MessagesConfig extends ConfigurateConfig {

	public MessagesConfig(File file) {
		super(file, "messages.yml", "messages.yml", true);
	}

	private String get(String key, String def) {
		if (getRootNode() == null) return def;
		return getRootNode().node("messages", key).getString(def);
	}

	public String reloading() {
		return get("reloading", "<color>● <gray>Reloading...");
	}

	public String magazineFull() {
		return get("magazine-full", "<warning>Magazine already full.");
	}

	public String outOfAmmo() {
		return get("out-of-ammo", "<error>Out of ammo <dark_gray>┃ <gray>press <color><key:key.swapOffhand><gray> to reload");
	}

	public String noAmmoInInventory() {
		return get("no-ammo-in-inventory", "<error>Out of <reset>%1<error> in your inventory.");
	}

	public String worldGuardDeny() {
		return get("worldguard-deny", "<error>You cannot use weapons here.");
	}
}
