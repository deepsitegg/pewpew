package gg.deepsite.pewpew.modules.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.api.events.PewpewHitBlockEvent;
import gg.deepsite.pewpew.api.events.PewpewHitEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class ExprPewpewDistance extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprPewpewDistance.class, Number.class, ExpressionType.SIMPLE,
				"[the] pewpew (distance|range of [the] shot)");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PewpewHitEvent.class, PewpewHitBlockEvent.class)) {
			Skript.error("'pewpew distance' can only be used in a pewpew hit or hit block event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event event) {
		if (event instanceof PewpewHitEvent hit) return new Number[]{hit.getDistance()};
		if (event instanceof PewpewHitBlockEvent hit) return new Number[]{hit.getDistance()};
		return new Number[0];
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pewpew distance";
	}
}
