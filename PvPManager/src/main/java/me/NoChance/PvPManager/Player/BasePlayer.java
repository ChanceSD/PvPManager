package me.NoChance.PvPManager.Player;

import java.lang.ref.WeakReference;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class BasePlayer {

	private WeakReference<Player> player;
	private final UUID uuid;

	public BasePlayer(final Player player) {
		this.player = new WeakReference<>(player);
		this.uuid = player.getUniqueId();
	}

	public final Player getPlayer() {
		return player.get();
	}

	protected void setPlayer(final Player player) {
		this.player = new WeakReference<>(player);
	}

	public final String getName() {
		return getPlayer().getName();
	}

	public final UUID getUUID() {
		return uuid;
	}

	public final boolean isOnline() {
		return getPlayer() != null;
	}

	public final void message(final String message) {
		if (isOnline() && !message.isEmpty()) {
			getPlayer().sendMessage(message);
		}
	}

	public void sendActionBar(final String message) {
		if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.10")) { // Premium PvPManager supports lower versions with NMS
			getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
		}
	}

}
