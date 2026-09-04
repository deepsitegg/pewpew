package gg.deepsite.pewpew.shooting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpreadConeTest {

	private static double angleBetween(Vec3 a, Vec3 b) {
		return Math.toDegrees(Math.acos(Math.min(1, Math.max(-1, a.normalize().dot(b.normalize())))));
	}

	@Test
	void spreadStaysOnTheConeRegardlessOfAim() {
		Vec3[] aims = {
				new Vec3(1, 0, 0),
				new Vec3(0, 1, 0),
				new Vec3(0, -1, 0),
				new Vec3(0.3, 0.9, -0.2),
		};
		for (Vec3 aim : aims) {
			for (int step = 0; step < 8; step++) {
				double azimuth = step * Math.PI / 4;
				Vec3 out = SpreadMath.cone(aim, Math.toRadians(5), azimuth);
				assertEquals(5.0, angleBetween(aim, out), 1e-6,
						"deviation must equal the cone angle when aiming " + aim);
				assertEquals(1.0, out.length(), 1e-9, "result must be normalized");
			}
		}
	}

	@Test
	void spreadCoversEveryDirectionAroundTheAim() {
		Vec3 aim = new Vec3(0, 1, 0);
		Vec3 first = SpreadMath.cone(aim, Math.toRadians(5), 0);
		Vec3 opposite = SpreadMath.cone(aim, Math.toRadians(5), Math.PI);
		assertTrue(angleBetween(first, opposite) > 9.9,
				"opposite azimuths must land on opposite sides of the cone");
	}

	@Test
	void zeroAngleIsUnchanged() {
		Vec3 aim = new Vec3(1, 2, 3).normalize();
		assertEquals(0.0, angleBetween(aim, SpreadMath.cone(aim, 0, 1.2)), 1e-9);
	}
}
