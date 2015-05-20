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

	private final PvPManager plugin;

	public PM(final PvPManager plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			if (args.length == 0 && player.hasPermission("pvpmanager.menu")) {
				Variables.helpMenu(player);
				return true;
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload") && player.hasPermission("pvpmanager.reload")) {
					reload(player);
					return true;
				} else if (args[0].equalsIgnoreCase("reload")) {
					player.sendMessage(Messages.getErrorPermission());
					return false;
				}
				if (args[0].equalsIgnoreCase("update") && player.hasPermission("pvpmanager.admin")) {
					if (Variables.isUpdateCheck()) {
						if (Variables.isUpdate()) {
							if (plugin.downloadUpdate())
								player.sendMessage("§2Update Successful. On next restart you will have §e" + Messages.getNewversion());
							else
								player.sendMessage("§4An error ocurred while updating, please report to the developer");
						} else
							player.sendMessage("§2You have the latest version: §ePvPManager v" + Messages.getCurrentversion());
					} else
						player.sendMessage("§4Update Checking is disabled, enable it in the Config file");
					return true;
				}
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("debug") && player.hasPermission("pvpmanager.debug")) {
					final PvPlayer p = plugin.getPlayerHandler().get(player);
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
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					reload(sender);
					return true;
				}
			}
			return false;
		}
		sender.sendMessage(Messages.getErrorCommand());
		return false;
	}

	public final void reload(final CommandSender player) {
		Variables.setUpdate(false);
		plugin.getServer().getScheduler().cancelTasks(plugin);
		plugin.getServer().getPluginManager().disablePlugin(plugin);
		plugin.getServer().getPluginManager().enablePlugin(plugin);
		player.sendMessage("§2PvPManager Reloaded!");
	}

}
