package me.NoChance.PvPManager;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import me.NoChance.PvPManager.Commands.Announce;
import me.NoChance.PvPManager.Commands.Newbie;
import me.NoChance.PvPManager.Commands.PM;
import me.NoChance.PvPManager.Commands.PvP;
import me.NoChance.PvPManager.Commands.PvPInfo;
import me.NoChance.PvPManager.Commands.PvPList;
import me.NoChance.PvPManager.Commands.PvPOverride;
import me.NoChance.PvPManager.Commands.PvPStatus;
import me.NoChance.PvPManager.Commands.Tag;
import me.NoChance.PvPManager.Libraries.Metrics.CustomMetrics;
import me.NoChance.PvPManager.Libraries.Updater.BukkitUpdater;
import me.NoChance.PvPManager.Libraries.Updater.SpigotUpdater;
import me.NoChance.PvPManager.Libraries.Updater.Updater;
import me.NoChance.PvPManager.Libraries.Updater.Updater.UpdateResult;
import me.NoChance.PvPManager.Libraries.Updater.Updater.UpdateType;
import me.NoChance.PvPManager.Listeners.EntityListener;
import me.NoChance.PvPManager.Listeners.EntityListener1_9;
import me.NoChance.PvPManager.Listeners.PlayerListener;
import me.NoChance.PvPManager.Managers.ConfigManager;
import me.NoChance.PvPManager.Managers.DependencyManager;
import me.NoChance.PvPManager.Managers.DisplayManager;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Log;

public final class PvPManager extends JavaPlugin {

	private ConfigManager configM;
	private PlayerHandler playerHandler;
	private Updater updater;
	private DependencyManager dependencyManager;
	private DisplayManager displayManager;
	private static PvPManager instance;

	@Override
	public void onEnable() {
		final long start = System.currentTimeMillis();
		instance = this;
		Log.setup(getLogger());
		loadFiles();
		dependencyManager = new DependencyManager();
		displayManager = new DisplayManager(this);
		playerHandler = new PlayerHandler(this);
		startListeners();
		getCommand("pvp").setExecutor(new PvP(playerHandler));
		getCommand("newbie").setExecutor(new Newbie(playerHandler));
		getCommand("pvpmanager").setExecutor(new PM(this));
		getCommand("pvpoverride").setExecutor(new PvPOverride(playerHandler));
		getCommand("pvpinfo").setExecutor(new PvPInfo(playerHandler));
		getCommand("pvplist").setExecutor(new PvPList(playerHandler));
		getCommand("pvpstatus").setExecutor(new PvPStatus(playerHandler));
		getCommand("pvptag").setExecutor(new Tag(playerHandler));
		getCommand("announce").setExecutor(new Announce());
		startMetrics();
		checkJavaVersion();
		Log.info("PvPManager Enabled (" + (System.currentTimeMillis() - start) + " ms)");
	}

	@Override
	public void onDisable() {
		playerHandler.handlePluginDisable();
		instance = null;
	}

	private void loadFiles() {
		this.configM = new ConfigManager(this);
		Messages.setup(this);
	}

	private void startListeners() {
		if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9")) {
			registerListener(new EntityListener1_9(playerHandler));
		}
		registerListener(new EntityListener(playerHandler));
		registerListener(new PlayerListener(playerHandler));
		dependencyManager.startListeners(playerHandler);
	}

	private void startMetrics() {
		new CustomMetrics(this);
	}

	public void checkForUpdates() {
		Log.info("Checking for updates...");
		updater = new BukkitUpdater(this, 63773, UpdateType.VERSION_CHECK);
		if (updater.getResult().name().startsWith("FAIL")) {
			updater = new SpigotUpdater(this, 845, UpdateType.VERSION_CHECK);
		}
		if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
			Messages.setNewVersion(updater.getLatestName());
			Bukkit.broadcast("§6[§8PvPManager§6]§2 Update Available: §e" + Messages.getNewVersion(), "pvpmanager.admin");
			if (Settings.isAutoUpdate()) {
				if (updater.downloadFile()) {
					Log.info("Version Downloaded To Your Update Folder");
					return;
				}
				Log.info("Could not download latest update. Please update manually from one of the links below.");
			}
			Settings.setUpdate(true);
			Bukkit.broadcast("§6[§8PvPManager§6]§2 §6Spigot Link: §8https://www.spigotmc.org/resources/pvpmanager.845/", "pvpmanager.admin");
			Bukkit.broadcast("§6[§8PvPManager§6]§2 §6Bukkit Link: §8https://dev.bukkit.org/projects/pvpmanager", "pvpmanager.admin");
		} else {
			Log.info("No update found");
		}
	}

	private void checkJavaVersion() {
		int javaVersion;
		String version = System.getProperty("java.version");
		if (version.startsWith("1.")) {
			version = version.substring(2, 3);
		} else {
			final int dot = version.indexOf(".");
			if (dot != -1) {
				version = version.substring(0, dot);
			}
		}
		javaVersion = Integer.parseInt(version);
		if (javaVersion <= 8) {
			Log.warning("You appear to be using Java 8 or lower. For now the plugin still works but please update to Java 16+");
			Log.warning("In the future PvPManager might stop supporting Java versions this old");
		}
	}

	private void registerListener(final Listener listener) {
		this.getServer().getPluginManager().registerEvents(listener, this);
	}

	public ConfigManager getConfigM() {
		return configM;
	}

	public PlayerHandler getPlayerHandler() {
		return playerHandler;
	}

	public DependencyManager getDependencyManager() {
		return dependencyManager;
	}

	public Updater getUpdater() {
		return updater;
	}

	public DisplayManager getDisplayManager() {
		return displayManager;
	}

	/**
	 * @return instance of PvPManager
	 */
	public static PvPManager getInstance() {
		return instance;
	}

}
