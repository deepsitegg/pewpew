package gg.deepsite.pewpew.modules.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.shooting.Spread;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class ExprPewpewBloom extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprPewpewBloom.class, Number.class, ExpressionType.COMBINED,
				"[the] pewpew bloom [of %players%]");
	}

	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event event) {
		Player[] targets = players.getArray(event);
		Number[] result = new Number[targets.length];
		for (int i = 0; i < targets.length; i++) {
			PewpewGunItem gun = gunOf(targets[i]);
			result[i] = gun == null ? 0 : Spread.bloom(targets[i], gun);
		}
		return result;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return mode == ChangeMode.DELETE || mode == ChangeMode.RESET ? new Class[0] : null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		for (Player player : players.getArray(event)) Spread.clear(player.getUniqueId());
	}

	@Nullable
	private static PewpewGunItem gunOf(Player player) {
		ItemsModule items = PewpewPlugin.getModuleManager().get(ItemsModule.class);
		if (items == null) return null;
		PewPewItem item = items.fromItemStack(player.getInventory().getItemInMainHand());
		return item instanceof PewpewGunItem gun ? gun : null;
	}

	@Override
	public boolean isSingle() {
		return players.isSingle();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pewpew bloom";
	}
}
