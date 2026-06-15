package gg.deepsite.pewpew.modules.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.api.events.PewpewHitEvent;
import gg.deepsite.pewpew.api.events.PewpewReloadEvent;
import gg.deepsite.pewpew.api.events.PewpewShootEvent;
import gg.deepsite.pewpew.api.events.PewpewThrowEvent;
import gg.deepsite.pewpew.api.events.PewpewThrowableDetonateEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class ExprPewpewItemId extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprPewpewItemId.class, String.class, ExpressionType.SIMPLE,
				"[the] pewpew (item|gun|weapon|throwable) id");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PewpewShootEvent.class, PewpewHitEvent.class, PewpewReloadEvent.class,
				PewpewThrowEvent.class, PewpewThrowableDetonateEvent.class)) {
			Skript.error("'pewpew item id' can only be used in a pewpew event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		String id = switch (event) {
			case PewpewShootEvent e -> e.getGun().getId();
			case PewpewHitEvent e -> e.getGun().getId();
			case PewpewReloadEvent e -> e.getGun().getId();
			case PewpewThrowEvent e -> e.getThrowable().getId();
			case PewpewThrowableDetonateEvent e -> e.getThrowable().getId();
			default -> null;
		};
		return id == null ? new String[0] : new String[]{id};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pewpew item id";
	}
}
