package me.NoChance.PvPManager.Commands;

import me.NoChance.PvPManager.Managers.PlayerHandler;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvPOverride implements CommandExecutor {

	private final PlayerHandler ph;

	public PvPOverride(final PlayerHandler ph) {
		this.ph = ph;
	}

	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (sender instanceof Player) {
			final boolean override = ph.get((Player) sender).toggleOverride();
			sender.sendMessage("§2PvP Override Set To: " + override);
		}
		return true;
	}

}
