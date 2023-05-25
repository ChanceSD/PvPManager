package me.NoChance.PvPManager.Commands;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.ChatUtils;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class Newbie implements TabExecutor {
	private final PlayerHandler ph;

	public Newbie(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0 && sender instanceof Player) {
			final PvPlayer pvPlayer = ph.get((Player) sender);
			if (!pvPlayer.isNewbie()) {
				pvPlayer.message(Messages.getErrorNotNewbie());
			} else {
				final long timeLeft = pvPlayer.getNewbieTimeLeft() / 1000;
				pvPlayer.message(String.format(Messages.getNewbieTimeCheck(), timeLeft));
			}
			return true;
		} else if (args.length >= 1) {
			if (Settings.isNewbieDisableAllowed() && args[0].equalsIgnoreCase("disable") && sender instanceof Player) {
				final PvPlayer player = ph.get((Player) sender);
				if (player.isNewbie()) {
					player.setNewbie(false);
					return true;
				}
				player.message(Messages.getErrorNotNewbie());
				return true;
			} else if (sender.hasPermission("pvpmanager.admin")) {
				if (!CombatUtils.isOnline(args[0])) {
					sender.sendMessage(Messages.getErrorPlayerNotFound().replace("%p", args[0]));
					return true;
				}
				final PvPlayer specifiedPlayer = ph.get(Bukkit.getPlayer(args[0]));
				final long timeLeft = specifiedPlayer.getNewbieTimeLeft() / 1000;
				sender.sendMessage(String.format(Messages.getNewbieTimeCheckOther(), specifiedPlayer.getName(), timeLeft));
				return true;
			}
		} else if (!(sender instanceof Player)) {
			sender.sendMessage("This command is only available for players.");
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 1) {
			if (!sender.hasPermission("pvpmanager.admin"))
				return ChatUtils.getMatchingEntries(args[0], Lists.newArrayList("disable"));
			final List<String> list = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			list.add("disable");
			return ChatUtils.getMatchingEntries(args[0], list);
		}

		return Collections.emptyList();
	}
}
