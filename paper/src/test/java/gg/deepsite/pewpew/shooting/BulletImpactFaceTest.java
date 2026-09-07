package gg.deepsite.pewpew.shooting;

import gg.deepsite.pewpew.modules.weapons.shooting.BulletImpacts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class BulletImpactFaceTest {

	@Test
	void picksTheFaceTheShotEntered() {
		assertArrayEquals(new double[]{1, 0, 0}, BulletImpacts.faceNormal(11.0, 5.3, 3.4, 10, 5, 3));
		assertArrayEquals(new double[]{-1, 0, 0}, BulletImpacts.faceNormal(10.0, 5.3, 3.4, 10, 5, 3));
		assertArrayEquals(new double[]{0, 1, 0}, BulletImpacts.faceNormal(10.4, 6.0, 3.4, 10, 5, 3));
		assertArrayEquals(new double[]{0, -1, 0}, BulletImpacts.faceNormal(10.4, 5.0, 3.4, 10, 5, 3));
		assertArrayEquals(new double[]{0, 0, 1}, BulletImpacts.faceNormal(10.4, 5.4, 4.0, 10, 5, 3));
		assertArrayEquals(new double[]{0, 0, -1}, BulletImpacts.faceNormal(10.6, 5.6, 3.0, 10, 5, 3));
	}

	@Test
	void centreHitStillPicksAFace() {
		assertArrayEquals(new double[]{1, 0, 0}, BulletImpacts.faceNormal(10.5, 5.5, 3.5, 10, 5, 3));
	}
}
