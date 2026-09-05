package gg.deepsite.pewpew.magazine;

import gg.deepsite.pewpew.modules.weapons.magazine.MagazineUtil;
import org.junit.jupiter.api.Test;

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
}
