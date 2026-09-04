package gg.deepsite.pewpew.configuration;

import gg.deepsite.pewpew.utils.configuration.ConfigMigrator;
import gg.deepsite.pewpew.utils.configuration.ConfigMigrator.Migration;

import java.util.List;

public final class ConfigMigrations {

	public static final int MAIN_VERSION = 2;
	public static final int ITEMS_VERSION = 1;

	public static final List<Migration> MAIN = List.of(
			new Migration(2, "bullet spread is now a true cone; kept the old square spread via compatibility.legacy-spread",
					(root, log) -> ConfigMigrator.set(root.node("compatibility", "legacy-spread"), true, log))
	);

	public static final List<Migration> ITEMS = List.of();

	private ConfigMigrations() {
	}
}
