package gg.deepsite.pewpew.shooting.recoil;

public interface RotationSink {

	boolean alive();

	float yaw();

	float pitch();

	boolean applyRelative(float deltaYaw, float deltaPitch);

	void setRotation(float yaw, float pitch);
}
