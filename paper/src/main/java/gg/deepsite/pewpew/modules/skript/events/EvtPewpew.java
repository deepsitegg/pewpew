package gg.deepsite.pewpew.modules.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import gg.deepsite.pewpew.api.events.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
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
				.description("Called when a Pewpew throwable (or grenade-launcher payload) detonates. "
						+ "event-location is the blast point. Cancellable.")
				.examples("on pewpew detonate:", "\tstrike lightning at event-location")
				.since("26.0.2");

		Skript.registerEvent("Pewpew Gun Explode", SimpleEvent.class, PewpewGunExplodeEvent.class,
						"pewpew gun explo(de|sion)")
				.description("Called when an explosive Pewpew gun projectile (rocket launcher) detonates. "
						+ "event-player is the shooter, event-location the blast point. Cancellable.")
				.examples("on pewpew gun explode:", "\tset pewpew damage to pewpew damage * 2")
				.since("26.0.2");

		Skript.registerEvent("Pewpew Kill", SimpleEvent.class, PewpewKillEvent.class, "pewpew kill[ed]")
				.description("Called when a player dies to a Pewpew gun. event-player is the killer, "
						+ "'pewpew victim' the player who died. Set 'pewpew death message' to change the broadcast.")
				.examples("on pewpew kill:", "	send \"headshot!\" to event-player")
				.since("26.1.1");

		Skript.registerEvent("Pewpew Reload Complete", SimpleEvent.class, PewpewReloadCompleteEvent.class,
						"pewpew reload(ed|[ ]complete[d]|[ ]finish[ed])")
				.description("Called when a reload finishes and rounds are actually in the magazine. "
						+ "Use 'pewpew ammo' for the new count.")
				.examples("on pewpew reload complete:", "	send action bar \"%pewpew ammo% rounds\" to event-player")
				.since("26.1.1");

		Skript.registerEvent("Pewpew Scope", SimpleEvent.class, PewpewScopeEvent.class,
						"pewpew (scope|aim)[ing] [(1¦in|2¦out)]")
				.description("Called when a player scopes in or out with a Pewpew gun. "
						+ "Use 'pewpew is scoping in' to tell the two apart. Cancellable.")
				.examples("on pewpew scope:", "	if pewpew is scoping in:", "		send \"steady...\" to event-player")
				.since("26.1.1");

		Skript.registerEvent("Pewpew Attachment", SimpleEvent.class, PewpewAttachmentEvent.class,
						"pewpew attachment [(install|remove)]")
				.description("Called when a player fits or removes an attachment in the bench. "
						+ "'pewpew attachment id' is the attachment, 'pewpew attachment slot' the slot. Cancellable.")
				.examples("on pewpew attachment:",
						"	if pewpew attachment id is \"gold_scope\":",
						"		cancel event")
				.since("26.1.1");

		Skript.registerEvent("Pewpew Hit Block", SimpleEvent.class, PewpewHitBlockEvent.class,
						"pewpew hit block")
				.description("Called when a Pewpew shot stops on a block instead of an entity. "
						+ "event-block is what was hit, event-location the impact point.")
				.examples("on pewpew hit block:", "	set event-block to air")
				.since("26.1.1");

		EventValues.registerEventValue(PewpewKillEvent.class, Player.class,
				PewpewKillEvent::getOnlineKiller, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewReloadCompleteEvent.class, Player.class,
				PewpewReloadCompleteEvent::getPlayer, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewScopeEvent.class, Player.class,
				PewpewScopeEvent::getPlayer, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewAttachmentEvent.class, Player.class,
				PewpewAttachmentEvent::getPlayer, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewHitBlockEvent.class, Player.class,
				PewpewHitBlockEvent::getShooter, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewHitBlockEvent.class, Block.class,
				PewpewHitBlockEvent::getBlock, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewHitBlockEvent.class, Location.class,
				PewpewHitBlockEvent::getLocation, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewKillEvent.class, LivingEntity.class,
				PewpewKillEvent::getVictim, EventValues.TIME_NOW);

		EventValues.registerEventValue(PewpewShootEvent.class, Player.class,
				PewpewShootEvent::getShooter, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewHitEvent.class, Player.class,
				PewpewHitEvent::getShooter, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewReloadEvent.class, Player.class,
				PewpewReloadEvent::getPlayer, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewThrowEvent.class, Player.class,
				PewpewThrowEvent::getPlayer, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewGunExplodeEvent.class, Player.class,
				PewpewGunExplodeEvent::getShooter, EventValues.TIME_NOW);
		EventValues.registerEventValue(PewpewGunExplodeEvent.class, Location.class,
				PewpewGunExplodeEvent::getLocation, EventValues.TIME_NOW);

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
