package me.chancesd.pvpmanager.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.NoChance.PvPManager.PvPlayer;
import me.chancesd.pvpmanager.manager.PlayerHandler;
import me.chancesd.pvpmanager.setting.Messages;

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
			pvPlayer.message(Messages.PREFIXMSG + ChatColor.DARK_GREEN + "PvP Override set to: " + ChatColor.GOLD + override);
		}
		return true;
	}

}
