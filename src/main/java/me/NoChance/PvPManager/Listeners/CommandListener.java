package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPTimer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class CommandListener implements Listener {

	private PvPManager plugin;

	public CommandListener(PvPManager plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (Variables.stopCommands && Variables.inCombatEnabled) {
			if (plugin.inCombat.contains(event.getPlayer().getName())) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(Messages.Command_Denied_InCombat);
			}
		}
		if (Variables.pvpTimerEnabled) {
			String[] cmd = event.getMessage().split(" ");
			if (cmd[0].equalsIgnoreCase("/time")) {
				for (PvPTimer a : plugin.schedulers) {
					if (event.getPlayer().getWorld().equals(a.w)) {
						a.checkWorldPvP();
					}
				}
			}
		}

	}

	@EventHandler
	public void onConsoleCommand(ServerCommandEvent event) {
		if (Variables.pvpTimerEnabled) {
			String[] cmd = event.getCommand().split(" ");
			if (cmd[0].equalsIgnoreCase("time")) {
				for (PvPTimer a : plugin.schedulers) {
					if (a.w.equals(plugin.getServer().getWorld("world"))) {
						a.checkWorldPvP();
					}
				}
				if (cmd.length == 3) {
					for (PvPTimer a : plugin.schedulers) {
						if (cmd[3].equals(a.w.getName())) {
							a.checkWorldPvP();
						}
					}
				}
			}
		}
	}

}
