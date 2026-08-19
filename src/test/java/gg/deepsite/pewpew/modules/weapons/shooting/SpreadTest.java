package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.api.objects.SpreadModifiers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpreadTest {

	private static final SpreadModifiers MODIFIERS = SpreadModifiers.builder()
			.sprinting(2.0)
			.walking(1.5)
			.sneaking(0.5)
			.standing(1.0)
			.midair(3.0)
			.inWater(2.0)
			.build();

	@Test
	void standingStillIsTheBaseline() {
		assertEquals(1.0, Spread.stateMultiplier(MODIFIERS, false, false, false, false, false));
	}

	@Test
	void movementStatesAreExclusiveAndOrdered() {
		assertEquals(2.0, Spread.stateMultiplier(MODIFIERS, true, true, false, false, false), "sprinting beats walking");
		assertEquals(0.5, Spread.stateMultiplier(MODIFIERS, false, true, true, false, false), "sneaking beats walking");
		assertEquals(1.5, Spread.stateMultiplier(MODIFIERS, false, true, false, false, false));
	}

	@Test
	void midairAndWaterStackOnTopOfMovement() {
		assertEquals(4.5, Spread.stateMultiplier(MODIFIERS, false, true, false, true, false), 1e-9);
		assertEquals(9.0, Spread.stateMultiplier(MODIFIERS, false, true, false, true, true), 1e-9);
	}

	@Test
	void noModifiersMeansNoChange() {
		assertEquals(1.0, Spread.stateMultiplier(null, true, true, true, true, true));
	}

	@Test
	void bloomDecaysToZeroOverTime() {
		assertEquals(2.0, Spread.decay(2.0, 0, 0.1), 1e-9);
		assertEquals(1.0, Spread.decay(2.0, 10, 0.1), 1e-9);
		assertEquals(0.0, Spread.decay(2.0, 40, 0.1), 1e-9, "never goes negative");
	}

	@Test
	void bloomWithoutDecayPersists() {
		assertEquals(2.0, Spread.decay(2.0, 100, 0.0), 1e-9);
	}
}
