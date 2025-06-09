package me.chancesd.pvpmanager.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;
import me.chancesd.sdutils.utils.ChatUtils;
import me.chancesd.sdutils.utils.TimeUtil;

public class Newbie extends BaseCommand {

	private static final String ARG_PLAYER = "player";

	public Newbie(final PluginCommand pluginCommand, final PlayerManager ph) {
		super(pluginCommand);
		// Configure base command for showing usage
		description("Manage newbie protection")
				.usage("/newbie <subcommand>");

		// Add disable subcommand (conditionally available)
		if (Conf.NEWBIE_ALLOW_DISABLE.asBool()) {
			subCommand("disable", new DisableNewbieCommand(ph));
		}

		// Add admin subcommands
		subCommand("checktime", new CheckTimeCommand(ph));
		subCommand("add", new AddNewbieCommand(ph));
		subCommand("remove", new RemoveNewbieCommand(ph));
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		showHelpMenu(sender);
	}

	// Disable subcommand - available to all players when config allows
	private static class DisableNewbieCommand extends BaseCommand {
		private final PlayerManager ph;

		public DisableNewbieCommand(final PlayerManager ph) {
			this.ph = ph;
			playerOnly().description("Disable your newbie protection");
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			final Player player = (Player) sender;
			final CombatPlayer combatPlayer = ph.get(player);

			if (!combatPlayer.isNewbie()) {
				combatPlayer.message(Lang.ERROR_NOT_NEWBIE.msg());
				return;
			}
			combatPlayer.setNewbie(false);
			combatPlayer.message(Lang.NEWBIE_PROTECTION_REMOVED);
		}
	}

	// Check time subcommand - allows checking own time or admin checking others
	private static class CheckTimeCommand extends BaseCommand {
		private final PlayerManager ph;

		public CheckTimeCommand(final PlayerManager ph) {
			this.ph = ph;
			argument(ARG_PLAYER, ArgumentType.PLAYER).endArgument()
					.usage("/newbie checktime [player]")
					.description("Check newbie protection time");
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			// If no player argument provided, check own time (for players only)
			if (args.isEmpty()) {
				if (!(sender instanceof Player)) {
					sender.sendMessage("§cConsole must specify a player: /newbie checktime <player>");
					return;
				}

				final Player player = (Player) sender;
				final CombatPlayer combatPlayer = ph.get(player);

				if (!combatPlayer.isNewbie()) {
					combatPlayer.message(Lang.ERROR_NOT_NEWBIE.msg());
				} else {
					combatPlayer.message(Lang.NEWBIE_TIME_CHECK.msgTime(combatPlayer.getNewbieTimeLeft()));
				}
				return;
			}

			// Check another player's time (requires admin permission)
			if (!sender.hasPermission(Permissions.COMMAND_NEWBIE.getPermission())) {
				sender.sendMessage("§cYou don't have permission to check other players' newbie time.");
				return;
			}

			final Player targetPlayer = getArgument(args, ARG_PLAYER).getAsPlayerOrNull();
			final CombatPlayer target = ph.get(targetPlayer);
			final long timeLeft = target.getNewbieTimeLeft();
			sender.sendMessage(Lang.NEWBIE_TIME_CHECK_OTHER.msg(target.getName(), TimeUtil.getDiffDuration(Lang.NEWBIE_TIME_CHECK_OTHER, timeLeft)));
		}
	}

	// Add newbie subcommand - admin only
	private static class AddNewbieCommand extends BaseCommand {
		private final PlayerManager ph;

		public AddNewbieCommand(final PlayerManager ph) {
			this.ph = ph;
			permission(Permissions.COMMAND_NEWBIE.getPermission())
					.argument(ARG_PLAYER, ArgumentType.PLAYER).required().endArgument()
					.usage("/newbie add <player>")
					.description("Add newbie protection to a player");
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			final Player targetPlayer = getArgument(args, ARG_PLAYER).getAsPlayerOrNull();
			final CombatPlayer target = ph.get(targetPlayer);
			target.setNewbie(true);
			sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " Added newbie protection to &e " + target.getName()));
		}
	}

	// Remove newbie subcommand - admin only
	private static class RemoveNewbieCommand extends BaseCommand {
		private final PlayerManager ph;

		public RemoveNewbieCommand(final PlayerManager ph) {
			this.ph = ph;
			permission(Permissions.COMMAND_NEWBIE.getPermission())
					.argument(ARG_PLAYER, ArgumentType.PLAYER).required().endArgument()
					.usage("/newbie remove <player>")
					.description("Remove newbie protection from a player");
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			final Player targetPlayer = getArgument(args, ARG_PLAYER).getAsPlayerOrNull();
			final CombatPlayer target = ph.get(targetPlayer);
			target.setNewbie(false);
			sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " Removed newbie protection from &e " + target.getName()));
		}
	}
}
