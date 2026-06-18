package gg.deepsite.pewpew.modules.dummy.listeners;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import gg.deepsite.pewpew.modules.dummy.menu.DummyArmorMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DummyListener implements Listener {

	private static final NamespacedKey DUMMY_KEY = new NamespacedKey("pewpew", "dummy");
	private static final NamespacedKey TOTAL_KEY = new NamespacedKey("pewpew", "dummy_total");
	private static final double DUMMY_HEALTH = 1000.0;
	private static final long TOTAL_RESET_TICKS = 40L;

	private final Map<UUID, Long> lastHit = new ConcurrentHashMap<>();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity dummy) || !isDummy(dummy)) return;

		if (event.isApplicable(EntityDamageEvent.DamageModifier.BLOCKING)
				&& event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) != 0.0) {
			keepAlive(dummy);
			return;
		}

		double damage = event.getFinalDamage();

		PersistentDataContainer pdc = dummy.getPersistentDataContainer();
		double total = pdc.getOrDefault(TOTAL_KEY, PersistentDataType.DOUBLE, 0.0) + damage;
		pdc.set(TOTAL_KEY, PersistentDataType.DOUBLE, total);

		spawnDamageIndicator(dummy, damage);

		if (event.getDamageSource().getCausingEntity() instanceof Player player) {
			player.sendActionBar(ChatUtils.format(
					"<color>● <gray>Damage: <color>%1 <dark_gray>┃ <gray>Total: <color>%2",
					ChatUtils.PRIMARY,
					String.format("%.1f", damage),
					String.format("%.1f", total)));
		}

		scheduleTotalReset(dummy);
		keepAlive(dummy);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInteract(PlayerInteractEntityEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) return;
		if (!(event.getRightClicked() instanceof LivingEntity dummy) || !isDummy(dummy)) return;

		Player player = event.getPlayer();
		if (!player.isSneaking()) return;
		if (!player.getInventory().getItemInMainHand().getType().isAir()) return;

		event.setCancelled(true);
		new DummyArmorMenu(dummy, player).open(player);
	}

	private void scheduleTotalReset(LivingEntity dummy) {
		UUID id = dummy.getUniqueId();
		long stamp = System.nanoTime();
		lastHit.put(id, stamp);
		Bukkit.getScheduler().runTaskLater(PewpewPlugin.getInstance(), () -> {
			if (!Long.valueOf(stamp).equals(lastHit.get(id))) return;
			lastHit.remove(id);
			if (dummy.isValid()) {
				dummy.getPersistentDataContainer().set(TOTAL_KEY, PersistentDataType.DOUBLE, 0.0);
			}
		}, TOTAL_RESET_TICKS);
	}

	public static Mannequin spawnDummy(Location location) {
		World world = location.getWorld();
		return world.spawn(location, Mannequin.class, dummy -> {
			dummy.setAI(false);
			dummy.setSilent(true);
			dummy.setInvulnerable(false);
			dummy.addPotionEffect(new PotionEffect(
					PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));
			dummy.setPersistent(true);
			dummy.setCustomNameVisible(true);
			dummy.customName(ChatUtils.format("<color>Damage Dummy", ChatUtils.PRIMARY));

			AttributeInstance maxHealth = dummy.getAttribute(Attribute.MAX_HEALTH);
			if (maxHealth != null) {
				maxHealth.setBaseValue(DUMMY_HEALTH);
				dummy.setHealth(DUMMY_HEALTH);
			}

			dummy.getPersistentDataContainer().set(DUMMY_KEY, PersistentDataType.BYTE, (byte) 1);
		});
	}

	public static int clearDummies() {
		int count = 0;
		for (World world : Bukkit.getWorlds()) {
			for (Entity entity : world.getEntities()) {
				if (isDummy(entity)) {
					entity.remove();
					count++;
				}
			}
		}
		return count;
	}

	private static boolean isDummy(Entity entity) {
		return entity.getPersistentDataContainer().has(DUMMY_KEY, PersistentDataType.BYTE);
	}

	private void spawnDamageIndicator(LivingEntity dummy, double damage) {
		Location location = dummy.getEyeLocation().add(0, 0.5, 0);
		TextDisplay display = dummy.getWorld().spawn(location, TextDisplay.class, text -> {
			text.text(ChatUtils.format("<error>-%1", String.format("%.1f", damage)));
			text.setBillboard(Display.Billboard.CENTER);
			text.setSeeThrough(true);
			text.setShadowed(true);
			text.setDefaultBackground(false);
			text.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
		});
		Bukkit.getScheduler().runTaskLater(PewpewPlugin.getInstance(), display::remove, 20L);
	}

	private void keepAlive(LivingEntity dummy) {
		Bukkit.getScheduler().runTask(PewpewPlugin.getInstance(), () -> {
			if (!dummy.isValid()) return;
			AttributeInstance maxHealth = dummy.getAttribute(Attribute.MAX_HEALTH);
			dummy.setHealth(maxHealth != null ? maxHealth.getValue() : DUMMY_HEALTH);
			dummy.setFireTicks(0);
			updateShield(dummy);
		});
	}

	public static void updateShield(LivingEntity dummy) {
		EntityEquipment equipment = dummy.getEquipment();
		if (equipment == null) return;

		EquipmentSlot slot = null;
		if (equipment.getItemInOffHand().getType() == Material.SHIELD) {
			slot = EquipmentSlot.OFF_HAND;
		} else if (equipment.getItemInMainHand().getType() == Material.SHIELD) {
			slot = EquipmentSlot.HAND;
		}

		if (slot == null) {
			if (dummy.hasActiveItem()) dummy.clearActiveItem();
			return;
		}

		dummy.setShieldBlockingDelay(0);
		if (!dummy.hasActiveItem() || dummy.getActiveItemHand() != slot) {
			dummy.startUsingItem(slot);
		}
		dummy.setActiveItemRemainingTime(72000);
	}
}
