package me.chancesd.pvpmanager.setting;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.google.common.base.Preconditions;

public enum Permissions {
	ADMIN("pvpmanager.admin"),
	COMMAND_ANNOUNCE("pvpmanager.command.announce"),
	COMMAND_NEWBIE("pvpmanager.command.newbie"),
	COMMAND_OVERRIDE("pvpmanager.command.override"),
	COMMAND_PVP("pvpmanager.command.pvp"),
	COMMAND_PVP_OTHERS("pvpmanager.command.pvp.others"),
	COMMAND_PVPGLOBAL("pvpmanager.command.pvpglobal"),
	COMMAND_PVPINFO("pvpmanager.command.pvpinfo"),
	COMMAND_PVPINFO_OTHERS("pvpmanager.command.pvpinfo.others"),
	COMMAND_PVPLIST("pvpmanager.command.pvplist"),
	COMMAND_PVPSTATUS("pvpmanager.command.pvpstatus"),
	COMMAND_PVPSTATUS_OTHERS("pvpmanager.command.pvpstatus.others"),
	COMMAND_TAG("pvpmanager.command.tag"),
	COMMAND_UNTAG("pvpmanager.command.untag"),
	// Exempt permissions
	EXEMPT_BLOCK_COMMANDS("pvpmanager.exempt.blockcommands"),
	EXEMPT_COMBAT_LOG("pvpmanager.exempt.combatlog"),
	EXEMPT_COMBAT_TAG("pvpmanager.exempt.combattag"),
	EXEMPT_DISABLE_ACTIONS("pvpmanager.exempt.disableactions"),
	EXEMPT_KILL_ABUSE("pvpmanager.exempt.killabuse"),
	EXEMPT_PROTECTION_RESPAWN("pvpmanager.exempt.respawnprotection"),
	EXEMPT_PVPTOGGLE_COOLDOWN("pvpmanager.exempt.pvptogglecooldown");

	@NotNull
	private final String permission;

	private Permissions(final String permission) {
		Preconditions.checkNotNull(permission);
		this.permission = permission;
	}

	/**
	 * Checks if the player has this permission
	 *
	 * @param player the player to check for permission
	 * @return true if player has permission
	 */
	public boolean hasPerm(final Player player) {
		return player.hasPermission(permission);
	}

	/**
	 * Check if the command sender has this permission
	 *
	 * @param sender the sender to check for permission
	 * @return true if sender has permission
	 */
	public boolean hasPerm(final CommandSender sender) {
		return sender.hasPermission(permission);
	}

	/**
	 * Gets the string for this permission
	 */
	@NotNull
	public String getPermission() {
		return permission;
	}

	@Override
	public String toString() {
		return getPermission();
	}

}
