package me.NoChance.PvPManager.Managers;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.PvPManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class DisplayManager {

	private final PvPManager plugin;

	public DisplayManager(final PvPManager plugin) {
		this.plugin = plugin;
	}

	public void displayActionBar(final Player p, final String message) {
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
	}

}
