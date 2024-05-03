package me.chancesd.pvpmanager.setting.lang;

public class Replacement {

	public static final String PLAYER = prepare("player");
	public static final String TIME = prepare("time");
	public static final String VICTIM = prepare("victim");
	public static final String MONEY = prepare("money");
	public static final String EXP = prepare("exp");
	public static final String STATE = prepare("state");
	public static final String PREFIX = prepare("prefix");

	private static String prepare(final String value) {
		return "{" + value + "}";
	}

}
