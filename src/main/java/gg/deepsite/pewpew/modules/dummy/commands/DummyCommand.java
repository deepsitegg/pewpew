package gg.deepsite.pewpew.modules.dummy.commands;

import com.jazzkuh.commandlib.common.annotations.Command;
import com.jazzkuh.commandlib.common.annotations.Description;
import com.jazzkuh.commandlib.common.annotations.Main;
import com.jazzkuh.commandlib.common.annotations.Permission;
import com.jazzkuh.commandlib.common.annotations.Subcommand;
import com.jazzkuh.commandlib.spigot.AnnotationCommand;
import gg.deepsite.pewpew.modules.dummy.listeners.DummyListener;
import gg.deepsite.pewpew.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("dummy")
public class DummyCommand extends AnnotationCommand {

	@Main
	@Description("Spawns a damage-test dummy at your location.")
	@Permission("pewpew.command.dummy")
	public void main(Player sender) {
		Location spawn = sender.getLocation().clone();
		spawn.setYaw(spawn.getYaw() + 180.0f);
		spawn.setPitch(0.0f);
		DummyListener.spawnDummy(spawn);
		sender.sendMessage(ChatUtils.prefix("<success>Spawned a damage dummy. <gray>Hit it to see your damage."));
	}

	@Subcommand("clear")
	@Description("Removes all damage dummies.")
	@Permission("pewpew.command.dummy")
	public void clear(CommandSender sender) {
		int removed = DummyListener.clearDummies();
		sender.sendMessage(ChatUtils.prefix("<success>Removed <gray>" + removed + "<success> dummy(s)."));
	}
}
