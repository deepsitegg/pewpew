package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.events.PewpewHitEvent;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.integrations.CombatTagIntegration;
import gg.deepsite.pewpew.modules.weapons.attachment.AttachmentUtil;
import gg.deepsite.pewpew.modules.weapons.shooting.recoil.RecoilManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectileShotExecutor implements ShotExecutor {

	public static final NamespacedKey PROJECTILE_WEAPON_KEY = new NamespacedKey("pewpew", "weapon_id");
	public static final NamespacedKey PROJECTILE_DAMAGE_KEY = new NamespacedKey("pewpew", "weapon_damage");

	private final RecoilManager recoilManager;

	public ProjectileShotExecutor(@NotNull RecoilManager recoilManager) {
		this.recoilManager = recoilManager;
	}

	@Override
	public void execute(@NotNull Player shooter, @NotNull PewpewGunItem gun, @NotNull ItemStack weapon) {
		double recoilMultiplier = AttachmentUtil.recoilMultiplier(weapon);
		boolean scoped = ScopeState.isScoped(shooter);
		double spread = gun.getSpread() * recoilMultiplier;
		if (scoped) spread *= AttachmentUtil.aimSpreadMultiplier(weapon);
		double damage = AttachmentUtil.effectiveDamage(gun, weapon);
		boolean gravity = gun.getBulletDrop() > 0;
		Vector aim = shooter.getEyeLocation().getDirection();
		int pellets = Math.max(1, gun.getBulletCount());

		for (int pellet = 0; pellet < pellets; pellet++) {
			Vector velocity = Ballistics.applySpread(aim, spread).multiply(gun.getProjectileSpeed());
			Snowball projectile = shooter.launchProjectile(Snowball.class, velocity);
			projectile.setGravity(gravity);
			projectile.getPersistentDataContainer()
					.set(PROJECTILE_WEAPON_KEY, PersistentDataType.STRING, gun.getId());
			projectile.getPersistentDataContainer()
					.set(PROJECTILE_DAMAGE_KEY, PersistentDataType.DOUBLE, damage);
			if (gun.getTrailParticle() != null) trail(projectile, gun.getTrailParticle());
		}

		double recoil = gun.getRecoil() * recoilMultiplier;
		if (scoped) recoil *= AttachmentUtil.aimRecoilMultiplier(weapon);
		recoilManager.kick(shooter, recoil);
	}

	private void trail(Snowball projectile, Particle particle) {
		Bukkit.getScheduler().runTaskTimer(PewpewPlugin.getInstance(), task -> {
			if (projectile.isDead() || !projectile.isValid()) {
				task.cancel();
				return;
			}
			projectile.getWorld().spawnParticle(particle, projectile.getLocation(), 1, 0, 0, 0, 0);
		}, 1L, 1L);
	}

	@Nullable
	public static String getWeaponId(@NotNull Snowball projectile) {
		return projectile.getPersistentDataContainer()
				.get(PROJECTILE_WEAPON_KEY, PersistentDataType.STRING);
	}

	public void handleHit(@NotNull Snowball projectile, @NotNull PewpewGunItem gun, @Nullable LivingEntity target) {
		Ballistics.impact(gun.getImpactParticle(), projectile.getLocation());
		if (target == null) return;
		Double stored = projectile.getPersistentDataContainer()
				.get(PROJECTILE_DAMAGE_KEY, PersistentDataType.DOUBLE);
		double damage = stored != null ? stored : gun.getBaseDamage();

		boolean headshot = gun.getHeadshotMultiplier() > 1.0
				&& Ballistics.isHeadshot(target, projectile.getLocation().getY());
		boolean crit = Ballistics.rollCrit(gun.getCritChance());
		double distance = projectile.getOrigin() != null ? projectile.getLocation().distance(projectile.getOrigin()) : 0;
		if (distance > 0) {
			damage *= Ballistics.falloffMultiplier(distance, gun.getFalloffStart(), gun.getFalloffEnd(), gun.getFalloffMinMultiplier());
		}
		if (headshot) damage *= gun.getHeadshotMultiplier();
		if (crit) damage *= gun.getCritMultiplier();

		Player shooterPlayer = projectile.getShooter() instanceof Player p ? p : null;
		PewpewHitEvent hitEvent = new PewpewHitEvent(shooterPlayer, gun, target, damage, headshot, crit, distance);
		if (!hitEvent.callEvent()) return;
		damage = hitEvent.getDamage();

		Entity causing = shooterPlayer != null ? shooterPlayer : projectile;
		if (shooterPlayer != null) {
			GunHitTracker.record(target, shooterPlayer, gun);
			if (target instanceof Player victim) CombatTagIntegration.tag(victim, shooterPlayer);
		}
		Ballistics.dealProjectileDamage(target, damage, causing, projectile);
		if (shooterPlayer != null) {
			Ballistics.applyKnockback(target, shooterPlayer, gun.getKnockback(), gun.getSelfKnockback());
		}
		Ballistics.disableShield(target, gun.getShieldDisableTime());
		Ballistics.applyEffects(target, gun.getVictimEffects());
		if (shooterPlayer != null) {
			Ballistics.applyEffects(shooterPlayer, gun.getShooterEffects());
			Ballistics.hitFeedback(shooterPlayer, gun, target, damage, headshot);
			if (crit) Ballistics.critEffect(shooterPlayer, target);
		}
	}
}
