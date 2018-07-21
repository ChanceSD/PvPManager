package me.NoChance.PvPManager.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;

public class PvPList implements CommandExecutor {

	private final PlayerHandler ph;

	public PvPList(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		sender.sendMessage(ChatColor.GOLD + "**** Players With PvP Enabled ****");
		sender.sendMessage(ChatColor.DARK_GRAY + (sender instanceof Player ? pvpList((Player) sender, false) : pvpList(null, true)));
		return true;
	}

	private String pvpList(final Player sender, final boolean console) {
		final StringBuilder list = new StringBuilder();
		for (final PvPlayer p : ph.getPlayers().values()) {
			if (p.hasPvPEnabled() && (console || sender.canSee(p.getPlayer()))) {
				list.append(p.getName()).append(", ");
			}
		}
		if (list.toString().isEmpty())
			return Messages.getPvpListNoResults();
		list.delete(list.length() - 2, list.length());
		return list.toString();
	}

}
