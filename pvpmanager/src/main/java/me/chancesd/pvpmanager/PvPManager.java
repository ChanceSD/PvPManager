package me.chancesd.pvpmanager;

import org.bukkit.ChatColor;
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
import me.chancesd.pvpmanager.listener.BlockedActionsListener;
import me.chancesd.pvpmanager.listener.EntityListener;
import me.chancesd.pvpmanager.listener.EntityListener1_20_4;
import me.chancesd.pvpmanager.listener.EntityListener1_9;
import me.chancesd.pvpmanager.listener.PlayerListener;
import me.chancesd.pvpmanager.listener.PlayerListener1_11;
import me.chancesd.pvpmanager.manager.ConfigManager;
import me.chancesd.pvpmanager.manager.DependencyManager;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.manager.StorageManager;
import me.chancesd.pvpmanager.manager.UpdateManager;
import me.chancesd.pvpmanager.manager.WorldManager;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.plugin.SDPlugin;
import me.chancesd.sdutils.scheduler.ScheduleUtils;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;

public class PvPManager extends SDPlugin {

	private ConfigManager configM;
	private PlayerManager playerHandler;
	private UpdateManager updateManager;
	private StorageManager storageManager;
	private DependencyManager dependencyManager;
	private WorldManager worldManager;
	private EntityListener entityListener;
	private static PvPManager instance;

	@Override
	public void onPluginLoad() {
		Log.setup(getLogger(), "§7[§4§lPvP§8§lManager§7]");
	}

	@Override
	public void onPluginEnable() {
		final long start = System.currentTimeMillis();
		instance = this;
		ScheduleUtils.setupExecutor(this);
		if (ScheduleUtils.checkFolia()) {
			Log.infoColor(ChatColor.AQUA + "Running on Folia. Support for Folia is still experimental");
			Log.infoColor(ChatColor.AQUA + "Please report any errors you find, most likely nobody reported them yet as not many people use Folia");
		}
		loadFiles();
		updateManager = new UpdateManager(this);
		storageManager = new StorageManager(this);
		dependencyManager = new DependencyManager();
		worldManager = new WorldManager(this);
		playerHandler = new PlayerManager(this);
		startListeners();
		registerCommands();
		startMetrics();
		Log.infoColor(ChatColor.GREEN + getDescription().getFullName() + " enabled " + ChatColor.GRAY + "("
				+ (System.currentTimeMillis() - start) + " ms)");
	}

	@Override
	public void onPluginDisable() {
		playerHandler.handlePluginDisable();
		storageManager.shutdown();
		ScheduleUtils.cancelAllTasks();
		instance = null;
	}

	private void loadFiles() {
		this.configM = new ConfigManager(this);
		Lang.setup(this);
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
		registerListener(new BlockedActionsListener(playerHandler));
		if (MCVersion.isAtLeast(MCVersion.V1_20_4)) {
			new EntityListener1_20_4(playerHandler);
		}
		dependencyManager.startListeners(this);
	}

	private void registerCommands() {
		BaseCommand.setGlobalDefaults(
				sender -> Lang.ERROR_PERMISSION.msg(),
				sender -> Lang.ERROR_NOT_PLAYER.msg(),
				Lang.ERROR_PLAYER_NOT_FOUND::msg);

		registerCommand("pvp", cmd -> new PvP(cmd, playerHandler));
		registerCommand("newbie", cmd -> new Newbie(cmd, playerHandler));
		registerCommand("pvpmanager", cmd -> new PM(cmd, this));
		registerCommand("pvpoverride", cmd -> new PvPOverride(cmd, playerHandler));
		registerCommand("pvpinfo", cmd -> new PvPInfo(cmd, playerHandler));
		registerCommand("pvplist", cmd -> new PvPList(cmd, playerHandler));
		registerCommand("pvpstatus", cmd -> new PvPStatus(cmd, playerHandler));
		registerCommand("pvptag", cmd -> new Tag(cmd, playerHandler));
		registerCommand("untag", cmd -> new Untag(cmd, playerHandler));
		registerCommand("announce", Announce::new);
		registerCommand("pvpglobal", cmd -> new PvPGlobal(cmd, playerHandler));
	}

	private void startMetrics() {
		new CustomMetrics(this);
	}

	public EntityListener getEntityListener() {
		return entityListener;
	}

	public ConfigManager getConfigM() {
		return configM;
	}

	public PlayerManager getPlayerManager() {
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
