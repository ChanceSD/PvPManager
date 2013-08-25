package me.NoChance.PvPManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;

public final class PvPManager extends JavaPlugin {

	public HashSet<String> playersStatusOff = new HashSet<String>();
	public HashSet<String> inCombat = new HashSet<String>();
	private ConfigManager configM;
	public Logger logger = Logger.getLogger("Minecraft");

	@Override
	public void onEnable() {
		saveDefaultConfig();
		reloadConfig();
		configM = new ConfigManager(this);
		configM.load();
		configM.loadUsers();
		new Variables(this);
		if (Variables.stopCommands) {
			new CommandListener(this);
		}
		new DamageListener(this);
		new PlayerListener(this);
		initMetrics();
	}

	@Override
	public void onDisable() {
		configM.saveUsers();
		configM.save();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (label.equalsIgnoreCase("pm") && sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 0) {
				player.sendMessage(ChatColor.GOLD
						+ "===== PvPManager Help Page =====");
				player.sendMessage(ChatColor.DARK_AQUA + "/pm "
						+ "| Shows This Help Page");
				player.sendMessage(ChatColor.DARK_AQUA + "/pm reload"
						+ "| Reloads PvPManager");
				player.sendMessage(ChatColor.DARK_AQUA + "/pm pvp <on/off> "
						+ "| Sets PvP Enabled or Disabled.");
				player.sendMessage(ChatColor.DARK_AQUA + "/pm pvp status "
						+ "| Checks if Your PvP is Enabled or Disabled.");
				return true;
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")
						&& player.hasPermission("pvpmanager.reload")) {
					reload(player);
					return true;
				} else if (args[0].equalsIgnoreCase("reload")) {
					player.sendMessage(ChatColor.DARK_RED
							+ "You don't have permission!");
					return false;
				}
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("pvp")) {
					if (args[1].equalsIgnoreCase("off")
							&& player
									.hasPermission("pvpmanager.pvpstatus.change")) {
						if (hasPvpEnabled(player.getName())) {
							playersStatusOff.add(player.getName());
							player.sendMessage(ChatColor.GREEN
									+ "PvP Disabled!");
							return true;
						} else {
							player.sendMessage(ChatColor.DARK_RED
									+ "You Already Have PvP Disabled!");
							return true;
						}
					}
					if (args[1].equalsIgnoreCase("on")
							&& player
									.hasPermission("pvpmanager.pvpstatus.change")) {
						if (!hasPvpEnabled(player.getName())) {
							playersStatusOff.remove(player.getName());
							player.sendMessage(ChatColor.RED + "PvP Enabled!");
							return true;
						} else {
							player.sendMessage(ChatColor.DARK_RED
									+ "You Already Have PvP Enabled!");
							return true;
						}
					}
					if (args[1].equalsIgnoreCase("status")
							&& player
									.hasPermission("pvpmanager.pvpstatus.self")) {
						if (!hasPvpEnabled(player.getName())) {
							player.sendMessage(ChatColor.AQUA
									+ "You Have PvP Disabled");
							return true;
						} else {
							player.sendMessage(ChatColor.AQUA
									+ "You Have PvP Enabled");
							return true;
						}
					}
				}
				return false;
			}
			if (args.length == 3) {
				if (args[0].equalsIgnoreCase("pvp")
						&& args[1].equalsIgnoreCase("status")
						&& player.hasPermission("pvpmanager.pvpstatus.others")) {
					if (!hasPvpEnabled(args[2])) {
						player.sendMessage(ChatColor.AQUA + args[2]
								+ " Has PvP disabled");
						return true;
					} else if (getServer().getPlayerExact(args[2]) != null
							&& hasPvpEnabled(args[2])) {
						player.sendMessage(ChatColor.AQUA + args[2]
								+ " Has PvP enabled");
						return true;
					} else {
						player.sendMessage(ChatColor.DARK_RED + args[2]
								+ " Does Not Exist or is Offline");
						return true;
					}
				}
			} else {
				return false;
			}
		}

		return false;
	}

	private void reload(Player player) {
		Bukkit.getServer().getPluginManager().disablePlugin(this);
		Bukkit.getServer().getPluginManager().enablePlugin(this);
		player.sendMessage("PvPManager Reloaded!");
	}

	private void initMetrics() {
		try {
			Metrics metrics = new Metrics(this);
			Graph keepItemsExp = metrics
					.createGraph("Percentage of Keep and Drop");
			keepItemsExp.addPlotter(new Metrics.Plotter("Keep Everything") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.keepItems && Variables.keepExp)
						i++;
					
					return i;
				}
			});
			keepItemsExp.addPlotter(new Metrics.Plotter("Drop Everything") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.keepItems && !Variables.keepExp)
						i++;
					
					return i;
				}
			});
			keepItemsExp.addPlotter(new Metrics.Plotter(
					"Keep Items and Drop Exp") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.keepItems && !Variables.keepExp)
						i++;
					
					return i;
				}
			});
			keepItemsExp.addPlotter(new Metrics.Plotter(
					"Keep Exp and Drop Items") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.keepItems && Variables.keepExp)
						i++;
					
					return i;
				}
			});	
			metrics.start();
		} catch (IOException e) {
		}
	}

	public boolean hasPvpEnabled(String name) {
		for (String n : playersStatusOff) {
			if (n.equalsIgnoreCase(name))
				return false;
		}
		return true;
	}

}