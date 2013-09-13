package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Variables;

import org.bukkit.entity.ExperienceOrb;
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
	public ItemStack[] armor;

	public PlayerListener(PvPManager plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		if (Variables.punishmentsEnabled && Variables.killOnLogout) {
			if (plugin.inCombat.contains(event.getPlayer().getName())) {
				loggedOut = event.getPlayer();
				loggedOut.setHealth(0);
				if (Variables.keepItems) {
					loggedOut.setHealth(20);
					loggedOut.getInventory().setContents(it);
					loggedOut.getInventory().setArmorContents(armor);
					if (!Variables.keepExp) {
						loggedOut.setLevel(0);
						loggedOut.setExp(0);
					}
				}
			}
		armor = null;
		it = null;
		loggedOut = null;
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (loggedOut != null) {
			Player player = event.getEntity();
			if (loggedOut.equals(player)) {
				if (Variables.keepItems) {
					armor = player.getInventory().getArmorContents();
					it = player.getInventory().getContents();
					event.getDrops().clear();
				}
				if (Variables.keepExp) {
					event.setKeepLevel(true);
					event.setDroppedExp(0);
				}
				if (!Variables.keepExp && Variables.keepItems) {
					player.getWorld()
							.spawn(player.getLocation(), ExperienceOrb.class)
							.setExperience(event.getDroppedExp());
				}
			}
		}
	}
}
