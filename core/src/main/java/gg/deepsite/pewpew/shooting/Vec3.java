package gg.deepsite.pewpew.shooting;

import org.jetbrains.annotations.NotNull;

public record Vec3(double x, double y, double z) {

	@NotNull
	public Vec3 add(@NotNull Vec3 other) {
		return new Vec3(x + other.x, y + other.y, z + other.z);
	}

	@NotNull
	public Vec3 subtract(@NotNull Vec3 other) {
		return new Vec3(x - other.x, y - other.y, z - other.z);
	}

	@NotNull
	public Vec3 multiply(double factor) {
		return new Vec3(x * factor, y * factor, z * factor);
	}

	public double dot(@NotNull Vec3 other) {
		return x * other.x + y * other.y + z * other.z;
	}

	@NotNull
	public Vec3 cross(@NotNull Vec3 other) {
		return new Vec3(
				y * other.z - z * other.y,
				z * other.x - x * other.z,
				x * other.y - y * other.x);
	}

	public double lengthSquared() {
		return x * x + y * y + z * z;
	}

	public double length() {
		return Math.sqrt(lengthSquared());
	}

	@NotNull
	public Vec3 normalize() {
		double length = length();
		return length == 0 ? this : multiply(1.0 / length);
	}

	@NotNull
	public Vec3 rotateAroundX(double radians) {
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		return new Vec3(x, y * cos - z * sin, y * sin + z * cos);
	}

	@NotNull
	public Vec3 rotateAroundY(double radians) {
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		return new Vec3(x * cos + z * sin, y, x * -sin + z * cos);
	}
}
