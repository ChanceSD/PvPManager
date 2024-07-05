package me.chancesd.pvpmanager.setting.lang;

public class Replacement {

	public static final Replacement PLAYER = prepare("player");
	public static final Replacement TIME = prepare("time");
	public static final Replacement VICTIM = prepare("victim");
	public static final Replacement MONEY = prepare("money");
	public static final Replacement EXP = prepare("exp");
	public static final Replacement STATE = prepare("state");
	public static final Replacement PREFIX = prepare("prefix");

	private final String placeholder;

	private Replacement(final String placeholder) {
		this.placeholder = placeholder;
	}

	private static Replacement prepare(final String value) {
		return new Replacement("{" + value + "}");
	}

	public String getPlaceholder() {
		return placeholder;
	}

}
