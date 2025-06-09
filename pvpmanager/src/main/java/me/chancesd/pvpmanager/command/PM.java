package me.chancesd.pvpmanager.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import me.chancesd.sdutils.database.DatabaseConfigBuilder.DatabaseType;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.listener.DebugEntityListener;
import me.chancesd.pvpmanager.manager.UpdateManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Locale;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.storage.fields.UserDataFields;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.scheduler.ScheduleUtils;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;

public class PM extends BaseCommand {

	private final PvPManager plugin;
	private DebugEntityListener damageListener;
	private final Help helpCommand;

	public PM(final PluginCommand pluginCommand, final PvPManager plugin) {
		super(pluginCommand);
		this.plugin = plugin;

		// Configure main command
		this.description("PvPManager administration commands")
				.usage("/pmr [subcommand]")
				.permission(Permissions.COMMAND_MENU.getPermission()); // Add all subcommands
		subCommand("reload", new ReloadCommand());
		subCommand("update", new UpdateCommand());
		subCommand("cleanup", new CleanupCommand());
		subCommand("convert", new ConvertCommand());
		subCommand("debug", new DebugCommand());
		subCommand("locale", new LocaleCommand());
		subCommand("worlds", new Worlds(plugin));
		helpCommand = new Help();
		subCommand("menu", helpCommand);
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		// If no arguments provided, show help menu
		if (args.isEmpty()) {
			helpCommand.helpMenu(sender, 1);
			return;
		}

		// This should never be reached since subcommands handle everything
		sender.sendMessage(Lang.ERROR_COMMAND.msg());
	}

	// Subcommand classes
	private class ReloadCommand extends BaseCommand {
		public ReloadCommand() {
			this.displayName("Reload").description("Reload PvPManager")
					.usage("/pmr reload")
					.permission(Permissions.COMMAND_RELOAD.getPermission());
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			reload(false);
			sender.sendMessage(Lang.PREFIX + " §aPvPManager reloaded!");
		}
	}

	private class UpdateCommand extends BaseCommand {

		public UpdateCommand() {
			this.displayName("Update").description("Update to latest version")
					.usage("/pmr update")
					.permission(Permissions.COMMAND_MENU.getPermission());
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
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
	}

	private class CleanupCommand extends BaseCommand {
		public CleanupCommand() {
			this.displayName("Cleanup").description("Cleanup inactive users")
					.usage("/pmr cleanup <days>")
					.permission(Permissions.COMMAND_MENU.getPermission())
					.argument("days", ArgumentType.INTEGER).required().endArgument();
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			if (args.isEmpty()) {
				sender.sendMessage("§4§lUsage: §f/pmr cleanup <days>");
				sender.sendMessage("§cThis command will remove users from the database that haven't logged in during the last x days.");
				sender.sendMessage(
						"§cThis means that if they come back their remaining newbie protection(if they had any) will be gone and their previous PvP state will also be default.");
				return;
			}

			try {
				final int daysValue = Integer.parseInt(args.get(0).getValue());
				final long days = TimeUnit.DAYS.toMillis(daysValue);
				sender.sendMessage("§2Cleaning up users that haven't logged in the past " + daysValue + " days");
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
						sender.sendMessage(Lang.PREFIX + " §2Finished. Cleaned up " + ids.size() + " inactive users.");
					}
				}.runTaskAsynchronously(plugin);
			} catch (final NumberFormatException e) {
				sender.sendMessage("§cError, days must be a number!");
			}
		}
	}

	private class ConvertCommand extends BaseCommand {
		public ConvertCommand() {
			this.displayName("Convert").description("Convert database (SQLITE/MYSQL)")
					.usage("/pmr convert <databaseType>")
					.permission(Permissions.COMMAND_MENU.getPermission())
					.argument("databaseType", ArgumentType.STRING).required().tabComplete("SQLITE", "MYSQL").endArgument();
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			if (args.isEmpty()) {
				sender.sendMessage(Lang.PREFIX + " §4§lUsage: §e/pmr convert <databaseType>");
				sender.sendMessage(Lang.PREFIX + " §cCurrently the database types are: " + Arrays.asList(DatabaseType.values()));
				return;
			}

			final String dbType = args.get(0).getValue();
			final DatabaseType databaseType;
			try {
				databaseType = DatabaseType.valueOf(dbType.toUpperCase());
			} catch (final IllegalArgumentException e) {
				sender.sendMessage(Lang.PREFIX + " §cInvalid database type. Available types are: " + Arrays.asList(DatabaseType.values()));
				return;
			}

			final DatabaseType currentType = plugin.getStorageManager().getStorage().getDatabaseType();
			if (currentType == databaseType) {
				sender.sendMessage(Lang.PREFIX + " §cCan't convert. You are already running on " + databaseType);
				return;
			}

			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				sender.sendMessage("§2Starting database conversion from " + currentType + " to " + databaseType);
				try {
					plugin.getStorageManager().convertFromCurrent(databaseType, sender, System.currentTimeMillis());
				} catch (final Exception e) {
					sender.sendMessage(Lang.PREFIX + " §cError! Make sure you entered the correct MySQL details in the config");
					return;
				}
				plugin.getConfig().set("Database.Type", databaseType.toString());
				plugin.saveConfig();
				reload(false);
				sender.sendMessage(Lang.PREFIX + " §aYou are now running on " + plugin.getStorageManager().getStorage().getDatabaseType());
			});
		}
	}

	private class DebugCommand extends BaseCommand {
		public DebugCommand() {
			this.displayName("Debug").description("Debug utilities")
					.usage("/pmr debug <subcommand> [player]")
					.permission(Permissions.COMMAND_MENU.getPermission())
					.argument("subcommand", ArgumentType.STRING).required().tabComplete("toggle", "damagedebug", "tag", "tagall", "attack", "players")
					.endArgument()
					.argument("player", ArgumentType.PLAYER).endArgument();
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			// Check if we have enough arguments before proceeding
			if (args.isEmpty()) {
				sender.sendMessage("§4§lUsage: §f/pmr debug <subcommand>");
				sender.sendMessage("§cAvailable subcommands: toggle, damagedebug, attack, players");
				return;
			}

			final String subcommand = args.get(0).getValue().toLowerCase();
			final CombatPlayer targetPlayer = getTargetPlayer(sender, args);
			switch (subcommand) {
			case "toggle":
				toggleDebugMode(sender);
				break;
			case "damagedebug":
				toggleDamageDebug(sender);
				break;
			case "players":
				listPlayers(sender);
				break;
			case "tag":
				if (targetPlayer != null) {
					targetPlayer.tag(true, targetPlayer);
					sender.sendMessage("Tagged player: " + targetPlayer.getName());
				} else {
					sender.sendMessage("§cPlayer not specified or not online.");
				}
				break;
			case "tagall":
				for (final CombatPlayer player : plugin.getPlayerManager().getPlayers().values()) {
					player.tag(true, player);
				}
				sender.sendMessage("Tagged all players");
				break;
			case "attack":
				if (targetPlayer != null) {
					attackPlayer(sender, targetPlayer);
				} else {
					sender.sendMessage("§cPlayer not specified or not online.");
				}
				break;
			default:
				sender.sendMessage("§cInvalid subcommand. Use /pmr debug <toggle|damagedebug|tag|tagall|players|attack> [player]");
				break;
			}
		}

		private CombatPlayer getTargetPlayer(final CommandSender sender, final List<CommandArgument> args) {
			if (args.size() >= 2) {
				final String playerName = args.get(1).getValue();
				if (!CombatUtils.isOnline(playerName)) {
					sender.sendMessage("§4Player not online!");
					return null;
				}
				return plugin.getPlayerManager().get(Bukkit.getPlayer(playerName));
			} else if (sender instanceof final Player player) {
				return plugin.getPlayerManager().get(player);
			}
			return null;
		}

		private void toggleDebugMode(final CommandSender sender) {
			Conf.setDebug(!Conf.DEBUG_MODE.asBool());
			Log.info("Debug mode: " + Conf.DEBUG_MODE.asBool());
			sender.sendMessage("Debug mode: " + Conf.DEBUG_MODE.asBool());
		}

		private void toggleDamageDebug(final CommandSender sender) {
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
		}

		private void listPlayers(final CommandSender sender) {
			for (final CombatPlayer player : plugin.getPlayerManager().getPlayers().values()) {
				if (!Bukkit.getOnlinePlayers().contains(player.getPlayer())) {
					sender.sendMessage("UUID: " + player.getUUID() + " - Name: " + player.getName() + " - Metadata: " + player.getPlayer().hasMetadata("NPC"));
					Log.info("UUID: " + player.getUUID() + " - Name: " + player.getName() + " - Metadata: " + player.getPlayer().hasMetadata("NPC"));
				}
			}
			sender.sendMessage("§aPlayers: §c" + plugin.getPlayerManager().getPlayers().size() + "§a/" + Bukkit.getOnlinePlayers().size());
			Log.info("Players: " + plugin.getPlayerManager().getPlayers().size() + "/" + Bukkit.getOnlinePlayers().size());
		}

		private void attackPlayer(final CommandSender sender, final CombatPlayer targetPlayer) {
			// Simulate attacking a player for debugging purposes
			targetPlayer.getPlayer().damage(5.0);
			sender.sendMessage("Attacked player with 5 damage");
		}
	}

	private class LocaleCommand extends BaseCommand {
		public LocaleCommand() {
			this.displayName("Locale").description("Change plugin language")
					.usage("/pmr locale [language]")
					.permission(Permissions.COMMAND_MENU.getPermission())
					.argument("language", ArgumentType.STRING).tabComplete(Locale.asStringList()).endArgument();
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			if (args.isEmpty()) {
				sender.sendMessage(Lang.PREFIX + " §aYour current Locale is: §c" + Lang.getLocale());
				sender.sendMessage(Lang.PREFIX + " §aAvailable languages are: §c" + Locale.asStringList());
				return;
			}

			Locale locale;
			try {
				locale = Locale.valueOf(args.get(0).getValue().toUpperCase());
			} catch (final IllegalArgumentException e) {
				sender.sendMessage(Lang.PREFIX + " §cInvalid Locale. Available languages are: " + Locale.asStringList());
				return;
			}
			if (Lang.getLocale() == locale) {
				sender.sendMessage(Lang.PREFIX + " §cCan't change Locale. You are already using " + locale);
				return;
			}

			Conf.LOCALE.set(locale.name());
			changeConfigSetting("General.Locale", locale.name());
			Lang.setup(plugin);
			sender.sendMessage(Lang.PREFIX + " §aLanguage changed to " + Lang.getLocale() + " - Filename: " + Lang.getLocale().fileName());
		}

		private void changeConfigSetting(final String path, final String value) {
			plugin.reloadConfig();
			plugin.getConfig().set(path, value);
			plugin.saveConfig();
		}
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
}
