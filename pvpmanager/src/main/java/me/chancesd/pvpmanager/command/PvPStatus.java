package me.chancesd.pvpmanager.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.utils.CombatUtils;

public class PvPStatus implements CommandExecutor {

	private final PlayerManager ph;

	public PvPStatus(final PlayerManager ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		CombatPlayer target;
		if (args.length == 0 && sender instanceof final Player player) {
			final CombatPlayer pvpPlayer = ph.get(player);
			if (!pvpPlayer.hasPvPEnabled()) {
				pvpPlayer.message(Messages.selfStatusDisabled);
				return true;
			}
			pvpPlayer.message(Messages.selfStatusEnabled);
			return true;
		} else if (args.length == 1 && sender.hasPermission("pvpmanager.pvpstatus.others")) {
			final String name = args[0];
			if (CombatUtils.isOnline(name)) {
				target = ph.get(Bukkit.getPlayer(name));
				if (!target.hasPvPEnabled()) {
					sender.sendMessage(Messages.othersStatusDisabled.getMsg(name));
					return true;
				}
				sender.sendMessage(Messages.otherStatusEnabled.getMsg(name));
				return true;
			}
			sender.sendMessage(Messages.errorPlayerNotFound.getMsg(name));
			return true;
		}
		return false;
	}

}
