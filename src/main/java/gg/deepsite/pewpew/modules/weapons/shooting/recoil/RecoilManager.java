package gg.deepsite.pewpew.modules.weapons.shooting.recoil;

import gg.deepsite.pewpew.api.objects.RecoilProfile;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RecoilManager {

    private final Plugin plugin;
    private final Map<UUID, RecoilController> controllers = new ConcurrentHashMap<>();
    private BukkitTask task;

    public RecoilManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        if (!RelativeRotation.available()) {
            plugin.getLogger().warning("Relative camera rotation is unavailable on this server, "
                    + "recoil falls back to absolute rotation and may jitter while moving.");
        }
    }

    public void kick(@NotNull Player player, @Nullable RecoilProfile profile, double degrees) {
        if (degrees <= 0) return;
        RecoilProfile resolved = profile != null ? profile : RecoilProfile.DEFAULT;
        RecoilController controller = controllers.get(player.getUniqueId());
        if (controller == null || !controller.uses(resolved)) {
            controller = new RecoilController(player, resolved);
            controllers.put(player.getUniqueId(), controller);
        }
        controller.kick(degrees);
        ensureRunning();
    }

    private void ensureRunning() {
        if (task != null) return;
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    private void tick() {
        controllers.values().removeIf(RecoilController::tick);
        if (controllers.isEmpty() && task != null) {
            task.cancel();
            task = null;
        }
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        controllers.clear();
    }
}
