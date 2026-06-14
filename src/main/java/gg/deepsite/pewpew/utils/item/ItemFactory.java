package gg.deepsite.pewpew.utils.item;

import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.attachment.PewpewAttachment;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.lore.AttachmentLoreRenderer;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class ItemFactory {

	@NotNull
	public static ItemStack build(@NotNull PewPewItem item) {
		ItemBuilder builder = new ItemBuilder(Material.PAPER).setName(item.getName());

		if (item.getLore() != null)         item.getLore().forEach(builder::addLoreLine);
		if (item.getItemModel() != null)    builder.setItemModel(item.getItemModel());
		if (item.getCustomModelData() != 0) builder.setCustomModelData(item.getCustomModelData());
		if (item.isHideItemFlags())         builder.setItemFlag(ItemFlag.values());
		if (item.getMaxStack() > 0)         builder.setMaxStackSize(item.getMaxStack());

		ItemStack stack = ItemsModule.stamp(builder.toItemStack(), item);
		if (item instanceof PewpewAttachment attachment) AttachmentLoreRenderer.apply(stack, attachment);
		return stack;
	}
}
