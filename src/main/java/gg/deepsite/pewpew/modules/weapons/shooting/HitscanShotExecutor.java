package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.api.events.PewpewHitEvent;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
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
		double spread = gun.getSpread() * recoilMultiplier;
		if (scoped) spread *= AttachmentUtil.aimSpreadMultiplier(weapon);
		double range = AttachmentUtil.effectiveRange(gun, weapon);
		double damage = AttachmentUtil.effectiveDamage(gun, weapon);
		int pellets = Math.max(1, gun.getBulletCount());

		for (int pellet = 0; pellet < pellets; pellet++) {
			Vector direction = Ballistics.applySpread(eye.getDirection(), spread);
			if (gun.getBulletDrop() > 0) {
				fireBallistic(shooter, gun, eye, direction, range, gun.getBulletDrop(), damage);
			} else {
				fireStraight(shooter, gun, eye, direction, range, damage);
			}
		}

		double recoil = gun.getRecoil() * recoilMultiplier;
		if (scoped) recoil *= AttachmentUtil.aimRecoilMultiplier(weapon);
		recoilManager.kick(shooter, recoil);
	}

	private void fireStraight(Player shooter, PewpewGunItem gun, Location eye, Vector direction, double range,
							  double damage) {
		RayTraceResult result = shooter.getWorld().rayTrace(
				eye, direction, range,
				FluidCollisionMode.NEVER, true, 0.1,
				entity -> entity instanceof LivingEntity && !entity.equals(shooter)
		);

		double traceLength = result != null
				? result.getHitPosition().distance(eye.toVector())
				: range;
		spawnTracer(eye, direction, traceLength, gun.getTrailParticle());

		if (result != null) {
			Ballistics.impact(gun.getImpactParticle(), result.getHitPosition().toLocation(shooter.getWorld()));
			if (result.getHitEntity() instanceof LivingEntity target) {
				applyHit(shooter, gun, target, result.getHitPosition().getY(), traceLength, damage);
			}
		}
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
				}
				return;
			}

			point.add(dir.clone().multiply(DROP_STEP));
			if (gun.getTrailParticle() != null) point.getWorld().spawnParticle(gun.getTrailParticle(), point, 1, 0, 0, 0, 0);
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
		Ballistics.dealProjectileDamage(target, damage, shooter, shooter);
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
