package gg.deepsite.pewpew.api.objects;

import gg.deepsite.pewpew.api.enums.AttachmentType;
import gg.deepsite.pewpew.api.enums.FiringMode;
import gg.deepsite.pewpew.api.enums.ReloadType;
import gg.deepsite.pewpew.api.enums.Trajectory;
import gg.deepsite.pewpew.api.objects.attachment.DefaultAttachment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PewpewGunItem extends PewpewWeaponItem {

	private FiringMode firingMode;
	private double range;
	private double projectileSpeed;
	private Material projectileModel;
	private Trajectory trajectory;
	private ExplosiveConfig explosive;
	private String payload;
	private int burstCount;
	private ReloadType reloadType;
	private double spread;
	private double recoil;
	private RecoilProfile recoilProfile;
	private double knockback;
	private double selfKnockback;
	private int bulletCount;
	private double bulletDrop;
	private double headshotMultiplier;
	private boolean automatic;
	private int actionOpenTime;
	private int actionCloseTime;
	private String deathMessage;
	private double critChance;
	private double critMultiplier;
	private int shieldDisableTime;
	private List<PotionEffect> victimEffects;
	private List<PotionEffect> shooterEffects;
	private double falloffStart;
	private double falloffEnd;
	private double falloffMinMultiplier;
	private Particle trailParticle;
	private Particle impactParticle;
	private List<PewpewSound> fireSound;
	private List<PewpewSound> hitSound;
	private String hitMessage;
	private List<DefaultAttachment> defaultAttachments;

	public boolean isForcedSlot(AttachmentType slot) {
		if (defaultAttachments == null) return false;
		for (DefaultAttachment def : defaultAttachments) {
			if (def.getSlot() == slot) return def.isForced();
		}
		return false;
	}
}
