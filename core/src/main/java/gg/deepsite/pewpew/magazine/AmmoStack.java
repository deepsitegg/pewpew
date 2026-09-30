package gg.deepsite.pewpew.magazine;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The rounds in a magazine, one ammo id per round, bottom first. The last entry is the next round fired, so a
 * magazine packed AP, incendiary, AP fires AP, incendiary, AP back out in reverse, like a real one.
 *
 * <p>Stored on items as a run-length string ({@code "ap*2,incendiary*1"}) so a 100-round drum stays small.
 */
@UtilityClass
public class AmmoStack {

	@NotNull
	public static List<String> decode(@Nullable String encoded) {
		List<String> rounds = new ArrayList<>();
		if (encoded == null || encoded.isBlank()) return rounds;

		for (String run : encoded.split(",")) {
			int star = run.lastIndexOf('*');
			String id = star < 0 ? run : run.substring(0, star);
			int count = 1;
			if (star >= 0) {
				try {
					count = Integer.parseInt(run.substring(star + 1));
				} catch (NumberFormatException ignored) {
				}
			}
			if (!id.isEmpty() && count > 0) rounds.addAll(Collections.nCopies(count, id));
		}
		return rounds;
	}

	@Nullable
	public static String encode(@NotNull List<String> rounds) {
		if (rounds.isEmpty()) return null;

		StringBuilder out = new StringBuilder();
		int i = 0;
		while (i < rounds.size()) {
			String id = rounds.get(i);
			int run = 1;
			while (i + run < rounds.size() && rounds.get(i + run).equals(id)) run++;
			if (!out.isEmpty()) out.append(',');
			out.append(id).append('*').append(run);
			i += run;
		}
		return out.toString();
	}

	/** Rounds written before mixed magazines existed: a count and one ammo id for all of them. */
	@NotNull
	public static List<String> legacy(@Nullable String ammoId, int rounds) {
		return ammoId == null || rounds <= 0 ? new ArrayList<>() : new ArrayList<>(Collections.nCopies(rounds, ammoId));
	}
}
