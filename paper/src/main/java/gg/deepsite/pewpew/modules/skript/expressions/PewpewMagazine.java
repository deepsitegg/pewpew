package gg.deepsite.pewpew.modules.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.api.objects.PewpewMagazineItem;
import gg.deepsite.pewpew.modules.skript.SkriptGuns;
import gg.deepsite.pewpew.modules.weapons.magazine.MagazineUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class PewpewMagazine extends SimpleExpression<String> {

	static {
		Skript.registerExpression(PewpewMagazine.class, String.class, ExpressionType.COMBINED,
				"[the] pewpew magazine [id] of %players%");
	}

	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		List<String> ids = new ArrayList<>();
		for (Player player : players.getArray(event)) {
			ItemStack held = player.getInventory().getItemInMainHand();
			if (SkriptGuns.gunOf(held) == null) continue;
			PewpewMagazineItem magazine = MagazineUtil.inserted(held);
			if (magazine != null) ids.add(magazine.getId());
		}
		return ids.toArray(new String[0]);
	}

	@Override
	public boolean isSingle() {
		return players.isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pewpew magazine of " + players.toString(event, debug);
	}
}
