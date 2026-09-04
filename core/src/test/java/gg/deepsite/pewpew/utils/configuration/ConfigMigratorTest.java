package gg.deepsite.pewpew.utils.configuration;

import gg.deepsite.pewpew.utils.configuration.ConfigMigrator.Migration;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ConfigMigratorTest {

	private static final Logger LOG = Logger.getLogger("ConfigMigratorTest");

	private static final List<Migration> MIGRATIONS = List.of(
			new Migration(2, "adds compatibility.legacy-spread",
					(root, log) -> ConfigMigrator.set(root.node("compatibility", "legacy-spread"), true, log)),
			new Migration(3, "renames lore.stat-display to lore.stats",
					(root, log) -> ConfigMigrator.rename(root, new Object[]{"lore", "stat-display"}, new Object[]{"lore", "stats"}, log))
	);

	private static ConfigurationNode parse(String yaml) throws Exception {
		return YamlConfigurationLoader.builder()
				.source(() -> new BufferedReader(new StringReader(yaml)))
				.build()
				.load();
	}

	@Test
	void unversionedConfigIsTreatedAsVersionOne() throws Exception {
		assertEquals(1, ConfigMigrator.versionOf(parse("lore:\n  stat-display: true\n")));
	}

	@Test
	void migratesAnOldConfigAndStampsTheNewVersion() throws Exception {
		ConfigurationNode root = parse("lore:\n  stat-display: true\n");

		assertTrue(ConfigMigrator.migrate(root, MIGRATIONS, 3, "config.yml", LOG));

		assertTrue(root.node("compatibility", "legacy-spread").getBoolean(),
				"upgrading servers must keep the old behaviour");
		assertTrue(root.node("lore", "stats").getBoolean(), "renamed key must carry the old value");
		assertTrue(root.node("lore", "stat-display").virtual(), "old key must be gone");
		assertEquals(3, ConfigMigrator.versionOf(root));
	}

	@Test
	void appliesOnlyPendingMigrations() throws Exception {
		ConfigurationNode root = parse("config-version: 2\nlore:\n  stat-display: true\n");

		assertTrue(ConfigMigrator.migrate(root, MIGRATIONS, 3, "config.yml", LOG));

		assertTrue(root.node("compatibility", "legacy-spread").virtual(),
				"already-applied migrations must not run again");
		assertTrue(root.node("lore", "stats").getBoolean());
		assertEquals(3, ConfigMigrator.versionOf(root));
	}

	@Test
	void currentConfigIsLeftAlone() throws Exception {
		ConfigurationNode root = parse("config-version: 3\nlore:\n  stat-display: true\n");

		assertFalse(ConfigMigrator.migrate(root, MIGRATIONS, 3, "config.yml", LOG));
		assertTrue(root.node("compatibility").virtual());
		assertTrue(root.node("lore", "stat-display").getBoolean(), "untouched config must keep its keys");
	}

	@Test
	void freshInstallDefaultsToTheNewBehaviour() throws Exception {
		ConfigurationNode root = parse("config-version: 3\ncompatibility:\n  legacy-spread: false\n");

		assertFalse(ConfigMigrator.migrate(root, MIGRATIONS, 3, "config.yml", LOG));
		assertFalse(root.node("compatibility", "legacy-spread").getBoolean());
	}
}
