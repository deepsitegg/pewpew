package gg.deepsite.pewpew.modules.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.api.events.PewpewScopeEvent;
import gg.deepsite.pewpew.modules.weapons.shooting.ScopeState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class CondPewpewScoped extends Condition {

	static {
		Skript.registerCondition(CondPewpewScoped.class,
				"%players% (is|are) scoped [in]",
				"%players% (is not|are not) scoped [in]",
				"pewpew is scoping in",
				"pewpew is scoping out");
	}

	@Nullable
	private Expression<Player> players;
	private boolean eventForm;
	private boolean wantScopingIn;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		eventForm = matchedPattern >= 2;
		if (eventForm) {
			wantScopingIn = matchedPattern == 2;
			if (!getParser().isCurrentEvent(PewpewScopeEvent.class)) {
				Skript.error("'pewpew is scoping in/out' can only be used in a pewpew scope event");
				return false;
			}
			return true;
		}

		players = (Expression<Player>) expressions[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (eventForm) {
			return event instanceof PewpewScopeEvent scope && scope.isScopingIn() == wantScopingIn;
		}
		return players != null && players.check(event, ScopeState::isScoped, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (eventForm) return "pewpew is scoping " + (wantScopingIn ? "in" : "out");
		return players + (isNegated() ? " is not" : " is") + " scoped";
	}
}
