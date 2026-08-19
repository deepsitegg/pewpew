package gg.deepsite.pewpew.modules.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.api.events.PewpewAttachmentEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class ExprPewpewAttachment extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprPewpewAttachment.class, String.class, ExpressionType.SIMPLE,
				"[the] pewpew attachment id", "[the] pewpew attachment slot");
	}

	private boolean slot;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		slot = matchedPattern == 1;
		if (!getParser().isCurrentEvent(PewpewAttachmentEvent.class)) {
			Skript.error("'pewpew attachment " + (slot ? "slot" : "id") + "' can only be used in a pewpew attachment event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		if (!(event instanceof PewpewAttachmentEvent attachment)) return new String[0];
		return new String[]{slot ? attachment.getSlot().name() : attachment.getAttachment().getId()};
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
		return slot ? "pewpew attachment slot" : "pewpew attachment id";
	}
}
