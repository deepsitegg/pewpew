package gg.deepsite.pewpew.shooting;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Glass {

	/** Glass blocks and panes of every colour, including tinted glass, by their block key. */
	public static boolean isGlass(@NotNull String blockKey) {
		String path = blockKey.substring(blockKey.indexOf(':') + 1);
		return path.endsWith("glass") || path.endsWith("glass_pane");
	}
}
