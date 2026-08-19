package gg.deepsite.pewpew.modules.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.events.PewpewReloadCompleteEvent;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.ammo.AmmoUtil;
import gg.deepsite.pewpew.modules.weapons.attachment.AttachmentUtil;
import gg.deepsite.pewpew.modules.weapons.lore.GunLoreRenderer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class ExprPewpewAmmo extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprPewpewAmmo.class, Number.class, ExpressionType.COMBINED,
				"[the] pewpew ammo [of %-players%]");
	}

	@Nullable
	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		if (players == null && !getParser().isCurrentEvent(PewpewReloadCompleteEvent.class)) {
			Skript.error("'pewpew ammo' needs a player outside of a pewpew reload complete event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event event) {
		if (players == null) {
			return event instanceof PewpewReloadCompleteEvent reload
					? new Number[]{reload.getAmmo()} : new Number[0];
		}

		Player[] targets = players.getArray(event);
		Number[] result = new Number[targets.length];
		for (int i = 0; i < targets.length; i++) {
			ItemStack held = targets[i].getInventory().getItemInMainHand();
			result[i] = gunOf(held) != null ? AmmoUtil.get(held) : 0;
		}
		return result;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (players == null) return null;
		return switch (mode) {
			case SET, ADD, REMOVE, DELETE, RESET -> new Class[]{Number.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (players == null) return;
		double value = (delta != null && delta.length > 0 && delta[0] instanceof Number number) ? number.doubleValue() : 0;

		for (Player player : players.getArray(event)) {
			ItemStack held = player.getInventory().getItemInMainHand();
			PewpewGunItem gun = gunOf(held);
			if (gun == null) continue;

			int max = AttachmentUtil.effectiveMaxAmmo(gun, held);
			int current = AmmoUtil.get(held);
			int updated = switch (mode) {
				case SET -> (int) value;
				case ADD -> current + (int) value;
				case REMOVE -> current - (int) value;
				case DELETE -> 0;
				case RESET -> max;
				default -> current;
			};

			AmmoUtil.set(held, Math.max(0, Math.min(max, updated)));
			GunLoreRenderer.apply(held, gun);
			player.getInventory().setItemInMainHand(held);
		}
	}

	@Nullable
	private static PewpewGunItem gunOf(ItemStack stack) {
		ItemsModule items = PewpewPlugin.getModuleManager().get(ItemsModule.class);
		if (items == null) return null;
		PewPewItem item = items.fromItemStack(stack);
		return item instanceof PewpewGunItem gun ? gun : null;
	}

	@Override
	public boolean isSingle() {
		return players == null || players.isSingle();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pewpew ammo";
	}
}
