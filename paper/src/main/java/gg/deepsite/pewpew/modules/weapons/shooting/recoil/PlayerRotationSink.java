package gg.deepsite.pewpew.modules.weapons.shooting.recoil;

import gg.deepsite.pewpew.shooting.recoil.RotationSink;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

record PlayerRotationSink(@NotNull Player player) implements RotationSink {

	@Override
	public boolean alive() {
		return player.isOnline() && !player.isDead();
	}

	@Override
	public float yaw() {
		return player.getLocation().getYaw();
	}

	@Override
	public float pitch() {
		return player.getLocation().getPitch();
	}

	@Override
	public boolean applyRelative(float deltaYaw, float deltaPitch) {
		return RelativeRotation.apply(player, deltaYaw, deltaPitch);
	}

	@Override
	public void setRotation(float yaw, float pitch) {
		player.setRotation(Location.normalizeYaw(yaw), pitch);
	}
}
