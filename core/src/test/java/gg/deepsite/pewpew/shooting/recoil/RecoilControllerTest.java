package gg.deepsite.pewpew.shooting.recoil;

import gg.deepsite.pewpew.api.objects.RecoilProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecoilControllerTest {

	private static final class FakeSink implements RotationSink {
		private float yaw;
		private float pitch;
		private boolean relativeSupported = true;
		float appliedPitch;
		float appliedYaw;

		@Override
		public boolean alive() {
			return true;
		}

		@Override
		public float yaw() {
			return yaw;
		}

		@Override
		public float pitch() {
			return pitch;
		}

		@Override
		public boolean applyRelative(float deltaYaw, float deltaPitch) {
			if (!relativeSupported) return false;
			appliedYaw += deltaYaw;
			appliedPitch += deltaPitch;
			yaw += deltaYaw;
			pitch += deltaPitch;
			return true;
		}

		@Override
		public void setRotation(float newYaw, float newPitch) {
			appliedYaw += newYaw - yaw;
			appliedPitch += newPitch - pitch;
			yaw = newYaw;
			pitch = newPitch;
		}
	}

	private static int settle(RecoilController controller) {
		for (int tick = 1; tick <= 400; tick++) {
			if (controller.tick()) return tick;
		}
		return -1;
	}

	@Test
	void aKickRaisesTheAimAndThenRecovers() {
		FakeSink sink = new FakeSink();
		RecoilController controller = new RecoilController(sink, RecoilProfile.DEFAULT);

		controller.kick(10);
		controller.tick();
		assertTrue(sink.appliedPitch < 0, "the first tick must pull the aim upwards");

		assertTrue(settle(controller) > 0, "recoil must settle");
		assertEquals(0f, sink.appliedPitch, 0.05f, "the aim returns to where it started");
	}

	@Test
	void fallsBackToAbsoluteRotationWhenRelativeIsUnavailable() {
		FakeSink sink = new FakeSink();
		sink.relativeSupported = false;
		RecoilController controller = new RecoilController(sink, RecoilProfile.DEFAULT);

		controller.kick(10);
		controller.tick();
		assertTrue(sink.pitch < 0, "absolute rotation must move the player too");

		assertTrue(settle(controller) > 0, "recoil must settle without relative rotation");
	}

	@Test
	void yawWrapsAtTheSeam() {
		assertEquals(-179f, RecoilController.normalizeYaw(181f), 1e-4);
		assertEquals(179f, RecoilController.normalizeYaw(-181f), 1e-4);
		assertEquals(0f, RecoilController.normalizeYaw(720f), 1e-4);
	}
}
