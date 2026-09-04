package gg.deepsite.pewpew.modules.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.modules.weapons.WeaponsModule;
import gg.deepsite.pewpew.modules.weapons.shooting.ShootingHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class CondPewpewReloading extends Condition {

	static {
		Skript.registerCondition(CondPewpewReloading.class,
				"%players% (is|are) reloading [a] [pewpew] [gun]",
				"%players% (is not|are not) reloading [a] [pewpew] [gun]");
	}

	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		WeaponsModule weapons = PewpewPlugin.getModuleManager().get(WeaponsModule.class);
		ShootingHandler handler = weapons == null ? null : weapons.getShootingHandler();
		if (handler == null) return isNegated();
		return players.check(event, handler::isReloading, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return players + (isNegated() ? " is not" : " is") + " reloading";
	}
}
