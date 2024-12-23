package me.NoChance.PvPManager.Player;

import java.util.UUID;

import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.world.CombatWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class BasePlayer {

	private final Player player;
	private final UUID uuid;
	private CombatWorld combatWorld;
	private long actionBarCooldown;
	private String lastActionBarMessage;

	protected BasePlayer(final Player player) {
		this.player = player;
		this.uuid = player.getUniqueId();
	}

	public final Player getPlayer() {
		return player;
	}

	@NotNull
	public final String getName() {
		return getPlayer().getName();
	}

	public final UUID getUUID() {
		return uuid;
	}

	public final boolean isOnline() {
		return getPlayer() != null;
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
	 * Sends an action bar message with the specified duration
	 *
	 * @param message  The message to be sent
	 * @param duration The duration in milliseconds
	 */
	public void sendActionBar(final String message, final long duration) {
		if (System.currentTimeMillis() < actionBarCooldown || message.isEmpty()
				|| System.currentTimeMillis() - actionBarCooldown < 1000 && message.equals(lastActionBarMessage))
			return;
		if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.10")) { // Premium PvPManager supports lower versions with NMS
			if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.16.5")) {
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
		return "PvPlayer[" + getName() + ", " + uuid + "]";
	}

}
