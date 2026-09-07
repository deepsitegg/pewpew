package gg.deepsite.pewpew.api.objects;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PewpewAnimationTest {

	@Test
	void modelAtWalksFrameBoundaries() {
		PewpewAnimation animation = new PewpewAnimation(List.of(
				new PewpewAnimation.Frame("p:a", 2),
				new PewpewAnimation.Frame("p:b", 3)));

		assertEquals(5, animation.length());
		assertEquals("p:a", animation.modelAt(0));
		assertEquals("p:a", animation.modelAt(1));
		assertEquals("p:b", animation.modelAt(2));
		assertEquals("p:b", animation.modelAt(4));
		assertNull(animation.modelAt(5));
		assertNull(animation.modelAt(-1));
	}

	@Test
	void bakedNumbersFramesFromOne() {
		PewpewAnimation animation = PewpewAnimation.baked("p:reload_", 3, 2);

		assertEquals(6, animation.length());
		assertEquals("p:reload_1", animation.modelAt(0));
		assertEquals("p:reload_2", animation.modelAt(2));
		assertEquals("p:reload_3", animation.modelAt(5));
	}

	@Test
	void scaledStretchesToTargetAndKeepsEveryFrame() {
		PewpewAnimation animation = PewpewAnimation.baked("p:f", 4, 1);
		PewpewAnimation stretched = animation.scaled(40.0 / animation.length());

		assertEquals(40, stretched.length());
		assertEquals("p:f1", stretched.modelAt(0));
		assertEquals("p:f4", stretched.modelAt(39));
	}

	@Test
	void scaledNeverDropsAFrameToZeroTicks() {
		PewpewAnimation animation = PewpewAnimation.baked("p:f", 10, 1);
		PewpewAnimation squashed = animation.scaled(0.1);

		assertEquals(10, squashed.length());
		assertEquals("p:f10", squashed.modelAt(9));
	}
}
