package gg.deepsite.pewpew.modules.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.api.events.PewpewKillEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class ExprPewpewDeathMessage extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprPewpewDeathMessage.class, String.class, ExpressionType.SIMPLE,
				"[the] pewpew death message");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PewpewKillEvent.class)) {
			Skript.error("'pewpew death message' can only be used in a pewpew kill event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		if (!(event instanceof PewpewKillEvent kill) || kill.getDeathMessage() == null) return new String[0];
		return new String[]{kill.getDeathMessage()};
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> new Class[]{String.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof PewpewKillEvent kill)) return;
		if (mode == ChangeMode.SET && delta != null && delta.length > 0) {
			kill.setDeathMessage(String.valueOf(delta[0]));
		} else {
			kill.setDeathMessage(null);
		}
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
		return "pewpew death message";
	}
}
