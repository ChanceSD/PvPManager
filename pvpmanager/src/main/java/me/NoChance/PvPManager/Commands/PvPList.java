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
		sender.sendMessage(Messages.getPvpListTitle());
		sender.sendMessage(Messages.getPvpListEnabled());
		sender.sendMessage(ChatColor.GRAY + (sender instanceof Player ? pvpList((Player) sender, true, false) : pvpList(null, true, true)));
		sender.sendMessage(Messages.getPvpListDisabled());
		sender.sendMessage(ChatColor.GRAY + (sender instanceof Player ? pvpList((Player) sender, false, false) : pvpList(null, false, true)));
		return true;
	}

	private String pvpList(final Player sender, final boolean enabled, final boolean console) {
		final StringBuilder list = new StringBuilder();
		for (final PvPlayer p : ph.getPlayers().values()) {
			final Player player = p.getPlayer();
			if (enabled == p.hasPvPEnabled() && player != null && (console || sender.canSee(player))) {
				list.append(p.getName()).append(", ");
			}
		}
		if (list.toString().isEmpty())
			return Messages.getPvpListNoResults();
		list.delete(list.length() - 2, list.length());
		return list.toString();
	}

}
