package me.NoChance.PvPManager.Managers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Version;
import me.NoChance.PvPManager.Config.Config;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Utils.Log;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {

	private final PvPManager plugin;
	private final File usersFile;
	private final YamlConfiguration users;
	private Config config;
	private int configVersion;

	public ConfigManager(final PvPManager plugin) {
		this.plugin = plugin;
		this.users = new YamlConfiguration();
		this.usersFile = new File(plugin.getDataFolder(), "users.yml");
		configVersion = plugin.getConfig().getInt("Config Version", 0);
		loadConfig();
		loadUsers();
	}

	private void updateDefaultConfig() {
		this.config.set("Default PvP", Variables.isDefaultPvp());
		this.config.set("PvP Blood", Variables.isPvpBlood());
		this.config.set("Auto Soup.Enabled", Variables.isAutoSoupEnabled());
		this.config.set("Auto Soup.Health Gain", Variables.getSoupHealth());
		this.config.set("Disable Fly", Variables.isDisableFly());
		this.config.set("Disable GameMode", Variables.isDisableGamemode());
		this.config.set("Disable Disguise", Variables.isDisableDisguise());
		this.config.set("Disable Invisibility", Variables.isDisableInvisibility());
		this.config.set("Ignore Zones For Tagged", Variables.isStopBorderHopping());
		this.config.set("Ignore No Damage Hits", Variables.isIgnoreNoDamageHits());

		this.config.set("In Combat.Enabled", Variables.isInCombatEnabled());
		this.config.set("In Combat.Silent", Variables.isInCombatSilent());
		this.config.set("In Combat.Time(seconds)", Variables.getTimeInCombat());
		this.config.set("In Combat.Name Tag Color", Variables.getNameTagColor());
		this.config.set("In Combat.Only Tag Attacker", Variables.isOnlyTagAttacker());
		this.config.set("In Combat.Block EnderPearl", Variables.isBlockEnderPearl());
		this.config.set("In Combat.Block Place Blocks", Variables.isBlockPlaceBlocks());
		this.config.set("In Combat.Stop Commands.Enabled", Variables.isStopCommands());
		this.config.set("In Combat.Stop Commands.Whitelist", Variables.isCommandsWhitelist());
		this.config.set("In Combat.Stop Commands.Commands", Variables.getCommandsAllowed());
		this.config.set("In Combat.Punishments.Enabled", Variables.isPunishmentsEnabled());
		this.config.set("In Combat.Punishments.Broadcast PvPLog", Variables.isBroadcastPvpLog());
		this.config.set("In Combat.Punishments.Log To File", Variables.isLogToFile());
		this.config.set("In Combat.Punishments.Kill on Logout.Enabled", Variables.isKillOnLogout());
		this.config.set("In Combat.Punishments.Kill on Logout.Drops.Inventory", Variables.isDropInventory());
		this.config.set("In Combat.Punishments.Kill on Logout.Drops.Experience", Variables.isDropExp());
		this.config.set("In Combat.Punishments.Kill on Logout.Drops.Armor", Variables.isDropArmor());
		this.config.set("In Combat.Punishments.Fine.Enabled", Variables.isFineEnabled());
		this.config.set("In Combat.Punishments.Fine.Amount", Variables.getFineAmount());

		this.config.set("Player Kills.Money Reward", Variables.getMoneyReward());
		this.config.set("Player Kills.Money Penalty", Variables.getMoneyPenalty());
		this.config.set("Player Kills.Commands On Kill.Enabled", Variables.isCommandsOnKillEnabled());
		this.config.set("Player Kills.Commands On Kill.Commands", Variables.getCommandsOnKill());

		this.config.set("PvP Toggle.Cooldown(seconds)", Variables.getToggleCooldown());
		this.config.set("PvP Toggle.Broadcast", Variables.isToggleBroadcast());
		this.config.set("PvP Toggle.NameTags.Enabled", Variables.isToggleNametagsEnabled());
		this.config.set("PvP Toggle.NameTags.Color On", Variables.getToggleColorOn());
		this.config.set("PvP Toggle.NameTags.Color Off", Variables.getToggleColorOff());

		this.config.set("Kill Abuse.Enabled", Variables.isKillAbuseEnabled());
		this.config.set("Kill Abuse.Max Kills", Variables.getKillAbuseMaxKills());
		this.config.set("Kill Abuse.Time Limit", Variables.getKillAbuseTime());
		this.config.set("Kill Abuse.Commands on Abuse", Variables.getKillAbuseCommands());
		this.config.set("Kill Abuse.Respawn Protection", Variables.getRespawnProtection());

		this.config.set("Newbie Protection.Enabled", Variables.isNewbieProtectionEnabled());
		this.config.set("Newbie Protection.Time(minutes)", Variables.getNewbieProtectionTime());
		this.config.set("Newbie Protection.Block Pick Items", Variables.isBlockPickNewbies());
		this.config.set("Newbie Protection.Protect From Everything", Variables.isNewbieGodMode());

		this.config.set("Update Check.Enabled", Variables.isUpdateCheck());
		this.config.set("Update Check.Auto Update", Variables.isUpdate());

		this.config.set("World Exclusions", Variables.getWorldsExcluded());
		this.config.saveConfig();
	}

	private void loadConfig() {
		if (getConfigVersion() < Integer.parseInt(Version.getConfigVersion())) {
			final File configFile = new File(plugin.getDataFolder(), "config.yml");
			if (configFile.exists()) {
				config = new Config(plugin, "config.yml");
				Variables.initizalizeVariables(this);
				configFile.delete();
				config = new Config(plugin, "config.yml");
				updateDefaultConfig();
				Variables.setConfigUpdated(true);
				configVersion = config.getInt("Config Version");
			} else {
				Log.info("New Config File Created Successfully!");
				config = new Config(plugin, "config.yml");
				Variables.initizalizeVariables(this);
			}
		} else {
			config = new Config(plugin, "config.yml");
			Variables.initizalizeVariables(this);
		}
	}

	private void loadUsers() {
		try {
			if (!usersFile.exists()) {
				plugin.saveResource("users.yml", false);
				Log.info("New Users File Created Successfully!");
				return;
			}
			users.load(usersFile);
		} catch (final Exception e) {
			Log.severe("Error loading users file! Error: ");
			e.printStackTrace();
		}
	}

	public final void saveUser(final UUID uuid, final boolean save) {
		final String id = uuid.toString();
		final List<String> userList = users.getStringList("players");
		if (save && userList.contains(id) || !save && !userList.contains(id))
			return;
		if (!save && userList.contains(id))
			userList.remove(id);
		if (save && !userList.contains(id))
			userList.add(id);

		users.set("players", userList);
		try {
			users.save(usersFile);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public final FileConfiguration getConfig() {
		return config;
	}

	public final YamlConfiguration getUserFile() {
		return users;
	}

	public final int getConfigVersion() {
		return configVersion;
	}

}
