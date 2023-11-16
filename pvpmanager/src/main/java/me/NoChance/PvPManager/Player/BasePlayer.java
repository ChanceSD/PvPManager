package me.NoChance.PvPManager.Player;

import java.lang.ref.WeakReference;
import java.util.UUID;

import me.chancesd.pvpmanager.world.CombatWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class BasePlayer {

	private WeakReference<Player> player;
	private final UUID uuid;
	private CombatWorld combatWorld;

	protected BasePlayer(final Player player) {
		this.player = new WeakReference<>(player);
		this.uuid = player.getUniqueId();
	}

	public final Player getPlayer() {
		return player.get();
	}

	protected void setPlayer(final Player player) {
		this.player = new WeakReference<>(player);
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

	public void setCombatWorld(CombatWorld combatWorld) {
		this.combatWorld = combatWorld;
	}

	public final void message(final String message) {
		if (isOnline() && !message.isEmpty()) {
			getPlayer().sendMessage(message);
		}
	}

	public void sendActionBar(final String message) {
		if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.10") && !message.isEmpty()) { // Premium PvPManager supports lower versions with NMS
			if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.16.5")) {
				getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
			} else {
				getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
			}
		}
	}

	@Override
	public String toString() {
		return "PvPlayer[" + getName() + ", " + uuid + "]";
	}

}
