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
public class PewpewMagazineItem extends PewPewItem {

	private String ammoType;
	private String modelSuffix;
	private int gunModelData;
	private int capacity;
	private double reloadModifier;
}
