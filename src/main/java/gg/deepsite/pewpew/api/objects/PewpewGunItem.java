package gg.deepsite.pewpew.api.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import gg.deepsite.pewpew.api.enums.FiringMode;
import gg.deepsite.pewpew.api.enums.ReloadType;
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
    private int burstCount;
    private ReloadType reloadType;
    private double spread;
    private double recoil;
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
    private PewpewSound fireSound;
    private PewpewSound hitSound;
    private String hitMessage;
}
