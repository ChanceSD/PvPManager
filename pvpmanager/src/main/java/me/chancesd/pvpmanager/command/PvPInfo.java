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
				.usage("/pvpinfo [player]").permission(Permissions.COMMAND_PVPINFO.getPermission())
				.argument(ARG_PLAYER, ArgumentType.PLAYER).requirePermission(Permissions.COMMAND_PVPINFO_OTHERS.getPermission()).endArgument();
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		if (args.isEmpty() && sender instanceof final Player player) {
			sendInfo(sender, ph.get(player));
		} else if (hasArgument(args, ARG_PLAYER)) {
			final CommandArgument targetArg = getArgument(args, ARG_PLAYER);
			final Player targetPlayer = targetArg.getAsPlayer();
			sendInfo(sender, ph.get(targetPlayer));
		} else {
			ChatUtils.send(sender, "&#FF5555This command requires a target player when used from console.");
		}
	}

	private void sendInfo(final CommandSender sender, final CombatPlayer target) {
		// Header with player name
		ChatUtils.send(sender, Lang.getString("PvPInfo_Header").replace("{player}", target.getName()));

		// Basic info
		ChatUtils.send(sender, Lang.getString("PvPInfo_UUID") + target.getUUID());

		// Combat status with time remaining
		if (target.isInCombat()) {
			final long timeLeft = target.getTagTimeLeft();
			ChatUtils.send(sender, Lang.getString("PvPInfo_Combat") + "&#FF5555⚔ In combat &7(" + timeLeft / 1000 + "s left)");
		} else {
			ChatUtils.send(sender, Lang.getString("PvPInfo_Combat") + "&a✗ Not in combat");
		}

		// Combined protection status
		ChatUtils.send(sender, Lang.getString("PvPInfo_Protection") + getProtectionStatus(target));

		// World and override
		ChatUtils.send(sender, Lang.getString("PvPInfo_World") + target.getPlayer().getWorld().getName());
		ChatUtils.send(sender, Lang.getString("PvPInfo_Override") + (target.hasOverride() ? "&a✓ Enabled" : "&c✗ Disabled"));

		// Enemies
		if (target.getEnemies().isEmpty()) {
			ChatUtils.send(sender, Lang.getString("PvPInfo_Enemies") + "&cNone");
		} else {
			final String enemies = String.join("&7, &f", target.getEnemies().stream().map(CombatPlayer::getName).toList());
			ChatUtils.send(sender, Lang.getString("PvPInfo_Enemies") + enemies);
		}

		// Exemptions
		ChatUtils.send(sender, Lang.getString("PvPInfo_Exemptions") + getExemptions(target.getPlayer()));

		// Footer
		ChatUtils.send(sender, Lang.getString("PvPInfo_Footer"));
	}

	private String getProtectionStatus(final CombatPlayer target) {
		final List<String> protections = new ArrayList<>();

		// Check for all possible protection types individually
		// This duplicates logic from PlayerManager.checkProtection() but allows showing multiple protections

		// Global protection check
		if (!ph.getGlobalStatus()) {
			protections.add("&#CC6666🛡 Global");
		}

		// World protection check
		if (!target.getCombatWorld().isCombatAllowed()) {
			protections.add("&#9966CC🛡 World");
		}

		// Respawn protection check
		if (target.hasRespawnProtection()) {
			protections.add("&#5599FF🛡 Respawn");
		}

		// Newbie protection (special handling with time)
		if (target.isNewbie()) {
			final long timeLeft = target.getNewbieTimeLeft();
			if (timeLeft > 0) {
				protections.add("&#FFAA00🛡 Newbie &7(" + timeLeft / 1000 / 60 + "m left)");
			} else {
				protections.add("&#FFAA00🛡 Newbie");
			}
		}

		// PvP disabled check
		if (!target.hasPvPEnabled()) {
			protections.add("&#FF6666🛡 PvP Disabled");
		}

		// AFK protection check via DependencyManager
		if (ph.getPlugin().getDependencyManager().shouldProtectAFK(target.getPlayer())) {
			protections.add("&#66CCAA🛡 AFK");
		}

		if (protections.isEmpty()) {
			return "&c✗ None";
		}

		return String.join("&7, ", protections);
	}

	public String getExemptions(final Player player) {
		final List<String> exemptions = new ArrayList<>();
		checkExemption(exemptions, "&aCombatTag", Permissions.EXEMPT_COMBAT_TAG, player);
		checkExemption(exemptions, "&aCombatLog", Permissions.EXEMPT_COMBAT_LOG, player);
		checkExemption(exemptions, "&aDisableActions", Permissions.EXEMPT_DISABLE_ACTIONS, player);
		checkExemption(exemptions, "&aKillAbuse", Permissions.EXEMPT_KILL_ABUSE, player);
		checkExemption(exemptions, "&aBlockCommands", Permissions.EXEMPT_BLOCK_COMMANDS, player);
		if (exemptions.isEmpty())
			return "&cNone";
		return String.join("&7, ", exemptions);
	}

	private void checkExemption(final List<String> exemptions, final @NotNull String displayName, final Permissions perm, final Player player) {
		if (perm.hasPerm(player))
			exemptions.add(displayName);
	}

}
