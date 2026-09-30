package gg.deepsite.pewpew.magazine;

import gg.deepsite.pewpew.modules.weapons.ammo.AmmoUtil;
import gg.deepsite.pewpew.modules.weapons.magazine.MagazineUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MagazineFillTest {

	@Test
	void singleRoundsFillExactlyTheFreeSpace() {
		assertEquals(30, MagazineUtil.itemsToFill(0, 30, 1, 64));
		assertEquals(3, MagazineUtil.itemsToFill(27, 30, 1, 64));
		assertEquals(0, MagazineUtil.itemsToFill(30, 30, 1, 64));
	}

	@Test
	void boxesStopAtCapacityAndSpendTheirRemainder() {
		assertEquals(3, MagazineUtil.itemsToFill(0, 30, 10, 64));
		assertEquals(1, MagazineUtil.itemsToFill(27, 30, 10, 64));
	}

	@Test
	void neverConsumesMoreThanIsAvailable() {
		assertEquals(5, MagazineUtil.itemsToFill(0, 30, 1, 5));
		assertEquals(0, MagazineUtil.itemsToFill(0, 30, 1, 0));
	}

	@Test
	void fitKeepsTheNextRoundsAndPadsLegacyRoundsUnderneath() {
		assertEquals(List.of("ap", "inc"), AmmoUtil.fit(new ArrayList<>(List.of("fmj", "ap", "inc")), 2, null));
		assertEquals(List.of("fmj", "fmj", "inc"), AmmoUtil.fit(new ArrayList<>(List.of("inc")), 3, "fmj"));
		assertEquals(List.of("inc"), AmmoUtil.fit(new ArrayList<>(List.of("inc")), 3, null));
	}
}
