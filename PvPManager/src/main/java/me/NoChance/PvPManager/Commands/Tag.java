package me.NoChance.PvPManager.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;

public class Tag implements CommandExecutor {

	private final PlayerHandler ph;

	public Tag(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0 && sender instanceof Player) {
			final PvPlayer pvPlayer = ph.get((Player) sender);
			if (!pvPlayer.isInCombat()) {
				pvPlayer.message("§cYou are not in combat");
			} else {
				pvPlayer.message(String.format("§8You are in combat for another %d seconds", (System.currentTimeMillis() - pvPlayer.getTaggedTime()) / 1000));
			}
			return true;
		} else if (!(sender instanceof Player)) {
			sender.sendMessage("This command is only available for players");
			return true;
		}
		return false;
	}

}
