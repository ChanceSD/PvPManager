package me.chancesd.pvpmanager.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.chancesd.pvpmanager.player.CombatPlayer;

/**
 * This event is fired when a player's newbie protection status changes.
 * <p>
 * Use {@link #isStarting} to determine if protection is starting or ending.
 */
public final class NewbieProtectionEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final CombatPlayer pvplayer;
	private final boolean starting;

	public NewbieProtectionEvent(final Player player, final CombatPlayer pvplayer, final boolean starting) {
		this.player = player;
		this.pvplayer = pvplayer;
		this.starting = starting;
	}

	/**
	 * The player whose newbie protection status changed
	 *
	 * @return Player whose protection status changed
	 */
	public final Player getPlayer() {
		return player;
	}

	/**
	 * Utility method to quickly get the PvPlayer whose protection status changed
	 *
	 * @return PvPlayer instance of the player
	 */
	public final CombatPlayer getPvPlayer() {
		return pvplayer;
	}

	/**
	 * Whether the newbie protection is starting or ending
	 *
	 * @return true if protection is starting, false if protection is ending
	 */
	public final boolean isStarting() {
		return starting;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
