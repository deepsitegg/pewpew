package gg.deepsite.pewpew.magazine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AmmoStackTest {

	@Test
	void roundTripsMixedOrder() {
		List<String> rounds = List.of("ap", "inc", "ap", "ap", "inc");
		assertEquals("ap*1,inc*1,ap*2,inc*1", AmmoStack.encode(rounds));
		assertEquals(rounds, AmmoStack.decode(AmmoStack.encode(rounds)));
	}

	@Test
	void emptyIsNull() {
		assertNull(AmmoStack.encode(List.of()));
		assertEquals(List.of(), AmmoStack.decode(null));
	}

	@Test
	void legacyExpandsToOneIdPerRound() {
		assertEquals(List.of("fmj", "fmj", "fmj"), AmmoStack.legacy("fmj", 3));
		assertEquals(List.of(), AmmoStack.legacy(null, 3));
	}

	@Test
	void namespacedIdsSurvive() {
		assertEquals(List.of("vibe:ap", "vibe:ap"), AmmoStack.decode("vibe:ap*2"));
	}
}
