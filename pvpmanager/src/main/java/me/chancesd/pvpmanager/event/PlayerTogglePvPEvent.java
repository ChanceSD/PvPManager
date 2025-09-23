package me.chancesd.pvpmanager.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.chancesd.pvpmanager.player.CombatPlayer;

/**
 * This event is called any time a Player's PvP state would change.
 * <br>
 * If the event is cancelled the player's state will not change.
 */
public final class PlayerTogglePvPEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private final Player player;
	private final CombatPlayer pvplayer;
	private final boolean pvpState;

	public PlayerTogglePvPEvent(final Player player, final CombatPlayer pvplayer, final boolean pvpState) {
		this.player = player;
		this.pvplayer = pvplayer;
		this.pvpState = pvpState;
	}

	/**
	 * The player
	 *
	 * @return Player
	 */
	public final Player getPlayer() {
		return player;
	}

	/**
	 * Utility method to quickly get the PvPlayer
	 *
	 * @return PvPlayer instance of the player
	 */
	public final CombatPlayer getCombatPlayer() {
		return pvplayer;
	}

	/**
	 * Returns the player's new PvP state
	 *
	 * @return true if the player would now have PvP enabled, false if the player would now have PvP disabled
	 */
	public final boolean getPvPState() {
		return pvpState;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(final boolean cancel) {
		cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
