package me.NoChance.PvPManager.Commands;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class PM implements CommandExecutor {

	private PvPManager plugin;

	public PM(PvPManager plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 0 && player.hasPermission("pvpmanager.menu")) {
				Variables.helpMenu(player);
				return true;
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload") && player.hasPermission("pvpmanager.reload")) {
					reload(player);
					return true;
				} else if (args[0].equalsIgnoreCase("reload")) {
					player.sendMessage(Messages.Error_Permission);
					return false;
				}
				if (args[0].equalsIgnoreCase("update") && player.hasPermission("pvpmanager.admin")) {
					if (Variables.updateCheck) {
						if (Variables.update) {
							if (plugin.downloadUpdate())
								player.sendMessage("§2Update Successful. On next restart you will have §e" + Messages.newVersion);
							else
								player.sendMessage("§4An error ocurred while updating, please report to the developer");
						} else
							player.sendMessage("§2You have the latest version: §ePvPManager v" + Messages.currentVersion);
					} else
						player.sendMessage("§4Update Checking is disabled, enable it in the Config file");
					return true;
				}
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("debug") && player.hasPermission("pvpmanager.debug")) {
					PvPlayer p = plugin.getPlayerHandler().get(player);
					if (args[1].equalsIgnoreCase("tag")) {
						p.setTagged(true, "Debug");
					} else if (args[1].equalsIgnoreCase("ct")) {
						p.message("Tagged: " + p.isInCombat());
					} else if (args[1].equalsIgnoreCase("newbie")) {
						p.setNewbie(true);
					} else if (args[1].equalsIgnoreCase("attack")) {
						plugin.getServer().getPluginManager()
								.callEvent(new EntityDamageByEntityEvent(player, player, DamageCause.ENTITY_ATTACK, 5.0));
					}
					return true;
				}
			}
		} else {
			if (args.length == 0) {
				sender.sendMessage("[PvPManager] You are not a player, available commands:");
				sender.sendMessage("pm reload");
				sender.sendMessage("pm pvpstart <time> <world>");
				sender.sendMessage("pm pvpend <time> <world>");
				sender.sendMessage("pvp status <player>");
				return true;
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					reload(sender);
					return true;
				}
			}
			return false;
		}
		sender.sendMessage(Messages.Error_Command);
		return false;
	}

	public void reload(CommandSender player) {
		Variables.update = false;
		plugin.getServer().getScheduler().cancelTasks(plugin);
		plugin.getServer().getPluginManager().disablePlugin(plugin);
		plugin.getServer().getPluginManager().enablePlugin(plugin);
		player.sendMessage("§2PvPManager Reloaded!");
	}

}
