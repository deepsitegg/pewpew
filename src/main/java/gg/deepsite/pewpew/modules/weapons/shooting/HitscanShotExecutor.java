package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.api.events.PewpewHitBlockEvent;
import gg.deepsite.pewpew.api.events.PewpewHitEvent;
import gg.deepsite.pewpew.api.objects.PewpewAmmoItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.modules.weapons.ammo.AmmoUtil;
import gg.deepsite.pewpew.integrations.CombatTagIntegration;
import gg.deepsite.pewpew.modules.weapons.attachment.AttachmentUtil;
import gg.deepsite.pewpew.modules.weapons.shooting.recoil.RecoilManager;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HitscanShotExecutor implements ShotExecutor {

	private static final double DROP_STEP = 0.5;

	private final RecoilManager recoilManager;

	public HitscanShotExecutor(@NotNull RecoilManager recoilManager) {
		this.recoilManager = recoilManager;
	}

	@Override
	public void execute(@NotNull Player shooter, @NotNull PewpewGunItem gun, @NotNull ItemStack weapon) {
		Location eye = shooter.getEyeLocation();
		double recoilMultiplier = AttachmentUtil.recoilMultiplier(weapon);
		boolean scoped = ScopeState.isScoped(shooter);
		double spread = Spread.effective(shooter, gun, weapon, scoped);
		double range = AttachmentUtil.effectiveRange(gun, weapon);
		PewpewAmmoItem ammo = AmmoUtil.statsOf(weapon);
		double damage = AttachmentUtil.effectiveDamage(gun, weapon) * AmmoUtil.damageMultiplier(ammo);
		int pierce = AmmoUtil.penetration(ammo);
		int pellets = Math.max(1, gun.getBulletCount());

		for (int pellet = 0; pellet < pellets; pellet++) {
			Vector direction = Ballistics.applySpread(eye.getDirection(), spread);
			if (gun.getBulletDrop() > 0) {
				fireBallistic(shooter, gun, eye, direction, range, gun.getBulletDrop(), damage);
			} else {
				fireStraight(shooter, gun, eye, direction, range, damage, pierce);
			}
		}

		Spread.addShot(shooter, gun);

		double recoil = gun.getRecoil() * recoilMultiplier;
		if (scoped) recoil *= AttachmentUtil.aimRecoilMultiplier(weapon);
		recoilManager.kick(shooter, gun.getRecoilProfile(), recoil);
		Ballistics.applySelfKnockback(shooter, gun.getSelfKnockback());
	}

	private void fireStraight(Player shooter, PewpewGunItem gun, Location eye, Vector direction, double range,
	                          double damage, int pierce) {
		Set<UUID> hit = new HashSet<>();
		Location origin = eye.clone();
		double remaining = range;
		double traveled = 0;
		int left = pierce;

		while (remaining > 0) {
			RayTraceResult result = shooter.getWorld().rayTrace(
					origin, direction, remaining,
					FluidCollisionMode.NEVER, true, 0.1,
					entity -> entity instanceof LivingEntity && !entity.equals(shooter)
							&& !hit.contains(entity.getUniqueId())
			);

			if (result == null) {
				spawnTracer(eye, direction, range, gun.getTrailParticle());
				return;
			}

			double step = result.getHitPosition().distance(origin.toVector());
			traveled += step;
			Ballistics.impact(gun.getImpactParticle(), result.getHitPosition().toLocation(shooter.getWorld()));

			if (!(result.getHitEntity() instanceof LivingEntity target)) {
				new PewpewHitBlockEvent(shooter, gun, result.getHitBlock(),
						result.getHitPosition().toLocation(shooter.getWorld()), traveled).callEvent();
				spawnTracer(eye, direction, traveled, gun.getTrailParticle());
				return;
			}

			hit.add(target.getUniqueId());
			applyHit(shooter, gun, target, result.getHitPosition().getY(), traveled, damage);

			if (left-- <= 0) {
				spawnTracer(eye, direction, traveled, gun.getTrailParticle());
				return;
			}

			origin = result.getHitPosition().toLocation(shooter.getWorld());
			origin.add(direction.clone().multiply(0.01));
			remaining = range - traveled;
		}

		spawnTracer(eye, direction, range, gun.getTrailParticle());
	}

	private void fireBallistic(Player shooter, PewpewGunItem gun, Location eye, Vector direction, double range, double drop,
	                           double damage) {
		Location point = eye.clone();
		Vector velocity = direction.clone();
		double traveled = 0;

		while (traveled < range) {
			Vector dir = velocity.clone().normalize();
			RayTraceResult result = shooter.getWorld().rayTrace(
					point, dir, DROP_STEP,
					FluidCollisionMode.NEVER, true, 0.1,
					entity -> entity instanceof LivingEntity && !entity.equals(shooter)
			);
			if (result != null) {
				Ballistics.impact(gun.getImpactParticle(), result.getHitPosition().toLocation(shooter.getWorld()));
				if (result.getHitEntity() instanceof LivingEntity target) {
					applyHit(shooter, gun, target, result.getHitPosition().getY(), traveled, damage);
				} else {
					new PewpewHitBlockEvent(shooter, gun, result.getHitBlock(),
							result.getHitPosition().toLocation(shooter.getWorld()), traveled).callEvent();
				}
				return;
			}

			point.add(dir.clone().multiply(DROP_STEP));
			if (gun.getTrailParticle() != null)
				point.getWorld().spawnParticle(gun.getTrailParticle(), point, 1, 0, 0, 0, 0);
			velocity = dir.setY(dir.getY() - drop * DROP_STEP);
			traveled += DROP_STEP;
		}
	}

	private void applyHit(Player shooter, PewpewGunItem gun, LivingEntity target, double hitY, double distance, double damage) {
		boolean headshot = gun.getHeadshotMultiplier() > 1.0 && Ballistics.isHeadshot(target, hitY);
		boolean crit = Ballistics.rollCrit(gun.getCritChance());
		damage *= Ballistics.falloffMultiplier(distance, gun.getFalloffStart(), gun.getFalloffEnd(), gun.getFalloffMinMultiplier());
		if (headshot) damage *= gun.getHeadshotMultiplier();
		if (crit) damage *= gun.getCritMultiplier();

		PewpewHitEvent hitEvent = new PewpewHitEvent(shooter, gun, target, damage, headshot, crit, distance);
		if (!hitEvent.callEvent()) return;
		damage = hitEvent.getDamage();

		GunHitTracker.record(target, shooter, gun);
		if (target instanceof Player victim) CombatTagIntegration.tag(victim, shooter);
		Ballistics.dealProjectileDamage(target, damage, shooter, shooter, gun.getDamageType());
		Ballistics.applyKnockback(target, shooter, gun.getKnockback());
		Ballistics.disableShield(target, gun.getShieldDisableTime());
		Ballistics.applyEffects(target, gun.getVictimEffects());
		Ballistics.applyEffects(shooter, gun.getShooterEffects());
		Ballistics.hitFeedback(shooter, gun, target, damage, headshot);
		if (crit) Ballistics.critEffect(shooter, target);
	}

	private void spawnTracer(Location eye, Vector direction, double length, Particle particle) {
		if (particle == null) return;
		Location point = eye.clone().add(direction.clone().multiply(1.5));
		for (double traveled = 1.5; traveled < length; traveled += 1.0) {
			eye.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);
			point.add(direction);
		}
	}
}
