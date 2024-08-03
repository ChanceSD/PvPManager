package me.chancesd.pvpmanager.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.utils.CombatUtils;

public class Untag implements CommandExecutor {

	private final PlayerManager ph;

	public Untag(final PlayerManager ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0)
			return false;
		CombatPlayer target;
		if (args.length == 1) {
			final String name = args[0];
			if (!CombatUtils.isOnlineWithFeedback(sender, name))
				return true;

			target = ph.get(Bukkit.getPlayer(name));
			if (!target.isInCombat()) {
				sender.sendMessage(Lang.PREFIXMSG + " §cThat player is not in combat");
				return true;
			}
			target.untag(UntagReason.COMMAND);
			sender.sendMessage(Lang.PREFIXMSG + " " + target.getName() + " §2has been untagged");
			return true;
		}
		return false;
	}

}
