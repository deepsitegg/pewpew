package gg.deepsite.pewpew.modules.metrics;

import com.jazzkuh.modulemanager.spigot.SpigotModule;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.PewpewThrowableItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SingleLineChart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class MetricsModule extends SpigotModule<PewpewPlugin> {

	private static final int PLUGIN_ID = 32453;

	public MetricsModule(SpigotModuleManager<PewpewPlugin> moduleManager) {
		super(moduleManager);
	}

	@Override
	public void onEnable() {
		Metrics metrics = new Metrics(getPlugin(), PLUGIN_ID);

		metrics.addCustomChart(new SingleLineChart("guns_configured", () -> guns().size()));
		metrics.addCustomChart(new SingleLineChart("throwables_configured", () -> throwables().size()));

		metrics.addCustomChart(new AdvancedPie("firing_modes", () -> countBy(guns(),
				gun -> gun.getFiringMode() == null ? "unknown" : gun.getFiringMode().name())));

		metrics.addCustomChart(new AdvancedPie("reload_types", () -> countBy(guns(),
				gun -> gun.getReloadType() == null ? "unknown" : gun.getReloadType().name())));
	}

	private List<PewpewGunItem> guns() {
		return items().getByType(PewpewGunItem.class);
	}

	private List<PewpewThrowableItem> throwables() {
		return items().getByType(PewpewThrowableItem.class);
	}

	private static ItemsModule items() {
		return PewpewPlugin.getModuleManager().get(ItemsModule.class);
	}

	private static <T> Map<String, Integer> countBy(List<T> list, java.util.function.Function<T, String> key) {
		Map<String, Integer> map = new HashMap<>();
		for (T item : list) inc(map, key.apply(item));
		return map;
	}

	private static void inc(Map<String, Integer> map, String key) {
		map.merge(key, 1, Integer::sum);
	}
}
