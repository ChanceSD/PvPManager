package me.NoChance.PvPManager.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Listeners.DebugEntityListener;
import me.NoChance.PvPManager.Settings.Locale;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.ChatUtils;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.storage.DatabaseConfigBuilder.DatabaseType;
import me.chancesd.pvpmanager.storage.fields.UserDataFields;
import me.chancesd.pvpmanager.utils.ScheduleUtils;

public class PM implements TabExecutor {

	private final PvPManager plugin;
	private DebugEntityListener damageListener;
	private final Worlds worldsSubcommand;

	public PM(final PvPManager plugin) {
		this.plugin = plugin;
		worldsSubcommand = new Worlds(plugin);
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (args.length == 0 && player.hasPermission("pvpmanager.menu")) {
				Settings.helpMenu(player);
				return true;
			}
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("reload")) {
				reload(sender);
				return true;
			}
			if (args[0].equalsIgnoreCase("update") && Permissions.ADMIN.hasPerm(sender)) {
				update(sender);
				return true;
			}
		}
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("cleanup") && Permissions.ADMIN.hasPerm(sender)) {
				cleanup(sender, args);
				return true;
			} else if (args[0].equalsIgnoreCase("convert") && Permissions.ADMIN.hasPerm(sender)) {
				convert(sender, args);
				return true;
			} else if (args[0].equalsIgnoreCase("debug") && sender.hasPermission("pvpmanager.debug")) {
				debug(sender, args);
				return true;
			} else if (args[0].equalsIgnoreCase("locale") && Permissions.ADMIN.hasPerm(sender)) {
				locale(sender, args);
				return true;
			} else if (args[0].equalsIgnoreCase("worlds") && Permissions.ADMIN.hasPerm(sender)) {
				worldsSubcommand.onCommand(sender, cmd, label, args);
				return true;
			}
		}
		sender.sendMessage(Messages.getErrorCommand());
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
					sender.sendMessage(Messages.PREFIXMSG + " §2Finished. Cleaned up " + ids.size() + " inactive users.");
				}
			}.runTaskAsynchronously(plugin);
		} catch (final NumberFormatException e) {
			sender.sendMessage("§cError, days must be a number!");
		}
	}

	private void convert(final CommandSender sender, final String[] args) {
		if (args.length == 1) {
			sender.sendMessage(Messages.PREFIXMSG + " §4§lUsage: §e/pmr convert <databaseType>");
			sender.sendMessage(Messages.PREFIXMSG + " §cCurrently the database types are: " + Arrays.asList(DatabaseType.values()));
			return;
		}

		final String dbType = args[1];
		final DatabaseType databaseType;
		try {
			databaseType = DatabaseType.valueOf(dbType.toUpperCase());
		} catch (final IllegalArgumentException e) {
			sender.sendMessage(Messages.PREFIXMSG + " §cInvalid database type. Available types are: " + Arrays.asList(DatabaseType.values()));
			return;
		}

		final DatabaseType currentType = plugin.getStorageManager().getStorage().getDatabaseType();
		if (currentType == databaseType) {
			sender.sendMessage(Messages.PREFIXMSG + " §cCan't convert. You are already running on " + databaseType);
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			sender.sendMessage("§2Starting database conversion from " + currentType + " to " + databaseType);
			try {
				plugin.getStorageManager().convertFromCurrent(databaseType, sender, System.currentTimeMillis());
			} catch (final Exception e) {
				sender.sendMessage(Messages.PREFIXMSG + " §cError! Make sure you entered the correct MySQL details in the config");
				return;
			}
			plugin.getConfig().set("Database.Type", databaseType.toString());
			plugin.saveConfig();
			reload(sender);
			sender.sendMessage(Messages.PREFIXMSG + " §aYou are now running on " + plugin.getStorageManager().getStorage().getDatabaseType());
		});
	}

	private void debug(final CommandSender sender, final String[] args) {
		PvPlayer p = null;
		if (args.length == 2 && sender instanceof Player) {
			p = plugin.getPlayerHandler().get((Player) sender);
		}
		if (args.length == 2 && args[1].equalsIgnoreCase("toggle")) {
			Settings.setDEBUG(!Settings.DEBUG);
			Log.info("Debug mode: " + Settings.DEBUG);
			sender.sendMessage("Debug mode: " + Settings.DEBUG);
		} else if (args.length == 2 && args[1].equalsIgnoreCase("damagedebug")) {
			if (damageListener == null) {
				sender.sendMessage("§4Warning §f- Some plugin features are disabled while in this mode");
				sender.sendMessage("Enabling a damage listener for debugging, check the console for details on every entity hit");
				sender.sendMessage("§cRun this command again §fafter you are done to disable debugging or reload the plugin");
				Settings.setDEBUG(true);
				damageListener = new DebugEntityListener(plugin.getPlayerHandler());
				HandlerList.unregisterAll(plugin.getEntityListener());
				Bukkit.getServer().getPluginManager().registerEvents(damageListener, plugin);
			} else {
				HandlerList.unregisterAll(damageListener);
				Bukkit.getServer().getPluginManager().registerEvents(plugin.getEntityListener(), plugin);
				damageListener = null;
				Settings.setDEBUG(false);
				sender.sendMessage("Debug damage listener disabled");
			}
		} else if (args[1].equalsIgnoreCase("tagall")) {
			for (final PvPlayer player : plugin.getPlayerHandler().getPlayers().values()) {
				player.setTagged(true, player);
			}
			sender.sendMessage("Tagged all players");
		} else if (args[1].equalsIgnoreCase("untagall")) {
			for (final PvPlayer player : plugin.getPlayerHandler().getPlayers().values()) {
				plugin.getPlayerHandler().untag(player);
			}
			sender.sendMessage("Untagged all players");
		} else if (args[1].equalsIgnoreCase("players")) {
			for (final PvPlayer player : plugin.getPlayerHandler().getPlayers().values()) {
				if (!Bukkit.getOnlinePlayers().contains(player.getPlayer())) {
					Log.info("UUID: " + player.getUUID() + " - Name: " + player.getName() + " - Metadata: " + player.getPlayer().hasMetadata("NPC"));
				}
			}
			Log.info("Players: " + plugin.getPlayerHandler().getPlayers().size() + "/" + Bukkit.getOnlinePlayers().size());
		} else if (args.length == 3) {
			if (!CombatUtils.isOnline(args[2])) {
				sender.sendMessage("§4Player not online!");
				return;
			}
			p = plugin.getPlayerHandler().get(Bukkit.getPlayer(args[2]));
		}
		if (p == null)
			return;
		if (args[1].equalsIgnoreCase("tag")) {
			p.setTagged(true, p);
		} else if (args[1].equalsIgnoreCase("attack")) {
			plugin.getServer().getPluginManager()
					.callEvent(new EntityDamageByEntityEvent(p.getPlayer(), p.getPlayer(), DamageCause.ENTITY_ATTACK, 5.0));
			sender.sendMessage("Attacked player with 5 damage");
		}
	}

	private void locale(final CommandSender sender, final String[] args) {
		if (args.length == 1) {
			sender.sendMessage(Messages.PREFIXMSG + " §aYour current Locale is: §c" + Messages.getLocale());
			sender.sendMessage(Messages.PREFIXMSG + " §aAvailable languages are: §c" + Locale.asStringList());
			return;
		}

		Locale locale;
		try {
			locale = Locale.valueOf(args[1].toUpperCase());
		} catch (final IllegalArgumentException e) {
			sender.sendMessage(Messages.PREFIXMSG + " §cInvalid Locale. Available languages are: " + Locale.asStringList());
			return;
		}
		if (Messages.getLocale() == locale) {
			sender.sendMessage(Messages.PREFIXMSG + " §cCan't change Locale. You are already using " + locale);
			return;
		}

		Settings.setLocale(locale.name());
		changeConfigSetting("General.Locale", locale.name());
		Messages.setup(plugin);
		sender.sendMessage(Messages.PREFIXMSG + " §aLanguage changed to " + Messages.getLocale() + " - Filename: " + Messages.getLocale().fileName());
	}

	private void changeConfigSetting(final String path, final String value) {
		plugin.reloadConfig();
		plugin.getConfig().set(path, value);
		plugin.saveConfig();
	}

	private void reload(final CommandSender sender) {
		if (!sender.hasPermission("pvpmanager.reload")) {
			sender.sendMessage(Messages.getErrorPermission());
			return;
		}

		reload(false);
		sender.sendMessage(Messages.PREFIXMSG + " §aPvPManager reloaded!");
	}

	private void reload(final boolean silent) {
		Log.setSilent(silent);
		Settings.setReloading(true);
		Settings.setUpdate(false);
		plugin.onDisable();
		ScheduleUtils.cancelAllTasks();
		HandlerList.unregisterAll(plugin);
		plugin.onEnable();
		Settings.setReloading(false);
		if (silent)
			Log.setSilent(false);
	}

	private void update(final CommandSender sender) {
		if (Settings.isUpdateCheck()) {
			if (Settings.isUpdate()) {
				if (plugin.getUpdateManager().getUpdater().downloadFile()) {
					sender.sendMessage("§2Update Successful. On next restart you will have §e" + Messages.getNewVersion());
				} else {
					sender.sendMessage("§4An error ocurred while updating, please report to the developer");
				}
			} else {
				sender.sendMessage("§2You have the latest version: §ePvPManager v" + Messages.getCurrentversion());
			}
		} else {
			sender.sendMessage("§4Update Checking is disabled, enable it in the Config file");
		}
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 1)
			return ChatUtils.getMatchingEntries(args[0], Lists.newArrayList("cleanup", "convert", "debug", "reload", "update", "locale", "worlds"));
		if (args.length == 2 && args[0].equalsIgnoreCase("convert"))
			return ChatUtils.getMatchingEntries(args[1], Lists.newArrayList("SQLITE", "MYSQL"));
		if (args.length == 2 && args[0].equalsIgnoreCase("debug"))
			return ChatUtils.getMatchingEntries(args[1], Lists.newArrayList("toggle", "damagedebug", "tag", "tagall", "attack", "players"));
		if (args.length == 2 && args[0].equalsIgnoreCase("locale"))
			return ChatUtils.getMatchingEntries(args[1], Locale.asStringList());

		return Collections.emptyList();
	}

}
