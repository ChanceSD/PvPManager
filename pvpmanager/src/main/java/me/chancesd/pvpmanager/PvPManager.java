package me.chancesd.pvpmanager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import me.chancesd.pvpmanager.command.Announce;
import me.chancesd.pvpmanager.command.Newbie;
import me.chancesd.pvpmanager.command.PM;
import me.chancesd.pvpmanager.command.PvP;
import me.chancesd.pvpmanager.command.PvPGlobal;
import me.chancesd.pvpmanager.command.PvPInfo;
import me.chancesd.pvpmanager.command.PvPList;
import me.chancesd.pvpmanager.command.PvPOverride;
import me.chancesd.pvpmanager.command.PvPStatus;
import me.chancesd.pvpmanager.command.Tag;
import me.chancesd.pvpmanager.command.Untag;
import me.chancesd.pvpmanager.library.metrics.CustomMetrics;
import me.chancesd.pvpmanager.listener.EntityListener;
import me.chancesd.pvpmanager.listener.EntityListener1_9;
import me.chancesd.pvpmanager.listener.PlayerListener;
import me.chancesd.pvpmanager.listener.PlayerListener1_11;
import me.chancesd.pvpmanager.manager.ConfigManager;
import me.chancesd.pvpmanager.manager.DependencyManager;
import me.chancesd.pvpmanager.manager.DisplayManager;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.manager.StorageManager;
import me.chancesd.pvpmanager.manager.UpdateManager;
import me.chancesd.pvpmanager.manager.WorldManager;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import me.chancesd.sdutils.library.PluginLibraries;
import me.chancesd.sdutils.plugin.SDPlugin;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;

public class PvPManager extends SDPlugin {

	private ConfigManager configM;
	private PlayerManager playerHandler;
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
		displayManager = new DisplayManager();
		worldManager = new WorldManager(this);
		playerHandler = new PlayerManager(this);
		startListeners();
		registerCommands();
		startMetrics();
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

	public PlayerManager getPlayerHandler() {
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
