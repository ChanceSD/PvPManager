package me.chancesd.pvpmanager.setting.lang;

public class Replacement {

	public static final Replacement PLAYER = prepare("player");
	public static final Replacement TIME = prepare("time");
	public static final Replacement VICTIM = prepare("victim");
	public static final Replacement MONEY = prepare("money");
	public static final Replacement EXP = prepare("exp");
	public static final Replacement STATE = prepare("state");
	public static final Replacement PREFIX = prepare("prefix");
	public static final Replacement POWER = prepare("power");

	private final String placeholder;

	private Replacement(final String placeholder) {
		this.placeholder = placeholder;
	}

	private static Replacement prepare(final String value) {
		return new Replacement("{" + value + "}");
	}
	
	/**
	 * Creates a dynamic replacement for one-off placeholders.
	 * Use predefined constants when available for better consistency.
	 * 
	 * @param key the placeholder key (without braces)
	 * @return a Replacement instance for {key}
	 */
	public static Replacement of(final String key) {
		if (key == null || key.trim().isEmpty()) {
			throw new IllegalArgumentException("Replacement key cannot be null or empty");
		}
		return new Replacement("{" + key + "}");
	}

	public String getPlaceholder() {
		return placeholder;
	}

}
