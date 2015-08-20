package me.NoChance.PvPManager.Commands;

import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Utils.CombatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvP implements CommandExecutor {

	private final PlayerHandler ph;

	public PvP(final PlayerHandler playerHandler) {
		this.ph = playerHandler;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			final PvPlayer pvpPlayer = ph.get(player);
			if (args.length == 0) {
				if (player.hasPermission("pvpmanager.pvpstatus.change")) {
					pvpPlayer.togglePvP();
					return true;
				}
			}
		}
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("disable") && sender instanceof Player) {
				final PvPlayer pvpPlayer = ph.get((Player) sender);
				if (pvpPlayer.isNewbie()) {
					pvpPlayer.setNewbie(false);
					return true;
				}
				sender.sendMessage(ChatColor.DARK_RED + "You are not protected!");
				return true;
			} else if (sender.hasPermission("pvpmanager.pvpstatus.change")) {
				if ((args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("on")) && sender instanceof Player) {
					final PvPlayer pvpPlayer = ph.get((Player) sender);
					if (pvpPlayer.hasToggleCooldownPassed()) {
						final boolean enable = args[0].equalsIgnoreCase("on");
						if (!enable && pvpPlayer.hasPvPEnabled() || enable && !pvpPlayer.hasPvPEnabled()) {
							pvpPlayer.setPvP(enable);
							return true;
						}
						sender.sendMessage(enable ? Messages.getAlreadyEnabled() : Messages.getAlreadyDisabled());
						return true;
					}
					return true;
				} else if (sender.hasPermission("pvpmanager.admin")) {
					if (!CombatUtils.isOnline(args[0])) {
						sender.sendMessage("§4Player not online!");
						return true;
					}
					final PvPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(args[0]));
					final boolean enable;
					if (args.length == 1)
						enable = !specifiedPlayer.hasPvPEnabled();
					else {
						if (args[1].equalsIgnoreCase("off"))
							enable = false;
						else if (args[1].equalsIgnoreCase("on"))
							enable = true;
						else
							return false;
					}
					specifiedPlayer.setPvP(enable);
					sender.sendMessage("§6[§8PvPManager§6] §2PvP " + (enable ? "enabled" : "disabled") + " for " + args[0]);
					return true;
				}
			}
		}
		sender.sendMessage(Messages.getErrorCommand());
		return true;
	}
}
