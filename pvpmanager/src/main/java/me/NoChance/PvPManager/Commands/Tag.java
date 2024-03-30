package me.NoChance.PvPManager.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class Tag implements CommandExecutor {

	private final PlayerHandler ph;

	public Tag(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0 && sender instanceof Player) {
			final PvPlayer pvPlayer = ph.get((Player) sender);
			if (!pvPlayer.isInCombat()) {
				pvPlayer.message(Messages.getTagNotInCombat());
			} else {
				final long timeLeft = (pvPlayer.getTaggedTime() + Settings.getTimeInCombat() * 1000 - System.currentTimeMillis()) / 1000;
				pvPlayer.message(String.format(Messages.getTagTimeLeft(), timeLeft));
			}
			return true;
		} else if (args.length == 1 && sender.hasPermission("pvpmanager.admin")) {
			final String name = args[0];
			if (!CombatUtils.isOnlineWithFeedback(sender, name))
				return true;

			final PvPlayer target = ph.get(Bukkit.getPlayer(name));
			if (target.isInCombat()) {
				sender.sendMessage(Messages.PREFIXMSG + " §cThat player is already in combat");
				return true;
			}
			target.setTagged(true, target);
			sender.sendMessage(Messages.PREFIXMSG + " " + target.getName() + "§2has been combat tagged");
			return true;
		}
		return false;
	}

}
