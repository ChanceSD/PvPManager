package me.NoChance.PvPManager.Commands;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PvPList implements CommandExecutor {

	private final PlayerHandler ph;

	public PvPList(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		sender.sendMessage(ChatColor.GOLD + "**** Players With PvP Enabled ****");
		sender.sendMessage(ChatColor.DARK_GRAY + pvpList());
		return true;
	}

	private String pvpList() {
		final StringBuilder list = new StringBuilder();
		for (final PvPlayer p : ph.getPlayers().values()) {
			if (p.hasPvPEnabled()) {
				list.append(p.getName() + ", ");
			}
		}
		if (list.toString().isEmpty())
			return "No Players with PvP Enabled";
		list.delete(list.length() - 2, list.length());
		return list.toString();
	}

}
