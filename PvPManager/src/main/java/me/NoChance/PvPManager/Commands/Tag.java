package me.NoChance.PvPManager.Commands;

import me.NoChance.PvPManager.Settings.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Settings;

public class Tag implements CommandExecutor {

	private final PlayerHandler ph;

	public Tag(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0 && sender instanceof Player) {
			final PvPlayer pvPlayer = ph.get((Player) sender);
			if (!pvPlayer.isInCombat())
				pvPlayer.message(Messages.getErrorNotInCombat());
			else {
				final long timeLeft = (pvPlayer.getTaggedTime() + Settings.getTimeInCombat() * 1000 - System.currentTimeMillis()) / 1000;
				pvPlayer.message(String.format(Messages.getTagRemaining(), timeLeft));
			}
			return true;
		} else if (!(sender instanceof Player)) {
			sender.sendMessage(Messages.getErrorNotPlayer());
			return true;
		}
		return false;
	}

}
