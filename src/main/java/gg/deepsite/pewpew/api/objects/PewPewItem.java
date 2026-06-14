package gg.deepsite.pewpew.api.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PewPewItem {
	private String id;

	private String name;
	private List<String> lore;
	private boolean hideItemFlags;
	private int customModelData;
	private String itemModel;
	private int maxStack;
}
