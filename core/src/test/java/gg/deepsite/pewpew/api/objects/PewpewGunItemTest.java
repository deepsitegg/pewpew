package gg.deepsite.pewpew.api.objects;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PewpewGunItemTest {

	@Test
	void acceptsAnyMagazineWhenUnset() {
		PewpewGunItem gun = PewpewGunItem.builder().build();
		assertTrue(gun.acceptsMagazine("whatever"));
		gun.setMagazines(List.of());
		assertTrue(gun.acceptsMagazine("whatever"));
	}

	@Test
	void restrictsToListedMagazines() {
		PewpewGunItem gun = PewpewGunItem.builder().magazines(List.of("ak_mag30")).build();
		assertTrue(gun.acceptsMagazine("ak_mag30"));
		assertFalse(gun.acceptsMagazine("ak_drum75"));
	}
}
