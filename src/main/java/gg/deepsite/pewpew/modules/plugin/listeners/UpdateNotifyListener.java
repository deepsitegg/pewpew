package gg.deepsite.pewpew.modules.plugin.listeners;

import gg.deepsite.pewpew.utils.ChatUtils;
import gg.deepsite.pewpew.utils.UpdateChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotifyListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (!UpdateChecker.isUpdateAvailable()) return;

		Player player = event.getPlayer();
		if (!player.hasPermission("pewpew.update.notify")) return;

		player.sendMessage(ChatUtils.prefix("<warning>A new version (<color_alt>"
				+ UpdateChecker.getLatestVersion() + "</color_alt>) is available!"));
		player.sendMessage(ChatUtils.prefix("<warning>Download: <color_alt><click:open_url:'"
				+ UpdateChecker.RELEASES_URL + "'><u>" + UpdateChecker.RELEASES_URL + "</u></click>"));
	}
}
