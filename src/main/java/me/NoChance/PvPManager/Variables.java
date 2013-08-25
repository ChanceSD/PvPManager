package me.NoChance.PvPManager;

public class Variables {

	private PvPManager plugin;
	public static boolean inCombatEnabled;
	public static int timeInCombat;
	public static boolean stopCommands;
	public static boolean punishmentsEnabled;
	public static boolean keepItems;
	public static boolean keepExp;
	public static boolean killOnLogout;

	public Variables(PvPManager plugin) {
		this.plugin = plugin;
		InitialiseVariables();
	}

	public void InitialiseVariables() {
		inCombatEnabled = getBoolean("In Combat.Enabled");
		timeInCombat = getInt("In Combat.Time(seconds)");
		stopCommands = getBoolean("In Combat.Stop Commands");
		punishmentsEnabled = getBoolean("In Combat.Punishments.Enabled");
		keepItems = getBoolean("In Combat.Punishments.Kill on Logout.Keep Items");
		keepExp = getBoolean("In Combat.Punishments.Kill on Logout.Keep Exp");
		killOnLogout = getBoolean("In Combat.Punishments.Kill on Logout.Enabled");
	}

	public boolean getBoolean(String a) {
		return plugin.getConfig().getBoolean(a);
	}

	public int getInt(String a) {
		return plugin.getConfig().getInt(a);
	}
}
