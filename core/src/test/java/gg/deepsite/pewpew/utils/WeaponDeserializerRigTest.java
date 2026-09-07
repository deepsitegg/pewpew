package gg.deepsite.pewpew.utils;

import gg.deepsite.pewpew.api.enums.AnimationEvent;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.PewpewRig;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class WeaponDeserializerRigTest {

	private static final String RIGGED_GUN = """
			rig_test:
			  type: GUN
			  name: "Rig Test"
			  itemModel: "pewpew:testgun_idle"
			  baseDamage: 2.0
			  fireRate: 10
			  reloadTime: 60
			  ammoType: pistol_9mm
			  maxAmmo: 5
			  range: 40.0
			  firingMode: HITSCAN
			  rig:
			    reload:
			      body:
			        itemModel: "pewpew:testgun_body"
			        keyframes:
			          - tick: 0
			            translation: [0.0, 0.0, 0.7]
			            scale: [0.5, 0.5, 0.5]
			          - tick: 30
			            translation: [0.1, -0.15, 0.7]
			            rotation: [22, 15, -28]
			            scale: [0.5, 0.5, 0.5]
			      part:
			        itemModel: "pewpew:testgun_part"
			        keyframes:
			          - tick: 20
			            translation: [0.0, -1.4, 0.7]
			""";

	private static ConfigurationNode parse(String yaml) throws Exception {
		return YamlConfigurationLoader.builder()
				.source(() -> new BufferedReader(new StringReader(yaml)))
				.build()
				.load();
	}

	private static PewpewRig reloadRig() throws Exception {
		List<PewPewItem> items = WeaponDeserializer.deserializeAll("guns.yml", parse(RIGGED_GUN));
		PewpewGunItem gun = assertInstanceOf(PewpewGunItem.class, items.get(0));
		PewpewRig rig = gun.getRig(AnimationEvent.RELOAD);
		assertNotNull(rig, "reload rig should parse");
		return rig;
	}

	@Test
	void parsesPartsKeyframesAndDefaults() throws Exception {
		PewpewRig rig = reloadRig();

		assertEquals(2, rig.parts().size());
		assertEquals(30, rig.length(), "rig length is the latest keyframe across all parts");

		PewpewRig.Part body = rig.parts().stream().filter(p -> p.id().equals("body")).findFirst().orElseThrow();
		assertEquals("pewpew:testgun_body", body.itemModel());
		assertArrayEquals(new float[]{0f, 0f, 0.7f}, body.at(0).translation());
		assertArrayEquals(new float[]{0f, 0f, 0f}, body.at(0).rotation(), "omitted rotation defaults to zero");
		assertArrayEquals(new float[]{0.5f, 0.5f, 0.5f}, body.at(0).scale());
		assertArrayEquals(new float[]{22f, 15f, -28f}, body.at(30).rotation());

		PewpewRig.Part part = rig.parts().stream().filter(p -> p.id().equals("part")).findFirst().orElseThrow();
		assertArrayEquals(new float[]{1f, 1f, 1f}, part.at(20).scale(), "omitted scale defaults to one");
	}

	@Test
	void durationAfterDrivesClientInterpolation() throws Exception {
		PewpewRig.Part body = reloadRig().parts().stream()
				.filter(p -> p.id().equals("body")).findFirst().orElseThrow();

		assertEquals(30, body.durationAfter(0), "gap to the next keyframe is the interpolation duration");
		assertEquals(0, body.durationAfter(30), "last keyframe has nothing to interpolate toward");
		assertNull(body.at(7), "no keyframe on an in-between tick; the client tweens it");
	}

	@Test
	void afterReturnsTheKeyframeInterpolationAimsAt() throws Exception {
		PewpewRig.Part body = reloadRig().parts().stream()
				.filter(p -> p.id().equals("body")).findFirst().orElseThrow();

		assertEquals(30, body.after(0).tick(), "at tick 0 the display is sent the tick-30 pose");
		assertNull(body.after(30), "the final keyframe has nothing to aim at");
		assertEquals(30, body.durationAfter(0), "and tweens toward it over the gap");
	}

	@Test
	void scalingStretchesKeyframesToTheRealReloadTime() throws Exception {
		PewpewRig stretched = reloadRig().scaled(60.0 / 30.0);

		PewpewRig.Part body = stretched.parts().stream()
				.filter(p -> p.id().equals("body")).findFirst().orElseThrow();
		assertEquals(60, stretched.length());
		assertNotNull(body.at(60), "the 30-tick keyframe moves to tick 60");
		assertEquals(60, body.durationAfter(0));
	}
}
