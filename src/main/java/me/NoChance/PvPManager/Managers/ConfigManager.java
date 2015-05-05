package me.NoChance.PvPManager.Managers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Config;
import me.NoChance.PvPManager.Config.Variables;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {

	private PvPManager plugin;
	private File usersFile;
	private YamlConfiguration users;
	private Config config;
	public static int configVersion;

	public ConfigManager(PvPManager plugin) {
		this.plugin = plugin;
		this.users = new YamlConfiguration();
		this.usersFile = new File(plugin.getDataFolder(), "users.yml");
		configVersion = plugin.getConfig().getInt("Config Version", 0);
		loadConfig();
		loadUsers();
	}

	private void updateDefaultConfig() {
		this.config.set("Default PvP", Variables.defaultPvp);
		this.config.set("PvP Blood", Variables.pvpBlood);
		this.config.set("Auto Soup.Enabled", Variables.autoSoupEnabled);
		this.config.set("Auto Soup.Health Gain", Variables.soupHealth);
		this.config.set("Disable Fly", Variables.disableFly);
		this.config.set("Disable GameMode", Variables.disableGamemode);
		this.config.set("Disable Disguise", Variables.disableDisguise);
		this.config.set("Disable Invisibility", Variables.disableInvisibility);
		this.config.set("Ignore Zones For Tagged", Variables.stopBorderHopping);
		this.config.set("Ignore No Damage Hits", Variables.ignoreNoDamageHits);

		this.config.set("In Combat.Enabled", Variables.inCombatEnabled);
		this.config.set("In Combat.Silent", Variables.inCombatSilent);
		this.config.set("In Combat.Time(seconds)", Variables.timeInCombat);
		this.config.set("In Combat.Name Tag Color", Variables.nameTagColor);
		this.config.set("In Combat.Only Tag Attacker", Variables.onlyTagAttacker);
		this.config.set("In Combat.Block EnderPearl", Variables.blockEnderPearl);
		this.config.set("In Combat.Stop Commands.Enabled", Variables.stopCommands);
		this.config.set("In Combat.Stop Commands.Allowed Commands", Variables.commandsAllowed);
		this.config.set("In Combat.Punishments.Enabled", Variables.punishmentsEnabled);
		this.config.set("In Combat.Punishments.Broadcast PvPLog", Variables.broadcastPvpLog);
		this.config.set("In Combat.Punishments.Log To File", Variables.logToFile);
		this.config.set("In Combat.Punishments.Kill on Logout", Variables.killOnLogout);
		this.config.set("In Combat.Punishments.Drops.Inventory", Variables.dropInventory);
		this.config.set("In Combat.Punishments.Drops.Experience", Variables.dropExp);
		this.config.set("In Combat.Punishments.Drops.Armor", Variables.dropArmor);
		this.config.set("In Combat.Punishments.Fine.Enabled", Variables.fineEnabled);
		this.config.set("In Combat.Punishments.Fine.Amount", Variables.fineAmount);

		this.config.set("Player Kills.Transfer Drops", Variables.transferDrops);
		this.config.set("Player Kills.Money Reward", Variables.moneyReward);
		this.config.set("Player Kills.Money Penalty", Variables.moneyPenalty);
		this.config.set("Player Kills.Commands On Kill.Enabled", Variables.commandsOnKillEnabled);
		this.config.set("Player Kills.Commands On Kill.Commands", Variables.commandsOnKill);

		this.config.set("PvP Toggle.Cooldown(seconds)", Variables.toggleCooldown);
		this.config.set("PvP Toggle.Broadcast", Variables.toggleBroadcast);
		this.config.set("PvP Toggle.Toggle Off on Death", Variables.toggleOffOnDeath);
		this.config.set("PvP Toggle.NameTags.Enabled", Variables.toggleNametagsEnabled);
		this.config.set("PvP Toggle.NameTags.Color On", Variables.toggleColorOn);
		this.config.set("PvP Toggle.NameTags.Color Off", Variables.toggleColorOff);

		this.config.set("Toggle Signs.Enabled", Variables.toggleSignsEnabled);
		this.config.set("Toggle Signs.Disable Toggle Command", Variables.disableToggleCommand);

		this.config.set("Kill Abuse.Enabled", Variables.killAbuseEnabled);
		this.config.set("Kill Abuse.Max Kills", Variables.killAbuseMaxKills);
		this.config.set("Kill Abuse.Time Limit", Variables.killAbuseTime);
		this.config.set("Kill Abuse.Commands on Abuse", Variables.killAbuseCommands);
		this.config.set("Kill Abuse.Respawn Protection", Variables.respawnProtection);

		this.config.set("Newbie Protection.Enabled", Variables.newbieProtectionEnabled);
		this.config.set("Newbie Protection.Time(minutes)", Variables.newbieProtectionTime);
		this.config.set("Newbie Protection.Block Pick Items", Variables.blockPickNewbies);
		this.config.set("Newbie Protection.Protect From Everything", Variables.newbieGodMode);

		this.config.set("Update Check.Enabled", Variables.updateCheck);
		this.config.set("Update Check.Auto Update", Variables.autoUpdate);

		this.config.set("World Exclusions", Variables.worldsExcluded);
		this.config.saveConfig();
	}

	private void loadConfig() {
		if (configVersion < 25) {
			File configFile = new File(plugin.getDataFolder(), "config.yml");
			if (configFile.exists()) {
				config = new Config(plugin, "config.yml");
				new Variables(this);
				configFile.delete();
				config = new Config(plugin, "config.yml");
				updateDefaultConfig();
				Variables.configUpdated = true;
				configVersion = config.getInt("Config Version");
			} else {
				plugin.getLogger().info("New Config File Created Successfully!");
				config = new Config(plugin, "config.yml");
				new Variables(this);
			}
		} else {
			config = new Config(plugin, "config.yml");
			new Variables(this);
		}
	}

	private void loadUsers() {
		try {
			if (!usersFile.exists()) {
				plugin.saveResource("users.yml", false);
				plugin.getLogger().info("New Users File Created Successfully!");
				return;
			}
			users.load(usersFile);
		} catch (Exception e) {
			plugin.getLogger().severe("Error loading users file! Error: ");
			e.printStackTrace();
		}
	}

	public void saveUser(UUID uuid, boolean save) {
		String id = uuid.toString();
		List<String> userList = users.getStringList("players");
		if (save && userList.contains(id) || !save && !userList.contains(id))
			return;
		if (!save && userList.contains(id))
			userList.remove(id);
		if (save && !userList.contains(id))
			userList.add(id);

		users.set("players", userList);
		try {
			users.save(usersFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public YamlConfiguration getUserFile() {
		return users;
	}

}
