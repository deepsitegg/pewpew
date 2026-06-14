package gg.deepsite.pewpew.api.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import gg.deepsite.pewpew.api.enums.ThrowableEffect;

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
}

