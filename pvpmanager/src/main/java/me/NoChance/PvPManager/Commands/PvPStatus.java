package me.NoChance.PvPManager.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Utils.CombatUtils;

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
			if (CombatUtils.isOnline(args[0])) {
				target = ph.get(Bukkit.getPlayer(args[0]));
				if (!target.hasPvPEnabled()) {
					sender.sendMessage(Messages.getOthersStatusDisabled().replace("%p", args[0]));
					return true;
				}
				sender.sendMessage(Messages.getOtherStatusEnabled().replace("%p", args[0]));
				return true;
			}
			sender.sendMessage(Messages.getErrorPlayerNotFound().replace("%p", args[0]));
			return true;
		}
		return false;
	}

}
