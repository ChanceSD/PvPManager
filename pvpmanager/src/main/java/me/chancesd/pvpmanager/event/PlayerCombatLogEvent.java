package me.chancesd.pvpmanager.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.chancesd.pvpmanager.player.CombatPlayer;

/**
 * This event is called whenever a player combat logged.
 * <br>
 * Meaning the player disconnected while in combat.
 */
public final class PlayerCombatLogEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private final Player player;
	private final CombatPlayer pvplayer;

	public PlayerCombatLogEvent(final Player player, final CombatPlayer pvplayer) {
		this.player = player;
		this.pvplayer = pvplayer;
	}

	/**
	 * The player that combat logged
	 *
	 * @return Player
	 */
	public final Player getPlayer() {
		return player;
	}

	/**
	 * Utility method to quickly get the PvPlayer that combat logged
	 *
	 * @return PvPlayer instance of the player
	 */
	public final CombatPlayer getCombatPlayer() {
		return pvplayer;
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
