package me.chancesd.pvpmanager.setting;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.google.common.base.Preconditions;

public enum Permissions {

	ADMIN("pvpmanager.admin"),
	EXEMPT_BLOCK_COMMANDS("pvpmanager.exempt.block.commands"),
	EXEMPT_COMBAT_LOG("pvpmanager.nocombatlog"),
	EXEMPT_COMBAT_TAG("pvpmanager.nocombattag"),
	EXEMPT_DISABLE_ACTIONS("pvpmanager.nodisable"),
	EXEMPT_KILL_ABUSE("pvpmanager.nokillabuse"),
	EXEMPT_PVPTOGGLE_COOLDOWN("pvpmanager.pvpstatus.nocooldown");

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
