package me.chancesd.pvpmanager.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.utils.TimeUtil;

public class Newbie implements TabExecutor {
    private final PlayerManager ph;
    private static final String CMD_DISABLE = "disable";
    private static final String CMD_CHECKTIME = "checktime";
    private static final String CMD_ADD = "add";
    private static final String CMD_REMOVE = "remove";

    public Newbie(final PlayerManager ph) {
        this.ph = ph;
    }

    @Override
    public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		// Show own newbie status
        if (args.length == 0) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage("This command is only available for players.");
                return false;
            }
            showNewbieStatus(ph.get(player));
            return true;
        }

        final String subcommand = args[0].toLowerCase();

		// Handle player disable command
        if (Conf.NEWBIE_ALLOW_DISABLE.asBool() && subcommand.equals(CMD_DISABLE)) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage("This command is only available for players.");
                return false;
            }
            return handleDisableNewbie(ph.get(player));
        }

		// Handle admin commands
        if (!Permissions.ADMIN.hasPerm(sender) || args.length != 2) {
            return false;
        }

        final String targetName = args[1];
        switch (subcommand) {
            case CMD_CHECKTIME:
                checkNewbieTime(sender, targetName);
                return true;
            case CMD_ADD:
                setNewbieStatus(sender, targetName, true);
                return true;
            case CMD_REMOVE:
                setNewbieStatus(sender, targetName, false);
                return true;
            default:
                return false;
        }
    }

    private void showNewbieStatus(final CombatPlayer player) {
        if (!player.isNewbie()) {
            player.message(Lang.ERROR_NOT_NEWBIE.msg());
        } else {
            player.message(Lang.NEWBIE_TIME_CHECK.msgTime(player.getNewbieTimeLeft()));
        }
    }

    private boolean handleDisableNewbie(final CombatPlayer player) {
        if (!player.isNewbie()) {
            player.message(Lang.ERROR_NOT_NEWBIE.msg());
            return true;
        }
        player.setNewbie(false);
        return true;
    }

    private void checkNewbieTime(final CommandSender sender, final String targetName) {
        if (!CombatUtils.isOnlineWithFeedback(sender, targetName)) return;

        final CombatPlayer target = ph.get(Bukkit.getPlayer(targetName));
        final long timeLeft = target.getNewbieTimeLeft();
		sender.sendMessage(Lang.NEWBIE_TIME_CHECK_OTHER.msg(target.getName(), TimeUtil.getDiffDuration(Lang.NEWBIE_TIME_CHECK_OTHER, timeLeft)));
    }

    private void setNewbieStatus(final CommandSender sender, final String targetName, final boolean enable) {
        if (!CombatUtils.isOnlineWithFeedback(sender, targetName)) return;

        final CombatPlayer target = ph.get(Bukkit.getPlayer(targetName));
        target.setNewbie(enable);
		sender.sendMessage(ChatUtils.colorize(
				String.format(Lang.PREFIXMSG + " %s newbie protection %s &e%s", enable ? "Added" : "Removed", enable ? "to" : "from", target.getName())));
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 1) {
			final List<String> commands = Permissions.ADMIN.hasPerm(sender)
                ? Lists.newArrayList(CMD_ADD, CMD_CHECKTIME, CMD_DISABLE, CMD_REMOVE)
                : Lists.newArrayList(CMD_DISABLE);
            return ChatUtils.getMatchingEntries(args[0], commands);
        }

        if (args.length == 2 && Permissions.ADMIN.hasPerm(sender)) {
			return ChatUtils.getMatchingEntries(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        }

        return Collections.emptyList();
    }
}
