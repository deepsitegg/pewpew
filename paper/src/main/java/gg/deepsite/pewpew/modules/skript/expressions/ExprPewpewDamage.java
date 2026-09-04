package gg.deepsite.pewpew.modules.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.api.events.PewpewGunExplodeEvent;
import gg.deepsite.pewpew.api.events.PewpewHitEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class ExprPewpewDamage extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprPewpewDamage.class, Number.class, ExpressionType.SIMPLE,
				"[the] pewpew damage", "[the] damage of [the] pewpew (shot|hit)");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PewpewHitEvent.class, PewpewGunExplodeEvent.class)) {
			Skript.error("'pewpew damage' can only be used in a pewpew hit or gun explode event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event event) {
		if (event instanceof PewpewHitEvent hit) return new Number[]{hit.getDamage()};
		if (event instanceof PewpewGunExplodeEvent explode) return new Number[]{explode.getExplosionDamage()};
		return new Number[0];
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, DELETE, RESET -> new Class[]{Number.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		double value = (delta != null && delta.length > 0 && delta[0] instanceof Number number) ? number.doubleValue() : 0;
		if (event instanceof PewpewHitEvent hit) {
			hit.setDamage(applied(hit.getDamage(), value, mode));
		} else if (event instanceof PewpewGunExplodeEvent explode) {
			explode.setExplosionDamage(applied(explode.getExplosionDamage(), value, mode));
		}
	}

	private static double applied(double current, double value, ChangeMode mode) {
		return switch (mode) {
			case SET -> Math.max(0, value);
			case ADD -> Math.max(0, current + value);
			case REMOVE -> Math.max(0, current - value);
			case DELETE, RESET -> 0;
			default -> current;
		};
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
		return "pewpew damage";
	}
}
