package gg.deepsite.pewpew.api.enums;

import gg.deepsite.pewpew.api.objects.PewpewSound;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public enum SoundEvent {

	GUN_FIRE("gun.fire", "entity.blaze.shoot", 0.6f, 1.6f),
	GUN_DRY_FIRE("gun.dry-fire", "block.dispenser.fail", 0.8f, 1.2f),
	GUN_ACTION_CLOSE("gun.action-close", "block.piston.contract", 0.7f, 0.8f),
	GUN_ACTION_OPEN("gun.action-open", "block.piston.extend", 0.7f, 0.8f),

	RELOAD_MAGAZINE_START("reload.magazine-start", "block.piston.contract", 0.8f, 1.2f),
	RELOAD_MAGAZINE_FINISH("reload.magazine-finish", "block.piston.extend", 0.8f, 1.4f),
	RELOAD_SINGLE_START("reload.single-start", "block.piston.contract", 0.8f, 1.0f),
	RELOAD_SINGLE_ROUND("reload.single-round", "block.piston.extend", 0.7f, 1.4f),

	MAGAZINE_SWAP_START("magazine.swap-start", "block.piston.contract", 0.8f, 1.2f),
	MAGAZINE_SWAP_FINISH("magazine.swap-finish", "block.piston.extend", 0.8f, 1.4f),
	MAGAZINE_FILL("magazine.fill", "block.piston.contract", 0.6f, 1.6f),

	HIT_MARKER("hit.marker", "block.note_block.pling", 1.0f, 1.0f),
	HIT_MARKER_HEADSHOT("hit.marker-headshot", "block.note_block.pling", 1.0f, 1.8f),
	HIT_CRIT("hit.crit", "entity.player.attack.crit", 1.0f, 1.2f),
	HIT_SHIELD_BREAK("hit.shield-break", "item.shield.break", 1.0f, 1.0f),

	EXPLOSION_BLAST("explosion.blast", "entity.generic.explode", 2.0f, 1.0f),

	THROWABLE_THROW("throwable.throw", "entity.snowball.throw", 0.8f, 0.8f),
	THROWABLE_EXPLODE("throwable.explode", "entity.generic.explode", 2.0f, 1.0f),
	THROWABLE_SMOKE("throwable.smoke", "entity.tnt.primed", 1.0f, 1.4f),
	THROWABLE_FLASH("throwable.flash", "item.firecharge.use", 1.5f, 0.6f),
	THROWABLE_POISON("throwable.poison", "entity.generic.splash", 1.0f, 1.0f),
	THROWABLE_FIRE("throwable.fire", "item.firecharge.use", 1.2f, 1.0f),

	MENU_EQUIP("menu.equip", "item.armor.equip_generic", 0.7f, 1.4f),
	MENU_UNEQUIP("menu.unequip", "item.armor.equip_generic", 0.7f, 1.2f);

	private final String path;
	private final PewpewSound fallback;

	SoundEvent(@NotNull String path, @NotNull String key, float volume, float pitch) {
		this.path = path;
		this.fallback = PewpewSound.of(key, volume, pitch);
	}

	@Nullable
	public static SoundEvent fromPath(@NotNull String path) {
		for (SoundEvent event : values()) {
			if (event.path.equalsIgnoreCase(path)) return event;
		}
		return null;
	}
}
