package gg.deepsite.pewpew.modules.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.modules.items.commands.ItemsCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class EffGivePewpew extends Effect {

	static {
		Skript.registerEffect(EffGivePewpew.class,
				"give [%-number% [of]] pewpew item[s] %string% to %players%");
	}

	@Nullable
	private Expression<Number> amount;
	private Expression<String> id;
	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		amount = (Expression<Number>) expressions[0];
		id = (Expression<String>) expressions[1];
		players = (Expression<Player>) expressions[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		String itemId = id.getSingle(event);
		if (itemId == null) return;

		Number count = amount != null ? amount.getSingle(event) : null;
		ItemStack stack = ItemsCommand.createItem(itemId, count != null ? Math.max(1, count.intValue()) : 1);
		if (stack == null) return;

		for (Player player : players.getArray(event)) {
			player.getInventory().addItem(stack.clone());
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "give pewpew item " + id.toString(event, debug);
	}
}
