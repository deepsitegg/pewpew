package gg.deepsite.pewpew.utils.configuration;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public final class ConfigMigrator {

	public static final String VERSION_KEY = "config-version";

	public record Migration(int to, @NotNull String description,
	                        @NotNull BiConsumer<ConfigurationNode, Logger> apply) {
	}

	private ConfigMigrator() {
	}

	public static int versionOf(@NotNull ConfigurationNode root) {
		return root.node(VERSION_KEY).getInt(1);
	}

	public static boolean migrate(@NotNull ConfigurationNode root, @NotNull List<Migration> migrations,
	                              int currentVersion, @NotNull String label, @NotNull Logger log) {
		int version = versionOf(root);
		if (version >= currentVersion) return false;

		log.info("Migrating " + label + " from config version " + version + " to " + currentVersion);

		for (Migration migration : migrations) {
			if (migration.to() <= version) continue;
			try {
				migration.apply().accept(root, log);
				log.info("  v" + migration.to() + ": " + migration.description());
			} catch (Exception e) {
				log.warning("  v" + migration.to() + " failed on " + label + ": " + e.getMessage());
			}
		}

		set(root.node(VERSION_KEY), currentVersion, log);
		return true;
	}

	public static void set(@NotNull ConfigurationNode node, Object value, @NotNull Logger log) {
		try {
			node.set(value);
		} catch (Exception e) {
			log.warning("Failed to write migrated value at " + node.path() + ": " + e.getMessage());
		}
	}

	public static void rename(@NotNull ConfigurationNode root, @NotNull Object[] from, @NotNull Object[] to,
	                          @NotNull Logger log) {
		ConfigurationNode source = root.node(from);
		if (source.virtual() || source.isNull()) return;
		set(root.node(to), source.raw(), log);
		ConfigurationNode parent = source.parent();
		if (parent != null) parent.removeChild(from[from.length - 1]);
	}
}
