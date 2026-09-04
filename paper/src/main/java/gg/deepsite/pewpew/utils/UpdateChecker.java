package gg.deepsite.pewpew.utils;

import gg.deepsite.pewpew.PewpewPlugin;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public final class UpdateChecker {

	private static final String API_URL = "https://api.github.com/repos/deepsitegg/pewpew/releases/latest";
	public static final String RELEASES_URL = "https://modrinth.com/plugin/pewpew/versions";

	private static final Pattern TAG_NAME = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");

	@Getter
	private static volatile boolean updateAvailable;
	@Getter
	private static volatile String latestVersion;

	public static void check(PewpewPlugin plugin) {
		String current = plugin.getPluginMeta().getVersion();

		HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
				.header("Accept", "application/vnd.github+json")
				.header("User-Agent", "Pewpew/" + current)
				.timeout(Duration.ofSeconds(10))
				.GET()
				.build();

		HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build()
				.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenAccept(response -> handle(plugin, current, response))
				.exceptionally(throwable -> {
					plugin.getLogger().warning("Update check failed: " + throwable.getMessage());
					return null;
				});
	}

	private static void handle(PewpewPlugin plugin, String current, HttpResponse<String> response) {
		if (response.statusCode() != 200) {
			plugin.getLogger().warning("Update check failed: GitHub returned HTTP " + response.statusCode());
			return;
		}

		Matcher matcher = TAG_NAME.matcher(response.body());
		if (!matcher.find()) {
			plugin.getLogger().warning("Update check failed: no release tag found.");
			return;
		}

		String latest = stripLeadingV(matcher.group(1));
		latestVersion = latest;
		updateAvailable = isNewer(latest, current);

		if (updateAvailable) {
			plugin.getLogger().warning("A new version of Pewpew is available: " + latest
					+ " (current: " + current + "). Download: " + RELEASES_URL);
		} else {
			plugin.getLogger().info("Pewpew is up to date (" + current + ").");
		}
	}

	private static String stripLeadingV(String tag) {
		return (tag.startsWith("v") || tag.startsWith("V")) ? tag.substring(1) : tag;
	}

	static boolean isNewer(String latest, String current) {
		int[] a = parse(latest);
		int[] b = parse(current);
		for (int i = 0; i < Math.max(a.length, b.length); i++) {
			int x = i < a.length ? a[i] : 0;
			int y = i < b.length ? b[i] : 0;
			if (x != y) return x > y;
		}
		return false;
	}

	private static int[] parse(String version) {
		String[] parts = version.split("[-+]")[0].split("\\.");
		int[] out = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			try {
				out[i] = Integer.parseInt(parts[i].trim());
			} catch (NumberFormatException ignored) {
				out[i] = 0;
			}
		}
		return out;
	}
}
