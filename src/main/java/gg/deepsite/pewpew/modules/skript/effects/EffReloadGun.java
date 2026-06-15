package gg.deepsite.pewpew.modules.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.WeaponsModule;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * {@code (force|make) %players% [to] reload [their] [pewpew] gun} — starts a
 * reload on the Pewpew gun each player is holding, as if they pressed the
 * reload key. Players not holding a gun are ignored.
 */
@SuppressWarnings({"unused", "deprecation", "removal"})
public class EffReloadGun extends Effect {

	static {
		Skript.registerEffect(EffReloadGun.class,
				"(force|make) %players% [to] reload [their] [pewpew] gun");
	}

	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		WeaponsModule weapons = PewpewPlugin.getModuleManager().get(WeaponsModule.class);
		ItemsModule items = PewpewPlugin.getModuleManager().get(ItemsModule.class);
		if (weapons == null || weapons.getShootingHandler() == null || items == null) return;

		for (Player player : players.getArray(event)) {
			ItemStack held = player.getInventory().getItemInMainHand();
			PewPewItem item = items.fromItemStack(held);
			if (item instanceof PewpewGunItem gun) {
				weapons.getShootingHandler().startReload(player, gun, held);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "force " + players.toString(event, debug) + " to reload their pewpew gun";
	}
}
