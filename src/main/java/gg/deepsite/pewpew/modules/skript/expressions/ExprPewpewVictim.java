package gg.deepsite.pewpew.modules.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.api.events.PewpewHitEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * {@code [the] pewpew (victim|target)} — the living entity hit by a pewpew shot.
 * Use this instead of {@code event-entity}, which is ambiguous in a pewpew hit
 * event because the shooter is also an entity. Only usable in a pewpew hit event.
 */
@SuppressWarnings({"unused", "deprecation", "removal"})
public class ExprPewpewVictim extends SimpleExpression<LivingEntity> {

	static {
		Skript.registerExpression(ExprPewpewVictim.class, LivingEntity.class, ExpressionType.SIMPLE,
				"[the] pewpew (victim|target|hit entity)");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PewpewHitEvent.class)) {
			Skript.error("'pewpew victim' can only be used in a pewpew hit event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected LivingEntity[] get(Event event) {
		if (!(event instanceof PewpewHitEvent hit)) return new LivingEntity[0];
		return new LivingEntity[]{hit.getTarget()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends LivingEntity> getReturnType() {
		return LivingEntity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pewpew victim";
	}
}
