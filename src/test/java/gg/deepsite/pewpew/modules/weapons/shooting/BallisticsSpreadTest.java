package gg.deepsite.pewpew.modules.weapons.shooting;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BallisticsSpreadTest {

	private static double angleBetween(Vector a, Vector b) {
		return Math.toDegrees(Math.acos(Math.min(1, Math.max(-1, a.clone().normalize().dot(b.clone().normalize())))));
	}

	@Test
	void spreadStaysOnTheConeRegardlessOfAim() {
		Vector[] aims = {
				new Vector(1, 0, 0),
				new Vector(0, 1, 0),
				new Vector(0, -1, 0),
				new Vector(0.3, 0.9, -0.2),
		};
		for (Vector aim : aims) {
			for (int step = 0; step < 8; step++) {
				double azimuth = step * Math.PI / 4;
				Vector out = Ballistics.spread(aim, Math.toRadians(5), azimuth);
				assertEquals(5.0, angleBetween(aim, out), 1e-6,
						"deviation must equal the cone angle when aiming " + aim);
				assertEquals(1.0, out.length(), 1e-9, "result must be normalized");
			}
		}
	}

	@Test
	void spreadCoversEveryDirectionAroundTheAim() {
		Vector aim = new Vector(0, 1, 0);
		Vector first = Ballistics.spread(aim, Math.toRadians(5), 0);
		Vector opposite = Ballistics.spread(aim, Math.toRadians(5), Math.PI);
		assertTrue(angleBetween(first, opposite) > 9.9,
				"opposite azimuths must land on opposite sides of the cone");
	}

	@Test
	void zeroAngleIsUnchanged() {
		Vector aim = new Vector(1, 2, 3).normalize();
		assertEquals(0.0, angleBetween(aim, Ballistics.spread(aim, 0, 1.2)), 1e-9);
	}
}
