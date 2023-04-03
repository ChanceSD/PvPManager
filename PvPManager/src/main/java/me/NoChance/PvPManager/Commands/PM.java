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
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.ChatUtils;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Log;
import me.chancesd.pvpmanager.storage.DatabaseConfigBuilder.DatabaseType;
import me.chancesd.pvpmanager.storage.fields.UserDataFields;

public class PM implements TabExecutor {

	private final PvPManager plugin;

	public PM(final PvPManager plugin) {
		this.plugin = plugin;
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
			if (args[0].equalsIgnoreCase("update") && sender.hasPermission("pvpmanager.admin")) {
				update(sender);
				return true;
			}
		}
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("cleanup") && sender.hasPermission("pvpmanager.admin")) {
				cleanup(sender, args);
				return true;
			} else if (args[0].equalsIgnoreCase("convert") && sender.hasPermission("pvpmanager.admin")) {
				convert(sender, args);
				return true;
			} else if (args[0].equalsIgnoreCase("debug") && sender.hasPermission("pvpmanager.debug")) {
				debug(sender, args);
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
				plugin.getStorageManager().convertFromCurrent(databaseType, sender instanceof Player ? sender : null, System.currentTimeMillis());
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
		} else if (args.length == 2 && args[1].equalsIgnoreCase("toggle")) {
			Settings.setDEBUG(!Settings.DEBUG);
			Log.info("Debug mode: " + Settings.DEBUG);
		} else if (args.length == 3) {
			if (!CombatUtils.isOnline(args[2])) {
				sender.sendMessage("§4Player not online!");
				return;
			}
			p = plugin.getPlayerHandler().get(Bukkit.getPlayer(args[2]));
		}
		if (p == null)
			return;
		final PermissionAttachment attachment = sender.addAttachment(plugin, 1200);
		if (attachment != null) {
			attachment.setPermission("pvpmanager.nocombattag", false);
		}
		if (args[1].equalsIgnoreCase("tag")) {
			p.setTagged(true, p);
		} else if (args[1].equalsIgnoreCase("ct")) {
			p.message("Tagged: " + p.isInCombat());
		} else if (args[1].equalsIgnoreCase("newbie")) {
			p.setNewbie(true);
		} else if (args[1].equalsIgnoreCase("attack")) {
			plugin.getServer().getPluginManager().callEvent(new EntityDamageByEntityEvent(p.getPlayer(), p.getPlayer(), DamageCause.ENTITY_ATTACK, 5.0));
		}
	}

	private void reload(final CommandSender sender) {
		if (!sender.hasPermission("pvpmanager.reload")) {
			sender.sendMessage(Messages.getErrorPermission());
			return;
		}

		Settings.setReloading(true);
		Settings.setUpdate(false);
		Bukkit.getScheduler().cancelTasks(plugin);
		HandlerList.unregisterAll(plugin);
		plugin.onDisable();
		plugin.onEnable();
		Settings.setReloading(false);
		sender.sendMessage(Messages.PREFIXMSG + " §aPvPManager reloaded!");
	}

	private void update(final CommandSender sender) {
		if (Settings.isUpdateCheck()) {
			if (Settings.isUpdate()) {
				if (plugin.getUpdater().downloadFile()) {
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
			return ChatUtils.getMatchingEntries(args[0], Lists.newArrayList("cleanup", "convert", "reload", "update"));
		if (args.length == 2 && args[0].equalsIgnoreCase("convert"))
			return ChatUtils.getMatchingEntries(args[1], Lists.newArrayList("SQLITE", "MYSQL"));

		return Collections.emptyList();
	}

}
