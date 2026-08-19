package gg.deepsite.pewpew.modules.weapons.shooting.recoil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecoilPatternTest {

	@Test
	void walksThePatternShotByShot() {
		for (int shot = 0; shot < 5; shot++) {
			assertEquals(shot, RecoilController.patternIndex(shot, 5, false));
		}
	}

	@Test
	void holdsTheLastStepWhenTheMagOutlastsThePattern() {
		assertEquals(4, RecoilController.patternIndex(5, 5, false));
		assertEquals(4, RecoilController.patternIndex(99, 5, false));
	}

	@Test
	void wrapsWhenLooping() {
		assertEquals(0, RecoilController.patternIndex(5, 5, true));
		assertEquals(1, RecoilController.patternIndex(6, 5, true));
		assertEquals(4, RecoilController.patternIndex(9, 5, true));
	}

	@Test
	void anEmptyPatternNeverIndexesOutOfBounds() {
		assertEquals(0, RecoilController.patternIndex(3, 0, false));
		assertEquals(0, RecoilController.patternIndex(3, 0, true));
	}
}
