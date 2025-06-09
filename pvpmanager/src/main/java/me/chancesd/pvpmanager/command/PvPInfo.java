package me.chancesd.pvpmanager.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;
import me.chancesd.sdutils.utils.ChatUtils;

public class PvPInfo extends BaseCommand {

	private static final String ARG_PLAYER = "player";

	private final PlayerManager ph;

	public PvPInfo(final PluginCommand pluginCommand, final PlayerManager ph) {
		super(pluginCommand);
		this.ph = ph;
		this.description("Check your or other player info")
				.usage("/pvpinfo [player]").permission(Permissions.COMMAND_PVP_INFO.getPermission())
				.argument(ARG_PLAYER, ArgumentType.PLAYER).requirePermission(Permissions.COMMAND_PVP_INFO_OTHERS.getPermission()).endArgument();
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		if (args.isEmpty() && sender instanceof final Player player) {
			sendInfo(sender, ph.get(player));
		} else if (hasArgument(args, ARG_PLAYER)) {
			final CommandArgument targetArg = getArgument(args, ARG_PLAYER);
			final Player targetPlayer = targetArg.getAsPlayerOrNull();
			sendInfo(sender, ph.get(targetPlayer));
		} else {
			sender.sendMessage("§cThis command requires a target player when used from console.");
		}
	}

	private void sendInfo(final CommandSender sender, final CombatPlayer target) {
		sender.sendMessage(Lang.getString("PvPInfo_Title"));
		sender.sendMessage(Lang.getString("PvPInfo_Line1") + target.getName());
		sender.sendMessage(Lang.getString("PvPInfo_Line2") + target.getUUID());
		sender.sendMessage(Lang.getString("PvPInfo_Line3") + target.hasPvPEnabled());
		sender.sendMessage(Lang.getString("PvPInfo_Line4") + target.isInCombat());
		sender.sendMessage(Lang.getString("PvPInfo_Line5") + target.isNewbie());
		sender.sendMessage(Lang.getString("PvPInfo_Line6") + target.getPlayer().getWorld().getName());
		sender.sendMessage(Lang.getString("PvPInfo_Line7") + target.hasOverride());
		sender.sendMessage(ChatUtils.colorize("&2- Enemies: &7"
				+ (target.getEnemies().isEmpty() ? "&cNone" : target.getEnemies().stream().map(CombatPlayer::getName).toList())));
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
