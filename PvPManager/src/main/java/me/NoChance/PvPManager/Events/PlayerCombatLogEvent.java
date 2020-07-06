package me.NoChance.PvPManager.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.NoChance.PvPManager.PvPlayer;

public final class PlayerCombatLogEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final PvPlayer pvplayer;

	public PlayerCombatLogEvent(final Player player, final PvPlayer pvplayer) {
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
	public final PvPlayer getPvPlayer() {
		return pvplayer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
