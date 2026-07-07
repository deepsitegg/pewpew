package gg.deepsite.pewpew.modules.plugin.commands;

import com.jazzkuh.commandlib.common.annotations.*;
import com.jazzkuh.commandlib.spigot.AnnotationCommand;
import lombok.SneakyThrows;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.configuration.DefaultConfiguration;
import gg.deepsite.pewpew.configuration.MessagesConfig;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.items.commands.ItemsCommand;
import gg.deepsite.pewpew.utils.ChatUtils;
import gg.deepsite.pewpew.utils.UpdateChecker;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.URL;
import java.util.jar.Manifest;

@Command("Pewpew")
public class PewpewCommand extends AnnotationCommand {

	@Main
	@Alias("pp")
	@Description("The pewpew command.")
	@Usage("<subcommand>")
	public void main(CommandSender sender) {
		version(sender);
	}

	@Subcommand("reload")
	@Description("Reloads the plugin configuration.")
	@Permission("pewpew.command.reload")
	public void reload(CommandSender sender) {
		PewpewPlugin.setDefaultConfiguration(new DefaultConfiguration(PewpewPlugin.getInstance().getDataFolder()));
		PewpewPlugin.getDefaultConfiguration().saveConfiguration();
		PewpewPlugin.setMessagesConfig(new MessagesConfig(PewpewPlugin.getInstance().getDataFolder()));
		PewpewPlugin.getMessagesConfig().saveConfiguration();
		PewpewPlugin.getModuleManager().get(ItemsModule.class).reload();
		sender.sendMessage(ChatUtils.prefix("<success>Configuration reloaded."));
	}

	@SneakyThrows
	@Subcommand("version")
	@Description("Shows the version of the plugin.")
	public void version(CommandSender sender) {
		URL url = PewpewPlugin.getInstance().getClass().getClassLoader().getResource("META-INF/MANIFEST.MF");
		if (url == null) {
			sender.sendMessage(ChatUtils.format("<error>Unable to load version information."));
			return;
		}
		try (var in = url.openStream()) {
			var attrs = new Manifest(in).getMainAttributes();
			String version     = attrs.getValue("Implementation-Version");
			String buildTime   = attrs.getValue("Build-Time");
			String maintainers = attrs.getValue("Maintainers");

			sender.sendMessage("");
			sender.sendMessage(ChatUtils.format("<color>Pewpew</color>", ChatUtils.PRIMARY));
			sender.sendMessage(ChatUtils.format("<color_alt>Version:</color_alt> %1",       ChatUtils.PRIMARY, version     != null ? version     : "<error>Unknown"));
			sender.sendMessage(ChatUtils.format("<color_alt>Build Date:</color_alt> %1",    ChatUtils.PRIMARY, buildTime   != null ? buildTime   : "<error>Unknown"));
			sender.sendMessage(ChatUtils.format("<color_alt>Maintained by:</color_alt> %1", ChatUtils.PRIMARY, maintainers != null ? maintainers : "<error>Unknown"));
			if (UpdateChecker.isUpdateAvailable()) {
				sender.sendMessage(ChatUtils.format("<warning>Update available: %1 - %2", ChatUtils.PRIMARY,
						UpdateChecker.getLatestVersion(), UpdateChecker.RELEASES_URL));
			}
			sender.sendMessage("");
		}
	}

	@Subcommand("list")
	@Description("Lists all registered items.")
	@Permission("pewpew.command.items.list")
	public void itemsList(CommandSender sender) {
		ItemsCommand.list(sender);
	}

	@Subcommand("give")
	@Usage("<id> [amount]")
	@Description("Gives you the item with the specified id.")
	@Permission("pewpew.command.items.give")
	public void itemsGive(Player sender, @Completion("@items") String id, @Optional Integer amount) {
		ItemsCommand.give(sender, id, amount);
	}

}
