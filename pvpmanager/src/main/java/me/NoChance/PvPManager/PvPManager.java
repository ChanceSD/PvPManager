package me.NoChance.PvPManager;

import me.chancesd.pvpmanager.managers.WorldManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import me.NoChance.PvPManager.Commands.Announce;
import me.NoChance.PvPManager.Commands.Newbie;
import me.NoChance.PvPManager.Commands.PM;
import me.NoChance.PvPManager.Commands.PvP;
import me.NoChance.PvPManager.Commands.PvPGlobal;
import me.NoChance.PvPManager.Commands.PvPInfo;
import me.NoChance.PvPManager.Commands.PvPList;
import me.NoChance.PvPManager.Commands.PvPOverride;
import me.NoChance.PvPManager.Commands.PvPStatus;
import me.NoChance.PvPManager.Commands.Tag;
import me.NoChance.PvPManager.Commands.Untag;
import me.NoChance.PvPManager.Libraries.Metrics.CustomMetrics;
import me.NoChance.PvPManager.Listeners.EntityListener;
import me.NoChance.PvPManager.Listeners.EntityListener1_9;
import me.NoChance.PvPManager.Listeners.PlayerListener;
import me.NoChance.PvPManager.Listeners.PlayerListener1_11;
import me.NoChance.PvPManager.Managers.ConfigManager;
import me.NoChance.PvPManager.Managers.DependencyManager;
import me.NoChance.PvPManager.Managers.DisplayManager;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.chancesd.sdutils.library.PluginLibraries;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.pvpmanager.managers.StorageManager;
import me.chancesd.pvpmanager.managers.UpdateManager;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import me.NoChance.PvPManager.Utils.MCVersion;

public class PvPManager extends JavaPlugin {

	private ConfigManager configM;
	private PlayerHandler playerHandler;
	private UpdateManager updateManager;
	private StorageManager storageManager;
	private DependencyManager dependencyManager;
	private DisplayManager displayManager;
	private WorldManager worldManager;
	private EntityListener entityListener;
	private static PvPManager instance;

	@Override
	public void onLoad() {
		Log.setup(getLogger(), Messages.PREFIXMSG);
		PluginLibraries.checkDependencies(this);
	}

	@Override
	public void onEnable() {
		final long start = System.currentTimeMillis();
		instance = this;
		ScheduleUtils.setupExecutor();
		if (ScheduleUtils.checkFolia()) {
			Log.infoColor(ChatColor.AQUA + "Running on Folia. Support for Folia is still experimental");
			Log.infoColor(ChatColor.AQUA + "Please report any errors you find, most likely nobody reported them yet as not many people use Folia");
		}
		loadFiles();
		updateManager = new UpdateManager(this);
		storageManager = new StorageManager(this);
		dependencyManager = new DependencyManager();
		displayManager = new DisplayManager(this);
		worldManager = new WorldManager(this);
		playerHandler = new PlayerHandler(this);
		startListeners();
		registerCommands();
		startMetrics();
		checkJavaVersion();
		Log.infoColor(ChatColor.GREEN + getDescription().getFullName() + " enabled " + ChatColor.GRAY + "("
				+ (System.currentTimeMillis() - start) + " ms)");
	}

	@Override
	public void onDisable() {
		playerHandler.handlePluginDisable();
		storageManager.shutdown();
		ScheduleUtils.cancelAllTasks();
		instance = null;
	}

	private void loadFiles() {
		this.configM = new ConfigManager(this);
		Messages.setup(this);
	}

	private void startListeners() {
		if (MCVersion.isAtLeast(MCVersion.V1_9)) {
			registerListener(new EntityListener1_9(playerHandler));
		}
		entityListener = new EntityListener(playerHandler);
		registerListener(entityListener);
		if (MCVersion.isAtLeast(MCVersion.V1_11_2)) {
			registerListener(new PlayerListener1_11(playerHandler));
		}
		registerListener(new PlayerListener(playerHandler));
		dependencyManager.startListeners(this);
	}

	private void registerCommands() {
		registerCommand(getCommand("pvp"), new PvP(playerHandler));
		registerCommand(getCommand("newbie"), new Newbie(playerHandler));
		registerCommand(getCommand("pvpmanager"), new PM(this));
		registerCommand(getCommand("pvpoverride"), new PvPOverride(playerHandler));
		registerCommand(getCommand("pvpinfo"), new PvPInfo(playerHandler));
		registerCommand(getCommand("pvplist"), new PvPList(playerHandler));
		registerCommand(getCommand("pvpstatus"), new PvPStatus(playerHandler));
		registerCommand(getCommand("pvptag"), new Tag(playerHandler));
		registerCommand(getCommand("untag"), new Untag(playerHandler));
		registerCommand(getCommand("announce"), new Announce());
		registerCommand(getCommand("pvpglobal"), new PvPGlobal());
	}

	private void startMetrics() {
		new CustomMetrics(this);
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
			} else {
				final int separator = version.indexOf("-");
				if (separator != -1) {
					version = version.substring(0, separator);
				}
			}
		}
		try {
			javaVersion = Integer.parseInt(version);
		} catch (final NumberFormatException e) {
			return;
		}
		if (javaVersion < 16) {
			Log.severe("You appear to be using Java 15 or lower. For now the plugin still works but please update to Java 16+");
			Log.severe("In the future PvPManager will stop supporting Java versions this old");
		}
	}

	private void registerListener(final Listener listener) {
		this.getServer().getPluginManager().registerEvents(listener, this);
	}

	private void registerCommand(final PluginCommand command, final CommandExecutor executor) {
		if (command == null)
			return;
		command.setExecutor(executor);
	}

	public EntityListener getEntityListener() {
		return entityListener;
	}

	public ConfigManager getConfigM() {
		return configM;
	}

	public PlayerHandler getPlayerHandler() {
		return playerHandler;
	}

	public UpdateManager getUpdateManager() {
		return updateManager;
	}

	public StorageManager getStorageManager() {
		return storageManager;
	}

	public DependencyManager getDependencyManager() {
		return dependencyManager;
	}

	public DisplayManager getDisplayManager() {
		return displayManager;
	}

	public WorldManager getWorldManager() {
		return worldManager;
	}

	/**
	 * @return instance of PvPManager
	 */
	public static PvPManager getInstance() {
		return instance;
	}

}
