package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class CommandListener implements Listener {

	private PvPManager plugin;

	public CommandListener(PvPManager plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (Variables.stopCommands && Variables.inCombatEnabled) {
			if (plugin.getCm().isInCombat(event.getPlayer())) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(Messages.Command_Denied_InCombat);
			}
		}
		// Checking if PvPTimer is right every time a command is executed
		// Using this instead of a repeating task, seems less resource intensive
		if (Variables.pvpTimerEnabled) {
			if (plugin.getWtm().isPvpTimerWorld(event.getPlayer().getWorld())) {
				plugin.getWtm().getPvpTimer(event.getPlayer().getWorld()).checkWorldPvP();
			}
		}

	}

	@EventHandler
	public void onConsoleCommand(ServerCommandEvent event) {
		if (Variables.pvpTimerEnabled) {
			String[] cmd = event.getCommand().split(" ");
			if (cmd[0].equalsIgnoreCase("time")) {
				if (plugin.getWtm().isPvpTimerWorld("world")) {
					plugin.getWtm().getPvpTimer("world").checkWorldPvP();
				}

				if (cmd.length == 4) {
					if (plugin.getWtm().isPvpTimerWorld(cmd[3].toLowerCase())) {
						plugin.getWtm().getPvpTimer(cmd[3].toLowerCase()).checkWorldPvP();
					}
				}
			}
		}
	}

}
