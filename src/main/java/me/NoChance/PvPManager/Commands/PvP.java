package me.NoChance.PvPManager.Commands;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CombatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvP implements CommandExecutor {

	private PlayerHandler ph;

	public PvP(PlayerHandler playerHandler) {
		this.ph = playerHandler;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			PvPlayer pvpPlayer = ph.get(player);
			if (args.length == 0) {
				if ((player.hasPermission("pvpmanager.pvpstatus.change") && !Variables.toggleSignsEnabled)
						|| ((player.hasPermission("pvpmanager.pvpstatus.change") && Variables.toggleSignsEnabled && !Variables.disableToggleCommand))) {
					pvpPlayer.togglePvP();
					return true;
				} else if (Variables.toggleSignsEnabled && Variables.disableToggleCommand) {
					player.sendMessage(Messages.Error_PvPCommand_Disabled);
					return false;
				}
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("status") && player.hasPermission("pvpmanager.pvpstatus.self")) {
					if (!pvpPlayer.hasPvPEnabled()) {
						player.sendMessage(Messages.Self_Status_Disabled);
						return true;
					} else {
						player.sendMessage(Messages.Self_Status_Enabled);
						return true;
					}
				}
				if (args[0].equalsIgnoreCase("list") && player.hasPermission("pvpmanager.list")) {
					player.sendMessage(ChatColor.GOLD + "**** Players With PvP Enabled ****");
					player.sendMessage(ChatColor.DARK_GRAY + pvpList());
					return true;
				}
				if (args[0].equalsIgnoreCase("info") && player.hasPermission("pvpmanager.debug")) {
					player.sendMessage(ChatColor.YELLOW + "§lPvPManager Info");
					player.sendMessage(ChatColor.GREEN + "- Name: §f" + pvpPlayer.getName());
					player.sendMessage(ChatColor.GREEN + "- PvP Status: §f" + pvpPlayer.hasPvPEnabled());
					player.sendMessage(ChatColor.GREEN + "- Tagged: §f" + pvpPlayer.isInCombat());
					player.sendMessage(ChatColor.GREEN + "- Newbie: §f" + pvpPlayer.isNewbie());
					player.sendMessage(ChatColor.GREEN + "- World: §f" + pvpPlayer.getWorldName());
					player.sendMessage(ChatColor.GREEN + "- Override: §f" + pvpPlayer.hasOverride());
					return true;
				}
				if ((player.hasPermission("pvpmanager.pvpstatus.change") && !Variables.toggleSignsEnabled)
						|| ((player.hasPermission("pvpmanager.pvpstatus.change") && Variables.toggleSignsEnabled && !Variables.disableToggleCommand))) {
					if (args[0].equalsIgnoreCase("off")) {
						if (CombatUtils.hasTimePassed(pvpPlayer.getToggleTime(), Variables.toggleCooldown)) {
							if (pvpPlayer.hasPvPEnabled()) {
								pvpPlayer.setPvP(false);
								return true;
							} else {
								player.sendMessage(Messages.Already_Disabled);
								return true;
							}
						}
						return false;
					} else if (args[0].equalsIgnoreCase("on")) {
						if (CombatUtils.hasTimePassed(pvpPlayer.getToggleTime(), Variables.toggleCooldown)) {
							if (!pvpPlayer.hasPvPEnabled()) {
								pvpPlayer.setPvP(true);
								return true;
							} else {
								player.sendMessage(Messages.Already_Enabled);
								return true;
							}
						}
						return false;
					} else if (player.hasPermission("pvpmanager.admin")) {
						if (!isOnline(args[0])) {
							player.sendMessage("§4Player not online!");
							return false;
						}
						PvPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(args[0]));
						if (specifiedPlayer.hasPvPEnabled()) {
							specifiedPlayer.setPvP(false);
							player.sendMessage("§6[§8PvPManager§6] §2PvP disabled for " + args[0]);
							return true;
						} else {
							specifiedPlayer.setPvP(true);
							player.sendMessage("§6[§8PvPManager§6] §4PvP enabled for " + args[0]);
							return true;
						}
					}
				} else if (Variables.toggleSignsEnabled && Variables.disableToggleCommand) {
					player.sendMessage(Messages.Error_PvPCommand_Disabled);
					return false;
				}
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("disable") && args[1].equalsIgnoreCase("protection")) {
					if (pvpPlayer.isNewbie()) {
						pvpPlayer.setNewbie(false);
						return true;
					} else {
						player.sendMessage(ChatColor.DARK_RED + "You are not protected!");
						return false;
					}
				}
				if (args[0].equalsIgnoreCase("status") && player.hasPermission("pvpmanager.pvpstatus.others")) {
					if (isOnline(args[1])) {
						PvPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(args[1]));
						if (!specifiedPlayer.hasPvPEnabled()) {
							player.sendMessage(Messages.Others_Status_Disabled.replace("%p", args[1]));
							return true;
						} else if (specifiedPlayer.hasPvPEnabled()) {
							player.sendMessage(Messages.Other_Status_Enabled.replace("%p", args[1]));
							return true;
						}
					} else {
						player.sendMessage(ChatColor.DARK_RED + args[1] + " Does Not Exist or is Offline");
						return true;
					}
				}
			}
		} else {
			if (args.length == 0) {
				sender.sendMessage("You are not a player!");
				return false;
			} else if (args.length == 1) {
				if (!isOnline(args[0])) {
					sender.sendMessage("§4Player not online!");
					return false;
				}
				PvPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(args[0]));
				if (specifiedPlayer.hasPvPEnabled()) {
					specifiedPlayer.setPvP(false);
					sender.sendMessage("§6[§8PvPManager§6] §2PvP disabled for " + args[0]);
					return true;
				} else {
					specifiedPlayer.setPvP(true);
					sender.sendMessage("§6[§8PvPManager§6] §4PvP enabled for " + args[0]);
					return true;
				}
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("status")) {
					if (!isOnline(args[1])) {
						sender.sendMessage("§4Player not online!");
						return false;
					}
					PvPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(args[1]));
					if (!specifiedPlayer.hasPvPEnabled()) {
						sender.sendMessage(Messages.Others_Status_Disabled.replace("%p", args[1]));
						return true;
					} else if (isOnline(args[1]) && specifiedPlayer.hasPvPEnabled()) {
						sender.sendMessage(Messages.Other_Status_Enabled.replace("%p", args[1]));
						return true;
					} else {
						sender.sendMessage(ChatColor.DARK_RED + args[1] + " Does Not Exist or is Offline");
						return true;
					}
				}
			}
		}
		sender.sendMessage(Messages.Error_Command);
		return false;
	}

	private String pvpList() {
		StringBuilder list = new StringBuilder();
		for (PvPlayer p : ph.getPlayers().values()) {
			if (p.hasPvPEnabled()) {
				list.append(p.getName() + ", ");
			}
		}
		if (list.toString().isEmpty())
			return "No Players with PvP Enabled";
		list.delete(list.length() - 2, list.length());
		return list.toString();
	}

	@SuppressWarnings("deprecation")
	private boolean isOnline(String name) {
		return Bukkit.getPlayer(name) != null;
	}
}
