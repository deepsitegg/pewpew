package gg.deepsite.pewpew.modules.weapons.shooting.recoil;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class RecoilController {

    private static final float EPSILON = 0.01f;

    private final Player player;
    private final RecoilProfile profile;

    private float currentYaw;
    private float currentPitch;
    private float targetYaw;
    private float targetPitch;
    private float residualYaw;
    private float residualPitch;

    public RecoilController(@NotNull Player player, @NotNull RecoilProfile profile) {
        this.player = player;
        this.profile = profile;
    }

    public void kick(double verticalDegrees) {
        if (verticalDegrees <= 0) return;

        float sway = (float) ((ThreadLocalRandom.current().nextDouble() * 2 - 1)
                * verticalDegrees * profile.getHorizontalRatio());
        targetPitch -= (float) verticalDegrees;
        targetYaw += sway;

        float length = (float) Math.sqrt(targetYaw * targetYaw + targetPitch * targetPitch);
        float max = profile.getMaxAccum();
        if (max > 0 && length > max) {
            float scale = max / length;
            targetYaw *= scale;
            targetPitch *= scale;
        }

        residualYaw = targetYaw;
        residualPitch = targetPitch;
    }

    public boolean tick() {
        if (!player.isOnline() || player.isDead()) return true;

        float oldYaw = currentYaw;
        float oldPitch = currentPitch;

        targetYaw *= (1f - profile.getDamping());
        targetPitch *= (1f - profile.getDamping());

        currentYaw = lerp(currentYaw, targetYaw, profile.getSmoothingFactor());
        currentPitch = lerp(currentPitch, targetPitch, profile.getSmoothingFactor());

        if (profile.getRecovery() > 0f) {
            float recoveryYaw = residualYaw * (1f - profile.getRecoveryPercentage());
            float recoveryPitch = residualPitch * (1f - profile.getRecoveryPercentage());
            currentYaw = moveTowards(currentYaw, recoveryYaw, profile.getRecovery());
            currentPitch = moveTowards(currentPitch, recoveryPitch, profile.getRecovery());
        }

        float deltaYaw = (currentYaw - oldYaw) * profile.getRecoilSpeed();
        float deltaPitch = (currentPitch - oldPitch) * profile.getRecoilSpeed();

        if (Math.abs(deltaYaw) > EPSILON || Math.abs(deltaPitch) > EPSILON) {
            Location loc = player.getLocation();
            float pitch = Math.max(-90f, Math.min(90f, loc.getPitch() + deltaPitch));
            player.setRotation(loc.getYaw() + deltaYaw, pitch);
        }

        return settled();
    }

    private boolean settled() {
        return Math.abs(currentYaw) < EPSILON && Math.abs(currentPitch) < EPSILON
                && Math.abs(targetYaw) < EPSILON && Math.abs(targetPitch) < EPSILON;
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    private static float moveTowards(float current, float target, float maxDelta) {
        float diff = target - current;
        if (Math.abs(diff) <= maxDelta) return target;
        return current + Math.signum(diff) * maxDelta;
    }
}
