package me.chancesd.pvpmanager.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.NoChance.PvPManager.PvPlayer;
import me.chancesd.pvpmanager.manager.PlayerHandler;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.utils.CombatUtils;

public class PvPStatus implements CommandExecutor {

	private final PlayerHandler ph;

	public PvPStatus(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		PvPlayer target;
		if (args.length == 0 && sender instanceof Player) {
			final Player player = (Player) sender;
			final PvPlayer pvpPlayer = ph.get(player);
			if (!pvpPlayer.hasPvPEnabled()) {
				pvpPlayer.message(Messages.getSelfStatusDisabled());
				return true;
			}
			pvpPlayer.message(Messages.getSelfStatusEnabled());
			return true;
		} else if (args.length == 1 && sender.hasPermission("pvpmanager.pvpstatus.others")) {
			final String name = args[0];
			if (CombatUtils.isOnline(name)) {
				target = ph.get(Bukkit.getPlayer(name));
				if (!target.hasPvPEnabled()) {
					sender.sendMessage(Messages.getOthersStatusDisabled(name));
					return true;
				}
				sender.sendMessage(Messages.getOtherStatusEnabled(name));
				return true;
			}
			sender.sendMessage(Messages.getErrorPlayerNotFound(name));
			return true;
		}
		return false;
	}

}
