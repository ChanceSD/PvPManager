package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

	private PvPManager plugin;

	public CommandListener(PvPManager plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (Variables.stopCommands && Variables.inCombatEnabled) {
			if (plugin.getPlayerHandler().get(event.getPlayer()).isInCombat()) {
				if (!Variables.commandsAllowed.contains(event.getMessage().substring(1).split(" ")[0])) {
					event.setCancelled(true);
					event.getPlayer().sendMessage(Messages.Command_Denied_InCombat);
				}
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

}
