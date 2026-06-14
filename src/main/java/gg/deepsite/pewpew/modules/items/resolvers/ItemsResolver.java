package gg.deepsite.pewpew.modules.items.resolvers;

import com.jazzkuh.commandlib.common.AnnotationCommandSender;
import com.jazzkuh.commandlib.common.resolvers.CompletionResolver;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ItemsResolver implements CompletionResolver<CommandSender> {
	private final ItemsModule itemsModule = PewpewPlugin.getModuleManager().get(ItemsModule.class);

	@Override
	public List<String> resolve(AnnotationCommandSender<CommandSender> annotationCommandSender, String s) {
		return itemsModule.getItems().keySet().stream().toList();
	}

}
