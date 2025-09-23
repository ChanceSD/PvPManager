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
import me.chancesd.sdutils.scheduler.ScheduleUtils;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;
import me.chancesd.sdutils.utils.ChatUtils;

public class PM extends BaseCommand {

	private final PvPManager plugin;
	private DebugEntityListener damageListener;
	private final Help helpCommand;

	public PM(final PluginCommand pluginCommand, final PvPManager plugin) {
		super(pluginCommand);
		this.plugin = plugin;

		// Configure main command
		this.description("PvPManager administration commands")
				.usage("/pmr [subcommand]").argument("page", ArgumentType.INTEGER).endArgument();
		subCommand("reload", new ReloadCommand());
		subCommand("update", new UpdateCommand());
		subCommand("cleanup", new CleanupCommand());
		subCommand("convert", new ConvertCommand());
		subCommand("debug", new DebugCommand());
		subCommand("locale", new LocaleCommand());
		subCommand("worlds", new Worlds(plugin));
		helpCommand = new Help();
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		if (args.isEmpty()) {
			helpCommand.helpMenu(sender, 1);
		} else if (args.size() == 1) {
			final int page = getArgument(args, "page").getAsInt();
			if (page > 0) {
				helpCommand.helpMenu(sender, page);
			}
		}
	}

	// Subcommand classes
	private class ReloadCommand extends BaseCommand {
		public ReloadCommand() {
			this.displayName("Reload").description("Reload PvPManager")
					.usage("/pmr reload")
					.permission(Permissions.ADMIN.getPermission());
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			reload(false);
			sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " &#55FF55PvPManager reloaded!"));
		}
	}

	private class UpdateCommand extends BaseCommand {

		public UpdateCommand() {
			this.displayName("Update").description("Update to latest version")
					.usage("/pmr update")
					.permission(Permissions.ADMIN.getPermission());
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			if (Conf.CHECK_UPDATES.asBool()) {
				final UpdateManager updateManager = plugin.getUpdateManager();
				if (updateManager.hasUpdateAvailable()) {
					if (updateManager.getUpdater().downloadFile()) {
						sender.sendMessage(
								ChatUtils.colorize("&#00AA00Update Successful. On next restart you will have &#FFFF55" + updateManager.getNewVersion()));
					} else {
						sender.sendMessage(ChatUtils.colorize("&#FF5555An error ocurred while updating, please report to the developer"));
					}
				} else {
					sender.sendMessage(ChatUtils.colorize("&#00AA00You have the latest version: &#FFFF55PvPManager v" + updateManager.getCurrentversion()));
				}
			} else {
				sender.sendMessage(ChatUtils.colorize("&#FF5555Update Checking is disabled, enable it in the Config file"));
			}
		}
	}

	private class CleanupCommand extends BaseCommand {
		public CleanupCommand() {
			this.displayName("Cleanup").description("Cleanup inactive users")
					.usage("/pmr cleanup <days>")
					.permission(Permissions.ADMIN.getPermission())
					.argument("days", ArgumentType.INTEGER).endArgument();
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			if (args.isEmpty()) {
				sender.sendMessage(ChatUtils.colorize("&#FF5555Usage: &#FFFFFF/pmr cleanup <days>"));
				sender.sendMessage(ChatUtils
						.colorize("&#999999This command will remove users from the database that haven't logged in during the last &#FFFF55x &#999999days."));
				sender.sendMessage(ChatUtils.colorize(
						"&cThis means that if they come back their remaining newbie protection(if they had any) and their previous PvP state will be default."));
				return;
			}

			final int daysValue = getArgument(args, "days").getAsInt();
			final long days = TimeUnit.DAYS.toMillis(daysValue);
			sender.sendMessage(ChatUtils.colorize("&#999999Cleaning users that haven't logged in the past &#FFFF55" + daysValue + " &#999999days"));
			sender.sendMessage(ChatUtils.colorize("&#999999This might take a while depending on the size of your database"));

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
					sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " &#55FF55Finished. Cleaned up " + ids.size() + " inactive users."));
				}
			}.runTaskAsynchronously(plugin);
		}
	}

	private class ConvertCommand extends BaseCommand {
		public ConvertCommand() {
			this.displayName("Convert").description("Convert database (SQLITE/MYSQL)")
					.usage("/pmr convert <databaseType>")
					.permission(Permissions.ADMIN.getPermission())
					.argument("databaseType", ArgumentType.STRING).tabComplete("SQLITE", "MYSQL").endArgument();
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			if (args.isEmpty()) {
				sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " &#FF5555&lUsage: &e/pmr convert <databaseType>"));
				sender.sendMessage(
						ChatUtils.colorize(Lang.PREFIX + " &cCurrently the database types are: &7" + Arrays.asList(DatabaseType.values())));
				return;
			}

			final String dbType = args.get(0).getValue();
			final DatabaseType databaseType;
			try {
				databaseType = DatabaseType.valueOf(dbType.toUpperCase());
			} catch (final IllegalArgumentException e) {
				sender.sendMessage(
						ChatUtils.colorize(Lang.PREFIX + " &#FF5555Invalid database type. Available types are: &7" + Arrays.asList(DatabaseType.values())));
				return;
			}

			final DatabaseType currentType = plugin.getStorageManager().getStorage().getDatabaseType();
			if (currentType == databaseType) {
				sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " &#FF5555Can't convert. You are already running on " + databaseType));
				return;
			}

			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				sender.sendMessage(ChatUtils.colorize("&#00AA00Starting database conversion from " + currentType + " to " + databaseType));
				try {
					plugin.getStorageManager().convertFromCurrent(databaseType, sender, System.currentTimeMillis());
				} catch (final Exception e) {
					sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " &#FF5555Error! Make sure you entered the correct MySQL details in the config"));
					return;
				}
				plugin.getConfig().set("Database.Type", databaseType.toString());
				plugin.saveConfig();
				reload(false);
				sender.sendMessage(
						ChatUtils.colorize(Lang.PREFIX + " &#00AA00You are now running on " + plugin.getStorageManager().getStorage().getDatabaseType()));
			});
		}
	}

	private class DebugCommand extends BaseCommand {
		public DebugCommand() {
			this.displayName("Debug").description("Debug utilities")
					.usage("/pmr debug <subcommand> [player]")
					.permission(Permissions.ADMIN.getPermission())
					.argument("subcommand", ArgumentType.STRING).tabComplete("toggle", "damagedebug", "attack", "players")
					.endArgument()
					.argument("player", ArgumentType.PLAYER).endArgument();
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			// Check if we have enough arguments before proceeding
			if (args.isEmpty()) {
				sender.sendMessage(ChatUtils.colorize("&#FF5555&lUsage: &#FFFFFF/pmr debug <subcommand>"));
				sender.sendMessage(ChatUtils.colorize("&cAvailable subcommands: &7toggle, damagedebug, attack, players"));
				return;
			}

			final String subcommand = getArgument(args, "subcommand").getValue().toLowerCase();
			final CommandArgument argument = getArgument(args, "player");
			CombatPlayer targetPlayer = null;
			if (argument != null) {
				targetPlayer = plugin.getPlayerManager().get(argument.getAsPlayerOrNull());
			} else if (sender instanceof final Player commandSender) {
				targetPlayer = plugin.getPlayerManager().get(commandSender);
			}
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
			case "attack":
				if (targetPlayer != null) {
					attackPlayer(sender, targetPlayer);
				} else {
					ChatUtils.send(sender, "&#FF5555Player not specified");
				}
				break;
			default:
				ChatUtils.send(sender, "&#FF5555Invalid subcommand. Use &7/pmr debug <toggle|damagedebug|players|attack> [player]");
				break;
			}
		}

		private void toggleDebugMode(final CommandSender sender) {
			Conf.setDebug(!Conf.DEBUG_MODE.asBool());
			Log.info("Debug mode: " + Conf.DEBUG_MODE.asBool());
			ChatUtils.send(sender, "Debug mode: " + Conf.DEBUG_MODE.asBool());
		}

		private void toggleDamageDebug(final CommandSender sender) {
			if (damageListener == null) {
				ChatUtils.send(sender, "&#AA0000Warning &#FFFFFF- Some plugin features are disabled while in this mode");
				ChatUtils.send(sender, "Enabling a damage listener for debugging, check the console for details on every entity hit");
				ChatUtils.send(sender, "&#FF5555Run this command again &#FFFFFFafter you are done to disable debugging or reload the plugin");
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
					ChatUtils.send(sender,
							"UUID: " + player.getUUID() + " - Name: " + player.getName() + " - Metadata: " + player.getPlayer().hasMetadata("NPC"));
					Log.info("UUID: " + player.getUUID() + " - Name: " + player.getName() + " - Metadata: " + player.getPlayer().hasMetadata("NPC"));
				}
			}
			ChatUtils.send(sender,
					"&aPlayers: &c" + plugin.getPlayerManager().getPlayers().size() + "&a/" + Bukkit.getOnlinePlayers().size());
			Log.info("Players: " + plugin.getPlayerManager().getPlayers().size() + "/" + Bukkit.getOnlinePlayers().size());
		}

		private void attackPlayer(final CommandSender sender, final CombatPlayer targetPlayer) {
			// Simulate attacking a player for debugging purposes
			targetPlayer.getPlayer().damage(5.0);
			ChatUtils.send(sender, "Attacked player with 5 damage");
		}
	}

	private class LocaleCommand extends BaseCommand {
		public LocaleCommand() {
			this.displayName("Locale").description("Change plugin language")
					.usage("/pmr locale [language]")
					.permission(Permissions.ADMIN.getPermission())
					.argument("language", ArgumentType.STRING).tabComplete(Locale.asStringList()).endArgument();
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			if (args.isEmpty()) {
				ChatUtils.send(sender, Lang.PREFIX + " &7Your current Locale is: &#FF5555" + Lang.getLocale());
				ChatUtils.send(sender, Lang.PREFIX + " &7Available languages are: &#FF5555" + Locale.asStringList());
				return;
			}

			Locale locale;
			try {
				locale = Locale.valueOf(args.get(0).getValue().toUpperCase());
			} catch (final IllegalArgumentException e) {
				ChatUtils.send(sender, Lang.PREFIX + " &#FF5555Invalid Locale. &7Available languages are: &e" + Locale.asStringList());
				return;
			}
			if (Lang.getLocale() == locale) {
				ChatUtils.send(sender, Lang.PREFIX + " &#FF5555Can't change Locale. You are already using " + locale);
				return;
			}

			Conf.LOCALE.set(locale.name());
			changeConfigSetting("General.Locale", locale.name());
			Lang.setup(plugin);
			ChatUtils.send(sender, Lang.PREFIX + " &aLanguage changed to &e" + Lang.getLocale() + "&a - &7Filename: " + Lang.getLocale().fileName());
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
