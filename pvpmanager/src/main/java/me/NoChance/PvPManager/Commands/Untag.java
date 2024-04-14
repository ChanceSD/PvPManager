package me.NoChance.PvPManager.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class Untag implements CommandExecutor {

	private final PlayerHandler ph;

	public Untag(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0)
			return false;
		PvPlayer target;
		if (args.length == 1) {
			final String name = args[0];
			if (!CombatUtils.isOnlineWithFeedback(sender, name))
				return true;

			target = ph.get(Bukkit.getPlayer(name));
			if (!target.isInCombat()) {
				sender.sendMessage(Messages.PREFIXMSG + " §cThat player is not in combat");
				return true;
			}
			target.unTag();
			sender.sendMessage(Messages.PREFIXMSG + " " + target.getName() + " §2has been untagged");
			return true;
		}
		return false;
	}

}
