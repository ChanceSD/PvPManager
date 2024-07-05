package me.chancesd.pvpmanager.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import me.chancesd.sdutils.database.DatabaseConfigBuilder.DatabaseType;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.listener.DebugEntityListener;
import me.chancesd.pvpmanager.manager.UpdateManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.setting.Locale;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.storage.fields.UserDataFields;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.scheduler.ScheduleUtils;

public class PM implements TabExecutor {

	private final PvPManager plugin;
	private DebugEntityListener damageListener;
	private final Worlds worldsSubcommand;

	// subcommands
	private static final String CLEANUP = "cleanup";
	private static final String CONVERT = "convert";
	private static final String DEBUG = "debug";
	private static final String RELOAD = "reload";
	private static final String UPDATE = "update";
	private static final String LOCALE = "locale";
	private static final String WORLDS = "worlds";

	public PM(final PvPManager plugin) {
		this.plugin = plugin;
		worldsSubcommand = new Worlds(plugin);
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (args.length == 0 && Permissions.COMMAND_MENU.hasPerm(sender)) {
			helpMenu(sender);
			return true;
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase(RELOAD)) {
				reload(sender);
				return true;
			}
			if (args[0].equalsIgnoreCase(UPDATE) && Permissions.ADMIN.hasPerm(sender)) {
				update(sender);
				return true;
			}
		}
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase(CLEANUP) && Permissions.ADMIN.hasPerm(sender)) {
				cleanup(sender, args);
				return true;
			} else if (args[0].equalsIgnoreCase(CONVERT) && Permissions.ADMIN.hasPerm(sender)) {
				convert(sender, args);
				return true;
			} else if (args[0].equalsIgnoreCase(DEBUG) && Permissions.COMMAND_DEBUG.hasPerm(sender)) {
				debug(sender, args);
				return true;
			} else if (args[0].equalsIgnoreCase(LOCALE) && Permissions.ADMIN.hasPerm(sender)) {
				locale(sender, args);
				return true;
			} else if (args[0].equalsIgnoreCase(WORLDS) && Permissions.ADMIN.hasPerm(sender)) {
				worldsSubcommand.onCommand(sender, args);
				return true;
			}
		}
		sender.sendMessage(Lang.ERROR_COMMAND.msg());
		return false;
	}

	private void cleanup(final CommandSender sender, final String[] args) {
		if (args.length == 1) {
			sender.sendMessage("§4§lUsage: §f/pmr cleanup <days>");
			sender.sendMessage("§cThis command will remove users from the database that haven't logged in during the last x days.");
			sender.sendMessage(
					"§cThis means that if they come back their remaining newbie protection(if they had any) will be gone and their previous PvP state will also be default.");
			return;
		}
		try {
			final long days = TimeUnit.DAYS.toMillis(Integer.parseInt(args[1]));
			sender.sendMessage("§2Cleaning up users that haven't logged in the past " + Integer.parseInt(args[1]) + " days");
			sender.sendMessage("§2This might take a while depending on the size of your database");
			new BukkitRunnable() {
				@Override
				public void run() {
					final ArrayList<UUID> ids = new ArrayList<>();
					for (final Map<String, Object> userData : plugin.getStorageManager().getStorage().getAllUserData()) {
						final String id = (String) userData.get(UserDataFields.UUID);
						final UUID uuid = UUID.fromString(id);
						final OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
						if (p.isOnline()) {
							continue;
						}
						if (System.currentTimeMillis() - p.getLastPlayed() > days) {
							ids.add(uuid);
						}
					}
					ids.forEach(plugin.getStorageManager().getStorage()::removeUserData);
					sender.sendMessage(Lang.PREFIXMSG + " §2Finished. Cleaned up " + ids.size() + " inactive users.");
				}
			}.runTaskAsynchronously(plugin);
		} catch (final NumberFormatException e) {
			sender.sendMessage("§cError, days must be a number!");
		}
	}

	private void convert(final CommandSender sender, final String[] args) {
		if (args.length == 1) {
			sender.sendMessage(Lang.PREFIXMSG + " §4§lUsage: §e/pmr convert <databaseType>");
			sender.sendMessage(Lang.PREFIXMSG + " §cCurrently the database types are: " + Arrays.asList(DatabaseType.values()));
			return;
		}

		final String dbType = args[1];
		final DatabaseType databaseType;
		try {
			databaseType = DatabaseType.valueOf(dbType.toUpperCase());
		} catch (final IllegalArgumentException e) {
			sender.sendMessage(Lang.PREFIXMSG + " §cInvalid database type. Available types are: " + Arrays.asList(DatabaseType.values()));
			return;
		}

		final DatabaseType currentType = plugin.getStorageManager().getStorage().getDatabaseType();
		if (currentType == databaseType) {
			sender.sendMessage(Lang.PREFIXMSG + " §cCan't convert. You are already running on " + databaseType);
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			sender.sendMessage("§2Starting database conversion from " + currentType + " to " + databaseType);
			try {
				plugin.getStorageManager().convertFromCurrent(databaseType, sender, System.currentTimeMillis());
			} catch (final Exception e) {
				sender.sendMessage(Lang.PREFIXMSG + " §cError! Make sure you entered the correct MySQL details in the config");
				return;
			}
			plugin.getConfig().set("Database.Type", databaseType.toString());
			plugin.saveConfig();
			reload(sender);
			sender.sendMessage(Lang.PREFIXMSG + " §aYou are now running on " + plugin.getStorageManager().getStorage().getDatabaseType());
		});
	}

	private void debug(final CommandSender sender, final String[] args) {
		CombatPlayer p = null;
		if (args.length == 2 && sender instanceof final Player player) {
			p = plugin.getPlayerManager().get(player);
		}
		if (args.length == 2 && args[1].equalsIgnoreCase("toggle")) {
			Conf.setDebug(!Conf.DEBUG_MODE.asBool());
			Log.info("Debug mode: " + Conf.DEBUG_MODE.asBool());
			sender.sendMessage("Debug mode: " + Conf.DEBUG_MODE.asBool());
		} else if (args.length == 2 && args[1].equalsIgnoreCase("damagedebug")) {
			if (damageListener == null) {
				sender.sendMessage("§4Warning §f- Some plugin features are disabled while in this mode");
				sender.sendMessage("Enabling a damage listener for debugging, check the console for details on every entity hit");
				sender.sendMessage("§cRun this command again §fafter you are done to disable debugging or reload the plugin");
				Conf.setDebug(true);
				damageListener = new DebugEntityListener(plugin.getPlayerManager());
				HandlerList.unregisterAll(plugin.getEntityListener());
				Bukkit.getServer().getPluginManager().registerEvents(damageListener, plugin);
			} else {
				HandlerList.unregisterAll(damageListener);
				Bukkit.getServer().getPluginManager().registerEvents(plugin.getEntityListener(), plugin);
				damageListener = null;
				Conf.setDebug(false);
				sender.sendMessage("Debug damage listener disabled");
			}
		} else if (args[1].equalsIgnoreCase("tagall")) {
			for (final CombatPlayer player : plugin.getPlayerManager().getPlayers().values()) {
				player.tag(true, player);
			}
			sender.sendMessage("Tagged all players");
		} else if (args[1].equalsIgnoreCase("untagall")) {
			for (final CombatPlayer player : plugin.getPlayerManager().getPlayers().values()) {
				player.untag(UntagReason.COMMAND);
			}
			sender.sendMessage("Untagged all players");
		} else if (args[1].equalsIgnoreCase("players")) {
			for (final CombatPlayer player : plugin.getPlayerManager().getPlayers().values()) {
				if (!Bukkit.getOnlinePlayers().contains(player.getPlayer())) {
					Log.info("UUID: " + player.getUUID() + " - Name: " + player.getName() + " - Metadata: " + player.getPlayer().hasMetadata("NPC"));
				}
			}
			Log.info("Players: " + plugin.getPlayerManager().getPlayers().size() + "/" + Bukkit.getOnlinePlayers().size());
		} else if (args.length == 3) {
			if (!CombatUtils.isOnline(args[2])) {
				sender.sendMessage("§4Player not online!");
				return;
			}
			p = plugin.getPlayerManager().get(Bukkit.getPlayer(args[2]));
		}
		if (p == null)
			return;
		if (args[1].equalsIgnoreCase("tag")) {
			p.tag(true, p);
		} else if (args[1].equalsIgnoreCase("attack")) {
			plugin.getServer().getPluginManager()
					.callEvent(new EntityDamageByEntityEvent(p.getPlayer(), p.getPlayer(), DamageCause.ENTITY_ATTACK,
							DamageSource.builder(DamageType.PLAYER_ATTACK).build(), 5.0));
			sender.sendMessage("Attacked player with 5 damage");
		}
	}

	private void locale(final CommandSender sender, final String[] args) {
		if (args.length == 1) {
			sender.sendMessage(Lang.PREFIXMSG + " §aYour current Locale is: §c" + Lang.getLocale());
			sender.sendMessage(Lang.PREFIXMSG + " §aAvailable languages are: §c" + Locale.asStringList());
			return;
		}

		Locale locale;
		try {
			locale = Locale.valueOf(args[1].toUpperCase());
		} catch (final IllegalArgumentException e) {
			sender.sendMessage(Lang.PREFIXMSG + " §cInvalid Locale. Available languages are: " + Locale.asStringList());
			return;
		}
		if (Lang.getLocale() == locale) {
			sender.sendMessage(Lang.PREFIXMSG + " §cCan't change Locale. You are already using " + locale);
			return;
		}

		Conf.LOCALE.set(locale.name());
		changeConfigSetting("General.Locale", locale.name());
		Lang.setup(plugin);
		sender.sendMessage(Lang.PREFIXMSG + " §aLanguage changed to " + Lang.getLocale() + " - Filename: " + Lang.getLocale().fileName());
	}

	private void changeConfigSetting(final String path, final String value) {
		plugin.reloadConfig();
		plugin.getConfig().set(path, value);
		plugin.saveConfig();
	}

	private void reload(final CommandSender sender) {
		if (!Permissions.COMMAND_RELOAD.hasPerm(sender)) {
			sender.sendMessage(Lang.ERROR_PERMISSION.msg());
			return;
		}

		reload(false);
		sender.sendMessage(Lang.PREFIXMSG + " §aPvPManager reloaded!");
	}

	private void reload(final boolean silent) {
		Log.setSilent(silent);
		plugin.setReloading(true);
		plugin.getUpdateManager().setUpdateAvailable(false);
		plugin.onDisable();
		ScheduleUtils.cancelAllTasks();
		HandlerList.unregisterAll(plugin);
		plugin.onEnable();
		plugin.setReloading(false);
		if (silent)
			Log.setSilent(false);
	}

	private void update(final CommandSender sender) {
		if (Conf.CHECK_UPDATES.asBool()) {
			final UpdateManager updateManager = plugin.getUpdateManager();
			if (updateManager.hasUpdateAvailable()) {
				if (updateManager.getUpdater().downloadFile()) {
					sender.sendMessage("§2Update Successful. On next restart you will have §e" + updateManager.getNewVersion());
				} else {
					sender.sendMessage("§4An error ocurred while updating, please report to the developer");
				}
			} else {
				sender.sendMessage("§2You have the latest version: §ePvPManager v" + updateManager.getCurrentversion());
			}
		} else {
			sender.sendMessage("§4Update Checking is disabled, enable it in the Config file");
		}
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 1)
			return ChatUtils.getMatchingEntries(args[0], Lists.newArrayList(CLEANUP, CONVERT, DEBUG, RELOAD, UPDATE, LOCALE, WORLDS));
		if (args.length == 2 && args[0].equalsIgnoreCase(CONVERT))
			return ChatUtils.getMatchingEntries(args[1], Lists.newArrayList("SQLITE", "MYSQL"));
		if (args.length == 2 && args[0].equalsIgnoreCase(DEBUG))
			return ChatUtils.getMatchingEntries(args[1], Lists.newArrayList("toggle", "damagedebug", "tag", "tagall", "attack", "players"));
		if (args.length == 2 && args[0].equalsIgnoreCase(LOCALE))
			return ChatUtils.getMatchingEntries(args[1], Locale.asStringList());

		return Collections.emptyList();
	}

	public static void helpMenu(final CommandSender player) {
		player.sendMessage(ChatColor.GOLD + "-------------- PvPManager Help Page --------------");
		player.sendMessage(ChatColor.GOLD + "/pvp [player]" + helpSeparator() + ChatColor.WHITE + "Set PvP enabled or disabled");
		player.sendMessage(ChatColor.GOLD + "/pvpinfo [player]" + helpSeparator() + ChatColor.WHITE + "Check your or other player info");
		player.sendMessage(ChatColor.GOLD + "/pvplist [offline]" + helpSeparator() + ChatColor.WHITE + "List all online or offline players");
		player.sendMessage(ChatColor.GOLD + "/pvpo " + helpSeparator() + ChatColor.WHITE + "Override all PvP protections");
		player.sendMessage(ChatColor.GOLD + "/pvpstatus [player]" + helpSeparator() + ChatColor.WHITE + "Check your or other player PvP status");
		player.sendMessage(ChatColor.GOLD + "/pvpglobal <on|off>" + helpSeparator() + ChatColor.WHITE + "Toggle PvP for the whole server");
		player.sendMessage(ChatColor.GOLD + "/pvptag <player> <time>" + helpSeparator() + ChatColor.WHITE + "Check tag time left or tag a player");
		player.sendMessage(ChatColor.GOLD + "/untag <player>" + helpSeparator() + ChatColor.WHITE + "Untags a player");
		player.sendMessage(ChatColor.GOLD + "/newbie disable" + helpSeparator() + ChatColor.WHITE + "Disable newbie protection");
		player.sendMessage(ChatColor.GOLD + "/pmr" + helpSeparator() + ChatColor.WHITE + "Shows this help page");
		player.sendMessage(ChatColor.GOLD + "/pmr worlds" + helpSeparator() + ChatColor.WHITE + "Control panel to manage world PvP");
		player.sendMessage(ChatColor.GOLD + "/pmr reload" + helpSeparator() + ChatColor.WHITE + "Reload PvPManager");
		player.sendMessage(ChatColor.GOLD + "/pmr cleanup" + helpSeparator() + ChatColor.WHITE + "Cleanup inactive users from database");
		player.sendMessage(ChatColor.GOLD + "/pmr update" + helpSeparator() + ChatColor.WHITE + "Update to latest version");
		player.sendMessage(ChatColor.GOLD + "-------------------------------------------------");
	}

	private static String helpSeparator() {
		return ChatColor.RED + " >> ";
	}

}
