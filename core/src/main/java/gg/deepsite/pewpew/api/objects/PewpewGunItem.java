package gg.deepsite.pewpew.api.objects;

import gg.deepsite.pewpew.api.enums.AnimationEvent;
import gg.deepsite.pewpew.api.enums.AttachmentType;
import gg.deepsite.pewpew.api.enums.FiringMode;
import gg.deepsite.pewpew.api.enums.ReloadType;
import gg.deepsite.pewpew.api.enums.Trajectory;
import gg.deepsite.pewpew.api.objects.attachment.DefaultAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PewpewGunItem extends PewpewWeaponItem {

	private FiringMode firingMode;
	private double range;
	private double projectileSpeed;
	private Key projectileModel;
	private Trajectory trajectory;
	private ExplosiveConfig explosive;
	private String payload;
	private int burstCount;
	private int burstDelay;
	private ReloadType reloadType;
	private double spread;
	private SpreadModifiers spreadModifiers;
	private double bloomPerShot;
	private double bloomMax;
	private double bloomDecay;
	private double recoil;
	private RecoilProfile recoilProfile;
	private double knockback;
	private double selfKnockback;
	private int bulletCount;
	private double bulletDrop;
	private double headshotMultiplier;
	private Key damageType;
	private boolean automatic;
	private int actionOpenTime;
	private int actionCloseTime;
	private String deathMessage;
	private double critChance;
	private double critMultiplier;
	private int shieldDisableTime;
	private List<PewpewEffect> victimEffects;
	private List<PewpewEffect> shooterEffects;
	private double falloffStart;
	private double falloffEnd;
	private double falloffMinMultiplier;
	private Key trailParticle;
	private Key impactParticle;
	private List<PewpewSound> fireSound;
	private List<PewpewSound> hitSound;
	private String hitMessage;
	private List<DefaultAttachment> defaultAttachments;
	private Map<AnimationEvent, PewpewAnimation> animations;
	private Map<AnimationEvent, PewpewRig> rigs;
	@Builder.Default
	private boolean animationCooldown = true;

	public PewpewAnimation getAnimation(AnimationEvent event) {
		return animations == null ? null : animations.get(event);
	}

	public PewpewRig getRig(AnimationEvent event) {
		return rigs == null ? null : rigs.get(event);
	}

	public boolean isForcedSlot(AttachmentType slot) {
		if (defaultAttachments == null) return false;
		for (DefaultAttachment def : defaultAttachments) {
			if (def.getSlot() == slot) return def.isForced();
		}
		return false;
	}
}
