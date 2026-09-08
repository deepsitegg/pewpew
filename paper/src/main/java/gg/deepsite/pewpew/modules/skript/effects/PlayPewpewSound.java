package gg.deepsite.pewpew.modules.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.api.enums.SoundEvent;
import gg.deepsite.pewpew.utils.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class PlayPewpewSound extends Effect {

	static {
		Skript.registerEffect(PlayPewpewSound.class,
				"play [the] pewpew sound %string% (for|to) %players%",
				"play [the] pewpew sound %string% at %locations%");
	}

	private Expression<String> sound;
	@Nullable
	private Expression<Player> players;
	@Nullable
	private Expression<Location> locations;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		sound = (Expression<String>) expressions[0];
		if (matchedPattern == 0) players = (Expression<Player>) expressions[1];
		else locations = (Expression<Location>) expressions[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		String path = sound.getSingle(event);
		if (path == null) return;
		SoundEvent soundEvent = SoundEvent.fromPath(path);
		if (soundEvent == null) return;

		if (players != null) {
			for (Player player : players.getArray(event)) Sounds.at(player, soundEvent);
			return;
		}
		if (locations == null) return;
		for (Location location : locations.getArray(event)) {
			if (location.getWorld() != null) Sounds.at(location.getWorld(), location, soundEvent);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "play pewpew sound " + sound.toString(event, debug);
	}
}
