package gg.deepsite.pewpew.modules.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.api.enums.AnimationEvent;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.modules.skript.SkriptGuns;
import gg.deepsite.pewpew.utils.Animations;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class PlayPewpewAnimation extends Effect {

	static {
		Skript.registerEffect(PlayPewpewAnimation.class,
				"play [the] pewpew animation %string% (for|on|to) %players% [over %-number% tick[s]]");
	}

	private Expression<String> animation;
	private Expression<Player> players;
	@Nullable
	private Expression<Number> ticks;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		animation = (Expression<String>) expressions[0];
		players = (Expression<Player>) expressions[1];
		ticks = (Expression<Number>) expressions[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		String path = animation.getSingle(event);
		if (path == null) return;
		AnimationEvent animationEvent = AnimationEvent.fromPath(path);
		if (animationEvent == null) return;

		Number stretch = ticks == null ? null : ticks.getSingle(event);
		int stretchToTicks = stretch == null ? 0 : Math.max(0, stretch.intValue());

		for (Player player : players.getArray(event)) {
			PewpewGunItem gun = SkriptGuns.heldGun(player);
			if (gun != null) Animations.play(player, gun, animationEvent, stretchToTicks);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "play pewpew animation " + animation.toString(event, debug) + " for " + players.toString(event, debug);
	}
}
