package me.chancesd.pvpmanager.player;

import java.util.UUID;

import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.setting.DisplayMode;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.utils.MCVersion;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class BasePlayer {

	@NotNull
	private final Player player;
	@NotNull
	private final UUID uuid;
	private CombatWorld combatWorld;
	private long actionBarCooldown;
	private String lastActionBarMessage;
	private boolean wasAllowedFlight;
	private final PlayerExemptions exemptions = new PlayerExemptions();

	protected BasePlayer(@NotNull final Player player) {
		this.player = player;
		this.uuid = player.getUniqueId();
	}

	@NotNull
	public final Player getPlayer() {
		return player;
	}

	@NotNull
	public final String getName() {
		return getPlayer().getName();
	}

	@NotNull
	public final UUID getUUID() {
		return uuid;
	}

	public final boolean isOnline() {
		return CombatUtils.isOnline(uuid);
	}

	public CombatWorld getCombatWorld() {
		return combatWorld;
	}

	public void setCombatWorld(final CombatWorld combatWorld) {
		this.combatWorld = combatWorld;
	}

	/**
	 * @param permission the permission to check
	 * @return true if this player has the specified permission
	 */
	public boolean hasPerm(final Permissions permission) {
		return permission.hasPerm(getPlayer());
	}

	public final void message(final String message) {
		if (isOnline() && !message.isEmpty()) {
			getPlayer().sendMessage(message);
		}
	}

	/**
	 * Sends a message according to its display mode (chat or action bar)
	 *
	 * @param message The Lang message to be sent
	 * @param duration The duration for action bar messages (ignored for chat)
	 * @param args    Optional arguments for message replacements
	 */
	public final void messageWithDuration(final Lang message, final long duration, final Object... args) {
		if (!isOnline()) {
			return;
		}

		final String formattedMessage = args.length > 0 ? message.msg(args) : message.msg();
		if (formattedMessage.isEmpty()) {
			return;
		}
		if (message.getDisplayMode() == DisplayMode.ACTION_BAR) {
			sendActionBar(formattedMessage, duration);
		} else {
			getPlayer().sendMessage(formattedMessage);
		}
	}

	/**
	 * Sends a message according to its display mode (chat or action bar) with default duration
	 *
	 * @param message The Lang message to be sent
	 * @param args    Optional arguments for message replacements
	 */
	public final void message(final Lang message, final Object... args) {
		messageWithDuration(message, 1000, args);
	}

	/**
	 * Sends an action bar message with the specified duration
	 *
	 * @param message  The message to be sent
	 * @param duration The duration in milliseconds
	 */
	@SuppressWarnings({ "deprecation" })
	public void sendActionBar(final String message, final long duration) {
		if (System.currentTimeMillis() < actionBarCooldown || message.isEmpty()
				|| System.currentTimeMillis() - actionBarCooldown < 1000 && message.equals(lastActionBarMessage))
			return;
		if (MCVersion.isAtLeast(MCVersion.V1_10)) { // Premium PvPManager supports lower versions with NMS
			if (MCVersion.isAtLeast(MCVersion.V1_20_4)) {
				getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
			} else if (MCVersion.isAtLeast(MCVersion.V1_16_5)) {
				getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
			} else {
				getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
			}
		}
		lastActionBarMessage = message;
		actionBarCooldown = System.currentTimeMillis() + duration;
	}

	/**
	 * Sends an action bar message with default duration
	 *
	 * @param message The message to be sent
	 */
	public void sendActionBar(final String message) {
		sendActionBar(message, 0);
	}

	@Override
	public String toString() {
		return "CombatPlayer[" + getName() + ", " + uuid + "]";
	}

	public boolean getWasAllowedFlight() {
		return wasAllowedFlight;
	}

	public void setWasAllowedFlight(final boolean wasAllowedFlight) {
		this.wasAllowedFlight = wasAllowedFlight;
	}

	public PlayerExemptions getExemptions() {
		return exemptions;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof BasePlayer))
			return false;
		return uuid.equals(((BasePlayer) obj).getUUID());
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

}
