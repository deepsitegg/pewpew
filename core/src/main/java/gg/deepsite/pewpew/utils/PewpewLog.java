package gg.deepsite.pewpew.utils;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public final class PewpewLog {

	private static Logger logger = Logger.getLogger("Pewpew");

	private PewpewLog() {
	}

	@NotNull
	public static Logger get() {
		return logger;
	}

	public static void set(@NotNull Logger value) {
		logger = value;
	}
}
