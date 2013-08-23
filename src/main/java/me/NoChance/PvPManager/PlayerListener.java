package me.NoChance.PvPManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

	private PvPManager plugin;
	public Player loggedOut;
	public ItemStack[] it;

	public PlayerListener(PvPManager plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		if (plugin.inCombat.contains(event.getPlayer().getName())) {
			if (plugin.getConfig().getBoolean(
					"PvPManager Settings.In Combat.Punishments.Enabled")) {
				if (plugin
						.getConfig()
						.getBoolean(
								"PvPManager Settings.In Combat.Punishments.Kill on Logout")) {
					loggedOut = event.getPlayer();
					loggedOut.setHealth(0);
					if (plugin
							.getConfig()
							.getBoolean(
									"PvPManager Settings.In Combat.Punishments.Keep Items")
							&& loggedOut.isDead()) {
						loggedOut.setHealth(20);
						loggedOut.getInventory().setContents(it);
						if (!plugin
								.getConfig()
								.getBoolean(
										"PvPManager Settings.In Combat.Punishments.Keep Exp")) {
							loggedOut.setLevel(0);
							loggedOut.setExp(0);
						}
					}
				}
			}
		}
		it = null;
		loggedOut = null;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (loggedOut.equals(player)) {
			if (plugin.getConfig().getBoolean(
					"PvPManager Settings.In Combat.Punishments.Keep Items")) {
				it = loggedOut.getInventory().getContents();
				event.getDrops().clear();
			}
			if (plugin.getConfig().getBoolean(
					"PvPManager Settings.In Combat.Punishments.Keep Exp")) {
				event.setKeepLevel(true);
				event.setDroppedExp(0);
			}
		}
	}
}
