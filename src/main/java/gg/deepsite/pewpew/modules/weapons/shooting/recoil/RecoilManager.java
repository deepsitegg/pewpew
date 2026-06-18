package gg.deepsite.pewpew.modules.weapons.shooting.recoil;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RecoilManager {

    private final Plugin plugin;
    private final Map<UUID, RecoilController> controllers = new ConcurrentHashMap<>();
    private BukkitTask task;

    public RecoilManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public void kick(@NotNull Player player, double verticalDegrees) {
        if (verticalDegrees <= 0) return;
        RecoilController controller = controllers.computeIfAbsent(
                player.getUniqueId(), id -> new RecoilController(player, RecoilProfile.DEFAULT));
        controller.kick(verticalDegrees);
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
