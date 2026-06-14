package gg.deepsite.pewpew.modules.dummy.menu;

import com.jazzkuh.inventorylib.objects.Menu;
import com.jazzkuh.inventorylib.objects.icon.Icon;
import gg.deepsite.pewpew.utils.ChatUtils;
import gg.deepsite.pewpew.utils.item.ItemBuilder;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DummyArmorMenu extends Menu {

	private static final EquipmentSlot[] SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFF_HAND
	};
	private static final String[] LABELS = {"Helmet", "Chestplate", "Leggings", "Boots", "Off-hand"};

	private final LivingEntity dummy;
	private final Player viewer;

	public DummyArmorMenu(LivingEntity dummy, Player viewer) {
		super(ChatUtils.format("<color>Dummy Equipment", ChatUtils.PRIMARY).decoration(TextDecoration.ITALIC, false),
				1, InventoryType.HOPPER);
		this.dummy = dummy;
		this.viewer = viewer;
		build();
	}

	private void build() {
		clearItems();
		EntityEquipment equipment = dummy.getEquipment();
		for (int i = 0; i < SLOTS.length; i++) {
			ItemStack fitted = equipment != null ? equipment.getItem(SLOTS[i]) : null;
			int index = i;
			if (fitted != null && !fitted.getType().isAir()) {
				addItem(new Icon(i, fitted.clone(), true, event -> unequip(index)));
			} else {
				addItem(new Icon(i, emptyPane(LABELS[i])));
			}
		}
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		if (!event.isShiftClick()) return;
		Inventory clicked = event.getClickedInventory();
		if (clicked == null || clicked.equals(getInventory())) return;

		ItemStack stack = event.getCurrentItem();
		if (stack == null || stack.getType().isAir()) return;
		int index = slotIndexFor(stack.getType());
		if (index < 0) return;
		equip(index, stack);
	}

	private void equip(int index, ItemStack source) {
		EntityEquipment equipment = dummy.getEquipment();
		if (equipment == null) return;

		ItemStack incoming = source.clone();
		incoming.setAmount(1);
		ItemStack previous = equipment.getItem(SLOTS[index]);

		source.setAmount(source.getAmount() - 1);
		equipment.setItem(SLOTS[index], incoming);
		if (previous != null && !previous.getType().isAir()) giveBack(previous);

		viewer.playSound(viewer.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.7f, 1.4f);
		refresh();
	}

	private void unequip(int index) {
		EntityEquipment equipment = dummy.getEquipment();
		if (equipment == null) return;

		ItemStack fitted = equipment.getItem(SLOTS[index]);
		if (fitted == null || fitted.getType().isAir()) return;

		equipment.setItem(SLOTS[index], null);
		giveBack(fitted);
		viewer.playSound(viewer.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.7f, 1.2f);
		refresh();
	}

	private void refresh() {
		build();
		update();
	}

	private void giveBack(ItemStack stack) {
		viewer.getInventory().addItem(stack).values()
				.forEach(left -> viewer.getWorld().dropItem(viewer.getLocation(), left));
	}

	private int slotIndexFor(Material material) {
		String name = material.name();
		if (material == Material.SHIELD) return 4;
		if (name.endsWith("_HELMET") || material == Material.CARVED_PUMPKIN) return 0;
		if (name.endsWith("_CHESTPLATE") || material == Material.ELYTRA) return 1;
		if (name.endsWith("_LEGGINGS")) return 2;
		if (name.endsWith("_BOOTS")) return 3;
		return -1;
	}

	private ItemStack emptyPane(String label) {
		return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
				.setName("<color>" + label + " Slot")
				.addLoreLine("<dark_gray>Shift-click " + label.toLowerCase() + " from")
				.addLoreLine("<dark_gray>your inventory to fit it.")
				.toItemStack();
	}
}
