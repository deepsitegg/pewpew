package gg.deepsite.pewpew.animation;

import gg.deepsite.pewpew.modules.weapons.animation.GunModels;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GunModelsComposeTest {

	private static final String BASE = "pewpew:rifle";

	@Test
	void idleUsesBaseModel() {
		assertEquals(BASE, GunModels.compose(BASE, null, null, null));
	}

	@Test
	void magazineAndStatesStackInOrder() {
		assertEquals("pewpew:rifle_mag30", GunModels.compose(BASE, "_mag30", null, null));
		assertEquals("pewpew:rifle_mag30_aim", GunModels.compose(BASE, "_mag30", "_aim", null));
		assertEquals("pewpew:rifle_mag30_shoot", GunModels.compose(BASE, "_mag30", null, "_shoot"));
		assertEquals("pewpew:rifle_mag30_aim_shoot", GunModels.compose(BASE, "_mag30", "_aim", "_shoot"));
	}

	@Test
	void aimSuffixIgnoredWhenNotScoped() {
		assertEquals("pewpew:rifle_shoot", GunModels.compose(BASE, null, null, "_shoot"));
	}

	@Test
	void customModelDataSumsTheSameStates() {
		assertEquals(0, GunModels.composeData(0, 0, 0, 0));
		assertEquals(1030, GunModels.composeData(1000, 30, 0, 0));
		assertEquals(1031, GunModels.composeData(1000, 30, 1, 0));
		assertEquals(1033, GunModels.composeData(1000, 30, 1, 2));
	}

	@Test
	void absoluteFrameWins() {
		assertEquals("pewpew:rifle_fire1", GunModels.compose(BASE, "_mag30", "_aim", "pewpew:rifle_fire1"));
	}
}
