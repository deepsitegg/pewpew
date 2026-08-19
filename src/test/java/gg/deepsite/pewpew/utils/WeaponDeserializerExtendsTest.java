package gg.deepsite.pewpew.utils;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class WeaponDeserializerExtendsTest {

	private static final Logger LOG = Logger.getLogger("WeaponDeserializerExtendsTest");

	private static ConfigurationNode parse(String yaml) throws Exception {
		return YamlConfigurationLoader.builder()
				.source(() -> new BufferedReader(new StringReader(yaml)))
				.build()
				.load();
	}

	private static ConfigurationNode resolve(ConfigurationNode root, String id) {
		return WeaponDeserializer.resolveExtends(id, root.node(id), WeaponDeserializer.index(List.of(root)), LOG);
	}

	@Test
	void childInheritsParentFieldsAndOverridesItsOwn() throws Exception {
		ConfigurationNode root = parse("""
				base_rifle:
				  type: GUN
				  maxStack: 1
				  baseDamage: 7.0
				  spread: 2.0
				ak47:
				  extends: base_rifle
				  name: "AK-47"
				  spread: 3.0
				""");

		ConfigurationNode ak = resolve(root, "ak47");

		assertEquals("GUN", ak.node("type").getString(), "inherited");
		assertEquals(1, ak.node("maxStack").getInt(), "inherited");
		assertEquals(7.0, ak.node("baseDamage").getDouble(), "inherited");
		assertEquals(3.0, ak.node("spread").getDouble(), "child wins");
		assertEquals("AK-47", ak.node("name").getString(), "own field kept");
		assertTrue(ak.node("extends").virtual(), "'extends' must not survive into the item");
	}

	@Test
	void parentIsNotMutatedByTheChild() throws Exception {
		ConfigurationNode root = parse("""
				base_rifle:
				  type: GUN
				  spread: 2.0
				ak47:
				  extends: base_rifle
				  spread: 3.0
				""");

		resolve(root, "ak47");

		assertEquals(2.0, root.node("base_rifle", "spread").getDouble(), "the template must be left alone");
	}

	@Test
	void inheritanceChainsThroughMultipleLevels() throws Exception {
		ConfigurationNode root = parse("""
				weapon:
				  type: GUN
				  maxStack: 1
				rifle:
				  extends: weapon
				  baseDamage: 7.0
				ak47:
				  extends: rifle
				  name: "AK-47"
				""");

		ConfigurationNode ak = resolve(root, "ak47");

		assertEquals("GUN", ak.node("type").getString());
		assertEquals(1, ak.node("maxStack").getInt());
		assertEquals(7.0, ak.node("baseDamage").getDouble());
	}

	@Test
	void unknownParentIsIgnoredAndTheItemStillLoads() throws Exception {
		ConfigurationNode root = parse("""
				ak47:
				  extends: does_not_exist
				  type: GUN
				  name: "AK-47"
				""");

		ConfigurationNode ak = resolve(root, "ak47");

		assertEquals("GUN", ak.node("type").getString());
		assertTrue(ak.node("extends").virtual());
	}

	@Test
	void circularInheritanceDoesNotHang() throws Exception {
		ConfigurationNode root = parse("""
				a:
				  extends: b
				  type: GUN
				b:
				  extends: a
				  spread: 1.0
				""");

		assertTimeoutPreemptively(java.time.Duration.ofSeconds(2), () -> {
			ConfigurationNode a = resolve(root, "a");
			assertEquals("GUN", a.node("type").getString());
		});
	}

	@Test
	void templatesAreIndexedAcrossFiles() throws Exception {
		ConfigurationNode bundled = parse("""
				base_rifle:
				  type: GUN
				  baseDamage: 7.0
				""");
		ConfigurationNode mine = parse("""
				my_gun:
				  extends: base_rifle
				  name: "Mine"
				""");

		ConfigurationNode gun = WeaponDeserializer.resolveExtends("my_gun", mine.node("my_gun"),
				WeaponDeserializer.index(List.of(bundled, mine)), LOG);

		assertEquals(7.0, gun.node("baseDamage").getDouble(), "a gun in one file can extend a template in another");
		assertEquals("Mine", gun.node("name").getString());
	}

	@Test
	void childDoesNotInheritAbstractFromItsTemplate() throws Exception {
		ConfigurationNode root = parse("""
				base_rifle:
				  abstract: true
				  type: GUN
				  spread: 2.0
				ak47:
				  extends: base_rifle
				  name: "AK-47"
				""");

		ConfigurationNode ak = resolve(root, "ak47");

		assertFalse(ak.node("abstract").getBoolean(false),
				"a template being abstract must not make everything extending it abstract too");
		assertEquals(2.0, ak.node("spread").getDouble(), "the rest of the template is still inherited");
	}

	@Test
	void abstractSurvivesOnTheItemThatDeclaresIt() throws Exception {
		ConfigurationNode root = parse("""
				weapon:
				  type: GUN
				  maxStack: 1
				base_rifle:
				  abstract: true
				  extends: weapon
				  spread: 2.0
				""");

		ConfigurationNode base = resolve(root, "base_rifle");

		assertTrue(base.node("abstract").getBoolean(false), "its own abstract flag is kept");
		assertEquals(1, base.node("maxStack").getInt(), "and it still inherits from its own parent");
	}

	@Test
	void itemWithoutExtendsIsUntouched() throws Exception {
		ConfigurationNode root = parse("""
				ak47:
				  type: GUN
				  spread: 3.0
				""");

		ConfigurationNode ak = WeaponDeserializer.resolveExtends("ak47", root.node("ak47"), Map.of(), LOG);

		assertEquals(3.0, ak.node("spread").getDouble());
	}
}
