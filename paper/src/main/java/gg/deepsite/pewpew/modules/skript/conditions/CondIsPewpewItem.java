package gg.deepsite.pewpew.modules.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewAmmoItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.PewpewThrowableItem;
import gg.deepsite.pewpew.api.objects.attachment.PewpewAttachment;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation", "removal"})
public class CondIsPewpewItem extends Condition {

	static {
		Skript.registerCondition(CondIsPewpewItem.class,
				"%itemstacks% (is|are) [a] pewpew (0:item|1:gun|2:throwable|3:attachment|4:ammo)",
				"%itemstacks% (is not|are not) [a] pewpew (0:item|1:gun|2:throwable|3:attachment|4:ammo)");
	}

	private Expression<ItemStack> items;
	private int kind;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		items = (Expression<ItemStack>) expressions[0];
		kind = parseResult.mark;
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return items.check(event, this::matches, isNegated());
	}

	private boolean matches(ItemStack stack) {
		ItemsModule module = PewpewPlugin.getModuleManager().get(ItemsModule.class);
		if (module == null || stack == null) return false;

		PewPewItem item = module.fromItemStack(stack);
		if (item == null) return false;

		return switch (kind) {
			case 1 -> item instanceof PewpewGunItem;
			case 2 -> item instanceof PewpewThrowableItem;
			case 3 -> item instanceof PewpewAttachment;
			case 4 -> item instanceof PewpewAmmoItem;
			default -> true;
		};
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return items + (isNegated() ? " is not" : " is") + " a pewpew item";
	}
}
