package gg.deepsite.pewpew.api.objects;

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
public class PewpewAmmoItem extends PewPewItem {

	private String ammoType;
	private int roundsPerItem;
	private double damageMultiplier;
	private double velocityMultiplier;
	private int penetration;
	private int fireTicks;
	/** Throwable id this round launches instead of the gun's own payload, for launchers with mixed grenades. */
	private String payload;
}
