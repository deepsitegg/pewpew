package gg.deepsite.pewpew.utils.item;

import gg.deepsite.pewpew.api.enums.HoldPose;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewMagazineItem;
import gg.deepsite.pewpew.api.objects.attachment.PewpewAttachment;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.lore.AttachmentLoreRenderer;
import gg.deepsite.pewpew.modules.weapons.lore.MagazineLoreRenderer;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ChargedProjectiles;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@UtilityClass
public class ItemFactory {

	@NotNull
	public static ItemStack build(@NotNull PewPewItem item) {
		boolean crossbow = item.getHoldPose() == HoldPose.CROSSBOW;
		ItemBuilder builder = new ItemBuilder(crossbow ? Material.CROSSBOW : Material.PAPER).setName(item.getName());

		if (item.getLore() != null) item.getLore().forEach(builder::addLoreLine);
		if (item.getItemModel() != null) builder.setItemModel(item.getItemModel());
		if (item.getCustomModelData() != 0) builder.setCustomModelData(item.getCustomModelData());
		if (item.isHideItemFlags()) builder.setItemFlag(ItemFlag.values());
		if (item.getMaxStack() > 0) builder.setMaxStackSize(item.getMaxStack());

		ItemStack stack = ItemsModule.stamp(builder.toItemStack(), item);
		if (crossbow) {
			stack.setData(DataComponentTypes.CHARGED_PROJECTILES,
					ChargedProjectiles.chargedProjectiles(List.of(ItemStack.of(Material.AIR))));
		}
		if (item instanceof PewpewAttachment attachment) AttachmentLoreRenderer.apply(stack, attachment);
		if (item instanceof PewpewMagazineItem magazine) MagazineLoreRenderer.apply(stack, magazine);
		return stack;
	}
}
