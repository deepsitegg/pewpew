package gg.deepsite.pewpew.utils.configuration;

import gg.deepsite.pewpew.PewpewPlugin;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.io.FileUtils;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.*;
import java.util.List;

@Getter
public abstract class ConfigurateConfig {
	protected final YamlConfigurationLoader loader;
	protected final File configFile;
	protected ConfigurationNode rootNode;

	public ConfigurateConfig(File file, String name, String def, boolean mergeDefaults) {
		configFile = new File(file, name);
		loader = YamlConfigurationLoader.builder()
				.path(file.toPath().resolve(name))
				.indent(2)
				.nodeStyle(NodeStyle.BLOCK)
				.headerMode(HeaderMode.PRESET)
				.build();

		if (def != null && !def.isEmpty()) this.defaultConfig(file, name, def, mergeDefaults);

		try {
			rootNode = loader.load();
		} catch (IOException e) {
			PewpewPlugin.getInstance().getLogger().warning("An error occurred while loading this configuration: " + e.getMessage());
		}
	}

	protected void migrate(@NotNull List<ConfigMigrator.Migration> migrations, int currentVersion, @NotNull String label) {
		if (rootNode == null) return;
		int from = ConfigMigrator.versionOf(rootNode);
		if (from >= currentVersion) return;

		backup(from);
		if (ConfigMigrator.migrate(rootNode, migrations, currentVersion, label,
				PewpewPlugin.getInstance().getLogger())) saveConfiguration();
	}

	private void backup(int fromVersion) {
		if (!configFile.exists()) return;
		File target = new File(configFile.getParentFile(), configFile.getName() + ".v" + fromVersion + ".bak");
		try {
			FileUtils.copyFile(configFile, target);
			PewpewPlugin.getInstance().getLogger().info("Backed up " + configFile.getName() + " to " + target.getName());
		} catch (IOException e) {
			PewpewPlugin.getInstance().getLogger()
					.warning("Could not back up " + configFile.getName() + " before migrating: " + e.getMessage());
		}
	}

	public void saveConfiguration() {
		try {
			loader.save(rootNode);
		} catch (Exception e) {
			PewpewPlugin.getInstance().getLogger().warning("Unable to save your messages configuration! Sorry! " + e.getMessage());
		}
	}

	@SneakyThrows
	private void defaultConfig(File file, String name, String def, boolean mergeDefaults) {
		File config = new File(file, name);
		if (!config.exists()) {
			try (InputStream resourceStream = PewpewPlugin.class.getResourceAsStream("/" + def)) {
				if (resourceStream == null) {
					PewpewPlugin.getInstance().getLogger().warning("Could not find def resource: " + def);
					return;
				}

				FileUtils.copyInputStreamToFile(resourceStream, config);
			}
		} else if (mergeDefaults) {
			try (InputStream resourceStream = PewpewPlugin.class.getResourceAsStream("/" + def)) {
				if (resourceStream == null) {
					PewpewPlugin.getInstance().getLogger().warning("Could not find def resource for merging: " + def);
					return;
				}

				YamlConfigurationLoader defaultLoader = YamlConfigurationLoader.builder()
						.source(() -> new BufferedReader(new InputStreamReader(resourceStream)))
						.indent(2)
						.nodeStyle(NodeStyle.BLOCK)
						.headerMode(HeaderMode.PRESET)
						.build();

				ConfigurationNode defaultNode = defaultLoader.load();

				if (rootNode == null) {
					rootNode = loader.load();
				}

				mergeNodes(rootNode, defaultNode);
				saveConfiguration();
			}
		}
	}

	@SneakyThrows
	private void mergeNodes(ConfigurationNode target, ConfigurationNode source) {
		for (var entry : source.childrenMap().entrySet()) {
			Object key = entry.getKey();
			ConfigurationNode sourceChild = entry.getValue();

			if (!target.hasChild(key)) {
				target.node(key).set(sourceChild);
			} else if (!sourceChild.isNull()) {
				mergeNodes(target.node(key), sourceChild);
			}
		}
	}
}
