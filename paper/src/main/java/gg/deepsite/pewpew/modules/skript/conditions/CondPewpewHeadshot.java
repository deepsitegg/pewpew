package gg.deepsite.pewpew.modules.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.api.events.PewpewHitEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class CondPewpewHeadshot extends Condition {

	static {
		Skript.registerCondition(CondPewpewHeadshot.class,
				"[the] pewpew (hit|shot) (was|is) [a] (0:headshot|1:crit[ical])",
				"[the] pewpew (hit|shot) (was not|is not) [a] (0:headshot|1:crit[ical])");
	}

	private boolean critical;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		critical = parseResult.mark == 1;
		setNegated(matchedPattern == 1);
		if (!getParser().isCurrentEvent(PewpewHitEvent.class)) {
			Skript.error("this condition can only be used in a pewpew hit event");
			return false;
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PewpewHitEvent hit)) return isNegated();
		boolean value = critical ? hit.isCritical() : hit.isHeadshot();
		return isNegated() != value;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pewpew hit was a " + (critical ? "critical" : "headshot");
	}
}
