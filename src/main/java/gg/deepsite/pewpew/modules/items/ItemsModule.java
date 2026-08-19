package gg.deepsite.pewpew.modules.items;

import com.jazzkuh.commandlib.common.resolvers.Resolvers;
import com.jazzkuh.modulemanager.spigot.SpigotModule;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.PewpewThrowableItem;
import gg.deepsite.pewpew.api.objects.attachment.PewpewAttachment;
import gg.deepsite.pewpew.configuration.ItemConfiguration;
import gg.deepsite.pewpew.modules.items.resolvers.ItemsResolver;
import gg.deepsite.pewpew.utils.PersistentDataUtil;
import gg.deepsite.pewpew.utils.WeaponDeserializer;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.spongepowered.configurate.ConfigurationNode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class ItemsModule extends SpigotModule<PewpewPlugin> {

	private static final String[] BUNDLED_FILES = {"guns.yml", "throwables.yml", "attachments.yml", "ammo.yml"};

	public static final String PDC_KEY = "item_id";

	@Getter
	private final Map<String, PewPewItem> items = new LinkedHashMap<>();

	public ItemsModule(SpigotModuleManager<PewpewPlugin> moduleManager) {
		super(moduleManager);
	}

	@Override
	public void onEnable() {
		loadItems();
		Resolvers.register(PewPewItem.class, new ItemsResolver(), "items");
	}

	@Override
	public void onDisable() {
		items.clear();
	}

	public void reload() {
		items.clear();
		loadItems();
	}

	public static final String USE_PERMISSION_PREFIX = "pewpew.use.";

	public void register(@NotNull PewPewItem item) {
		items.put(item.getId(), item);
		registerUsePermission(item.getId());
	}

	private static void registerUsePermission(@NotNull String id) {
		String node = USE_PERMISSION_PREFIX + id;
		if (Bukkit.getPluginManager().getPermission(node) != null) return;
		Bukkit.getPluginManager().addPermission(
				new Permission(node, "Use the Pewpew item '" + id + "'.", PermissionDefault.TRUE));
	}

	public boolean unregister(@NotNull String id) {
		return items.remove(id) != null;
	}

	@Nullable
	public PewPewItem get(@NotNull String id) {
		return items.get(id);
	}

	@NotNull
	public <T extends PewPewItem> List<T> getByType(@NotNull Class<T> type) {
		List<T> result = new ArrayList<>();
		for (PewPewItem item : items.values()) {
			if (type.isInstance(item)) {
				result.add(type.cast(item));
			}
		}
		return Collections.unmodifiableList(result);
	}

	@NotNull
	public List<PewPewItem> getAll() {
		return Collections.unmodifiableList(new ArrayList<>(items.values()));
	}

	public boolean isRegistered(@NotNull String id) {
		return items.containsKey(id);
	}

	@NotNull
	public static ItemStack stamp(@NotNull ItemStack itemStack, @NotNull PewPewItem item) {
		return PersistentDataUtil.setPewpew(itemStack, item.getId(), PDC_KEY);
	}

	@Nullable
	public PewPewItem fromItemStack(@NotNull ItemStack itemStack) {
		String id = PersistentDataUtil.getPewpew(itemStack, PDC_KEY);
		if (id == null) return null;
		return items.get(id);
	}

	private void loadItems() {
		Logger log = PewpewPlugin.getInstance().getLogger();
		File itemsFolder = new File(PewpewPlugin.getInstance().getDataFolder(), "items");

		if (!itemsFolder.exists()) {
			if (!itemsFolder.mkdirs()) {
				log.warning("Failed to create items/ folder");
				return;
			}
			for (String fileName : BUNDLED_FILES) {
				File dest = new File(itemsFolder, fileName);
				try (InputStream in = PewpewPlugin.class.getResourceAsStream("/items/" + fileName)) {
					if (in == null) {
						log.warning("Could not find bundled resource: items/" + fileName);
						continue;
					}
					FileUtils.copyInputStreamToFile(in, dest);
				} catch (IOException e) {
					log.warning("Failed to copy default file " + fileName + ": " + e.getMessage());
				}
			}
		}

		File[] files = itemsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
		if (files == null || files.length == 0) {
			log.info("No item files found in items/");
			return;
		}

		int guns = 0, throwables = 0, attachments = 0;

		Map<String, ConfigurationNode> roots = new LinkedHashMap<>();
		for (File file : files) {
			ItemConfiguration config = new ItemConfiguration(itemsFolder, file.getName());
			if (config.getRootNode() != null) roots.put(file.getName(), config.getRootNode());
		}
		Map<String, ConfigurationNode> templates = WeaponDeserializer.index(roots.values());
		boolean allowExtends = PewpewPlugin.getDefaultConfiguration().isExtendsEnabled();
		boolean allowAbstract = PewpewPlugin.getDefaultConfiguration().isAbstractEnabled();

		for (var fileEntry : roots.entrySet()) {
			List<PewPewItem> items = WeaponDeserializer.deserializeAll(fileEntry.getKey(), fileEntry.getValue(),
					templates, allowExtends, allowAbstract);

			for (PewPewItem item : items) {
				if (isRegistered(item.getId())) {
					log.warning("Duplicate item id '" + item.getId() + "' found in '"
							+ fileEntry.getKey() + "', overwriting previous entry.");
				}
				register(item);

				if (item instanceof PewpewGunItem) guns++;
				else if (item instanceof PewpewThrowableItem) throwables++;
				else if (item instanceof PewpewAttachment) attachments++;
			}
		}

		log.info("Loaded " + guns + " gun(s), " + throwables + " throwable(s), "
				+ attachments + " attachment(s) from items/");
	}
}
