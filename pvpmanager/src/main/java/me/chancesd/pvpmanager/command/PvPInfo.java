package me.chancesd.pvpmanager.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.NoChance.PvPManager.PvPlayer;
import me.chancesd.pvpmanager.manager.PlayerHandler;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.pvpmanager.utils.CombatUtils;

public class PvPInfo implements CommandExecutor {

	private final PlayerHandler ph;

	public PvPInfo(final PlayerHandler ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (args.length == 0 && sender instanceof Player) {
			sendInfo(sender, ph.get((Player) sender));
			return true;
		} else if (args.length == 1 && sender.hasPermission("pvpmanager.info.others")) {
			final String name = args[0];
			if (CombatUtils.isOnline(name)) {
				sendInfo(sender, ph.get(Bukkit.getPlayer(name)));
				return true;
			}
			sender.sendMessage(Messages.getErrorPlayerNotFound(name));
			return true;
		}
		return false;
	}

	private void sendInfo(final CommandSender sender, final PvPlayer target) {
		sender.sendMessage(Messages.getString("PvPInfo_Title"));
		sender.sendMessage(Messages.getString("PvPInfo_Line1") + target.getName());
		sender.sendMessage(Messages.getString("PvPInfo_Line2") + target.getUUID());
		sender.sendMessage(Messages.getString("PvPInfo_Line3") + target.hasPvPEnabled());
		sender.sendMessage(Messages.getString("PvPInfo_Line4") + target.isInCombat());
		sender.sendMessage(Messages.getString("PvPInfo_Line5") + target.isNewbie());
		sender.sendMessage(Messages.getString("PvPInfo_Line6") + target.getPlayer().getWorld().getName());
		sender.sendMessage(Messages.getString("PvPInfo_Line7") + target.hasOverride());
		sender.sendMessage(ChatUtils.colorize("&2- Enemies: &7"
				+ (target.getEnemies().isEmpty() ? "&cNone" : target.getEnemies().stream().map(PvPlayer::getName).collect(Collectors.toList()))));
		sender.sendMessage(ChatUtils.colorize("&2- Exempt Perms: &7" + getExemptions(target.getPlayer())));

	}

	public String getExemptions(final Player player) {
		final List<String> exemptions = new ArrayList<>();
		checkExemption(exemptions, "&aCombatTag&7", Permissions.EXEMPT_COMBAT_TAG, player);
		checkExemption(exemptions, "&aCombatLog&7", Permissions.EXEMPT_COMBAT_LOG, player);
		checkExemption(exemptions, "&aDisableActions&7", Permissions.EXEMPT_DISABLE_ACTIONS, player);
		checkExemption(exemptions, "&aKillAbuse&7", Permissions.EXEMPT_KILL_ABUSE, player);
		checkExemption(exemptions, "&aBlockCommands&7", Permissions.EXEMPT_BLOCK_COMMANDS, player);
		if (exemptions.isEmpty())
			return "&cNone";
		return exemptions.toString();
	}

	private void checkExemption(final List<String> exemptions, final @NotNull String displayName, final Permissions perm, final Player player) {
		if (perm.hasPerm(player))
			exemptions.add(displayName);
	}

}
