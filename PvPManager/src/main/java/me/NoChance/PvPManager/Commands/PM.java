package me.NoChance.PvPManager.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.permissions.PermissionAttachment;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class PM implements CommandExecutor {

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
				if (sender.hasPermission("pvpmanager.reload")) {
					reload(sender);
					return true;
				}
				sender.sendMessage(Messages.getErrorPermission());
				return true;
			}
			if (args[0].equalsIgnoreCase("update") && sender.hasPermission("pvpmanager.admin")) {
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
				return true;
			}
			sender.sendMessage(Messages.getErrorPermission());
			return true;
		} else if (args.length > 1 && args[0].equalsIgnoreCase("debug") && sender.hasPermission("pvpmanager.debug")) {
			PvPlayer p = null;
			if (args.length == 2 && sender instanceof Player) {
				p = plugin.getPlayerHandler().get((Player) sender);
			} else if (args.length == 3) {
				if (!CombatUtils.isOnline(args[2])) {
					sender.sendMessage("§4Player not online!");
					return true;
				}
				p = plugin.getPlayerHandler().get(Bukkit.getPlayer(args[2]));
			}
			if (p == null)
				return true;
			final PermissionAttachment attachment = sender.addAttachment(plugin, 1200);
			attachment.setPermission("pvpmanager.nocombat", false);
			if (args[1].equalsIgnoreCase("tag")) {
				p.setTagged(true, p);
			} else if (args[1].equalsIgnoreCase("ct")) {
				p.message("Tagged: " + p.isInCombat());
			} else if (args[1].equalsIgnoreCase("newbie")) {
				p.setNewbie(true);
			} else if (args[1].equalsIgnoreCase("attack")) {
				plugin.getServer().getPluginManager().callEvent(new EntityDamageByEntityEvent(p.getPlayer(), p.getPlayer(), DamageCause.ENTITY_ATTACK, 5.0));
			}
			return true;
		}
		sender.sendMessage(Messages.getErrorCommand());
		return false;
	}

	private void reload(final CommandSender player) {
		Settings.setUpdate(false);
		Bukkit.getScheduler().cancelTasks(plugin);
		HandlerList.unregisterAll(plugin);
		plugin.onDisable();
		plugin.onEnable();
		player.sendMessage("§2PvPManager Reloaded!");
	}

}
