package gg.deepsite.pewpew.api.objects.attachment;

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
public class PewpewMagazineAttachment extends PewpewAttachment {

    private int ammoBonus;
    private double reloadModifier;
}
