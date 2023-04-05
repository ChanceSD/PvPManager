package me.NoChance.PvPManager.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;

public class PvPOverride implements CommandExecutor {

	private final PlayerHandler ph;

	public PvPOverride(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (sender instanceof Player) {
			final PvPlayer pvPlayer = ph.get((Player) sender);
			final boolean override = pvPlayer.toggleOverride();
			pvPlayer.message("§2PvP Override Set To: " + override);
		}
		return true;
	}

}
