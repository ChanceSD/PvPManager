package me.chancesd.pvpmanager.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;

/**
 * This event is called before a player is set as no longer in combat.
 * <br>
 * This event may be called by different threads, it is up to the developer to make their code thread safe
 */
public final class PlayerUntagEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final CombatPlayer pvplayer;
	private final UntagReason reason;

	public PlayerUntagEvent(final Player player, final CombatPlayer pvplayer, final UntagReason reason) {
		this.player = player;
		this.pvplayer = pvplayer;
		this.reason = reason;
	}

	/**
	 * The player being untagged in this event
	 *
	 * @return Player being tagged
	 */
	public final Player getPlayer() {
		return player;
	}

	/**
	 * Utility method to quickly get the PvPlayer being untagged
	 *
	 * @return PvPlayer instance of the player
	 */
	public final CombatPlayer getCombatPlayer() {
		return pvplayer;
	}

	/**
	 * @return The reason for this player being untagged
	 */
	public UntagReason getReason() {
		return reason;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
