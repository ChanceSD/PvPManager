package me.NoChance.PvPManager;

import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvPManager extends JavaPlugin {

	public HashSet<String> playersStatusOff = new HashSet<String>();

	@Override
	public void onEnable() {
		new DamageListener(this);
		new PlayerListener(this);
	}

	@Override
	public void onDisable() {

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (label.equalsIgnoreCase("pm") && sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 0) {
				player.sendMessage(ChatColor.GOLD + "===== PvPManager Help Page =====");
				player.sendMessage(ChatColor.DARK_AQUA + "/pm " + "| Shows This Help Page");
				player.sendMessage(ChatColor.DARK_AQUA + "/pm pvp <on/off> " + "| Sets PvP Enabled or Disabled.");
				player.sendMessage(ChatColor.DARK_AQUA + "/pm pvp status " + "| Checks if Your PvP is Enabled or Disabled.");
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("pvp")) {
					if (args[1].equalsIgnoreCase("off") && player.hasPermission("pvpmanager.pvpstatus.change")) {
						if(hasPvpEnabled(player.getName())){
							playersStatusOff.add(player.getName());
							player.sendMessage(ChatColor.GREEN + "PvP Disabled!");
						}
						else 
							player.sendMessage(ChatColor.DARK_RED + "You Already Have PvP Disabled!");
					}
					if (args[1].equalsIgnoreCase("on") && player.hasPermission("pvpmanager.pvpstatus.change")) {
						if(!hasPvpEnabled(player.getName())){
						playersStatusOff.remove(player.getName());
						player.sendMessage(ChatColor.RED + "PvP Enabled!");
						}
						else 
							player.sendMessage(ChatColor.DARK_RED + "You Already Have PvP Enabled!");
					}
					if (args[1].equalsIgnoreCase("status") && player.hasPermission("pvpmanager.pvpstatus.self")) {
						if (!hasPvpEnabled(player.getName()))
							player.sendMessage(ChatColor.AQUA + "You Have PvP Disabled");
						else 
							player.sendMessage(ChatColor.AQUA + "You Have PvP Enabled");	
					}
				}
			}
			if (args.length == 3) {
				if (args[0].equalsIgnoreCase("pvp")
						&& args[1].equalsIgnoreCase("status") && player.hasPermission("pvpmanager.pvpstatus.others"))
					if (!hasPvpEnabled(args[2]))
						player.sendMessage(ChatColor.AQUA + args[2] + " Has PvP disabled");
					else if (getServer().getPlayerExact(args[2]) != null && hasPvpEnabled(args[2]))
						player.sendMessage(ChatColor.AQUA + args[2] + " Has PvP enabled");
					else
						player.sendMessage(ChatColor.DARK_RED + args[2] + " Does Not Exist or is Offline");
			}
		}
		return false;
	}
	
	public boolean hasPvpEnabled(String name){
		for (String n : playersStatusOff){
			if (n.equalsIgnoreCase(name))
				return false;
		}
			return true;
	}
}
