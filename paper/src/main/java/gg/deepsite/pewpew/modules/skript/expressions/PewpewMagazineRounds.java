package gg.deepsite.pewpew.modules.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.modules.skript.SkriptGuns;
import gg.deepsite.pewpew.modules.weapons.ammo.AmmoUtil;
import gg.deepsite.pewpew.modules.weapons.magazine.MagazineUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class PewpewMagazineRounds extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(PewpewMagazineRounds.class, Number.class, ExpressionType.COMBINED,
				"[the] pewpew magazine rounds of %players%",
				"[the] pewpew chamber of %players%");
	}

	private Expression<Player> players;
	private boolean chamber;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		chamber = matchedPattern == 1;
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event event) {
		Player[] targets = players.getArray(event);
		Number[] result = new Number[targets.length];
		for (int i = 0; i < targets.length; i++) {
			ItemStack held = targets[i].getInventory().getItemInMainHand();
			if (SkriptGuns.gunOf(held) == null || (!chamber && MagazineUtil.inserted(held) == null)) {
				result[i] = 0;
				continue;
			}
			result[i] = chamber ? AmmoUtil.chamber(held) : AmmoUtil.pool(held);
		}
		return result;
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
		return "pewpew " + (chamber ? "chamber" : "magazine rounds") + " of " + players.toString(event, debug);
	}
}
