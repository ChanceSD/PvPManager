package me.chancesd.pvpmanager.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

import me.chancesd.pvpmanager.player.CombatPlayer;

/**
 * This event is fired right before a player is tagged.
 * <p>
 * It is fired once for the damager and another for the damaged, if there is one of each.
 * <br>
 * Use {@link #isAttacker} to differentiate between them
 */
public final class PlayerTagEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private final Player player;
	private final CombatPlayer pvplayer;
	private final boolean isAttacker;
	private final Player enemy;

	public PlayerTagEvent(final Player player, final CombatPlayer pvplayer, final boolean isAttacker, final Player enemy) {
		this.player = player;
		this.pvplayer = pvplayer;
		this.isAttacker = isAttacker;
		this.enemy = enemy;
	}

	public PlayerTagEvent(final Player player, final CombatPlayer pvplayer, final boolean isAttacker) {
		this(player, pvplayer, isAttacker, null);
	}

	/**
	 * The player being tagged in this event
	 *
	 * @return Player being tagged
	 */
	public final Player getPlayer() {
		return player;
	}

	/**
	 * Utility method to quickly get the PvPlayer being tagged
	 *
	 * @return PvPlayer instance of the player
	 */
	public final CombatPlayer getPvPlayer() {
		return pvplayer;
	}

	/**
	 * Whether the player is the attacker or not
	 *
	 * @return true if the player is the tagger, false if the player got tagged
	 */
	public final boolean isAttacker() {
		return isAttacker;
	}

	/**
	 * Returns the enemy of {@link #getPlayer}.
	 * <p>
	 * In case {@link #isAttacker} is true it will return the player they damaged.
	 * <br>
	 * In case {@link #isAttacker} is false it will return their damager.
	 *
	 * @return The player's enemy or null if there isn't one
	 */
	@Nullable
	public final Player getEnemy() {
		return enemy;
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

}
