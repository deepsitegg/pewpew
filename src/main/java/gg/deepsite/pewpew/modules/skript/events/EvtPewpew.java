package gg.deepsite.pewpew.modules.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import gg.deepsite.pewpew.api.events.PewpewHitEvent;
import gg.deepsite.pewpew.api.events.PewpewReloadEvent;
import gg.deepsite.pewpew.api.events.PewpewShootEvent;
import gg.deepsite.pewpew.api.events.PewpewThrowEvent;
import gg.deepsite.pewpew.api.events.PewpewThrowableDetonateEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@SuppressWarnings({"unused", "deprecation", "removal"})
public final class EvtPewpew {

	static {
		Skript.registerEvent("Pewpew Shoot", SimpleEvent.class, PewpewShootEvent.class, "pewpew shoot[ing]")
				.description("Called when a player fires a Pewpew gun, once per shot. Cancellable.")
				.examples("on pewpew shoot:", "\tsend \"pew!\" to event-player")
				.since("26.0.2");

		Skript.registerEvent("Pewpew Hit", SimpleEvent.class, PewpewHitEvent.class, "pewpew (hit|damage)")
				.description("Called when a Pewpew shot lands on a living entity. "
						+ "event-entity is the target, event-player the shooter. Cancellable.")
				.examples("on pewpew hit:", "\tset pewpew damage to pewpew damage * 2")
				.since("26.0.2");

		Skript.registerEvent("Pewpew Reload", SimpleEvent.class, PewpewReloadEvent.class, "pewpew reload[ing]")
				.description("Called when a player starts reloading a Pewpew gun. Cancellable.")
				.examples("on pewpew reload:", "\tsend action bar \"reloading...\" to event-player")
				.since("26.0.2");

		Skript.registerEvent("Pewpew Throw", SimpleEvent.class, PewpewThrowEvent.class, "pewpew throw[ing]")
				.description("Called when a player throws a Pewpew throwable. Cancellable.")
				.examples("on pewpew throw:", "\tsend \"fire in the hole!\" to event-player")
				.since("26.0.2");

		Skript.registerEvent("Pewpew Detonate", SimpleEvent.class, PewpewThrowableDetonateEvent.class,
						"pewpew (detonat(e|ion)|explo(de|sion))")
				.description("Called when a Pewpew throwable detonates. event-location is the blast point. Cancellable.")
				.examples("on pewpew detonate:", "\tstrike lightning at event-location")
				.since("26.0.2");

		EventValues.registerEventValue(PewpewShootEvent.class, Player.class,
				PewpewShootEvent::getShooter, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewHitEvent.class, Player.class,
				PewpewHitEvent::getShooter, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewReloadEvent.class, Player.class,
				PewpewReloadEvent::getPlayer, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewThrowEvent.class, Player.class,
				PewpewThrowEvent::getPlayer, EventValues.TIME_NOW);

		EventValues.registerEventValue(PewpewHitEvent.class, LivingEntity.class,
				PewpewHitEvent::getTarget, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewThrowableDetonateEvent.class, Entity.class,
				PewpewThrowableDetonateEvent::getEntity, EventValues.TIME_NOW);

		EventValues.registerEventValue(PewpewThrowableDetonateEvent.class, Location.class,
				PewpewThrowableDetonateEvent::getLocation, EventValues.TIME_NOW);
	}

	private EvtPewpew() {
	}
}
