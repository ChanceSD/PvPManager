package me.NoChance.PvPManager.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.NoChance.PvPManager.PvPlayer;

public final class PlayerUntagEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private final Player player;
	private final PvPlayer pvplayer;

	public PlayerUntagEvent(final Player player, final PvPlayer pvplayer) {
		this.player = player;
		this.pvplayer = pvplayer;
	}

	public final Player getPlayer() {
		return player;
	}

	public final PvPlayer getPvPlayer() {
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
