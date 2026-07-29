package gg.deepsite.pewpew.api.objects;

import gg.deepsite.pewpew.api.enums.ThrowableEffect;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PewpewThrowableItem extends PewPewItem {

	private int fuseTime;
	private double blastRadius;
	private double throwForce;
	private ThrowableEffect effect;

	private double explosionDamage;
	private double explosionKnockback;
	private int effectDuration;
	private int effectAmplifier;
	private int fireTicks;
}

