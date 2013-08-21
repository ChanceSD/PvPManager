package me.NoChance.PvPManager;

import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

	private PvPManager plugin;
	public Player loggedOut;

	public PlayerListener(PvPManager plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		if (plugin.inCombat.contains(event.getPlayer().getName())) {
			if (plugin.getConfig().getBoolean("PvPManager Settings.In Combat.Punishments.Enabled")) {
				if (plugin.getConfig().getBoolean("PvPManager Settings.In Combat.Punishments.Kill on Logout")) {
					loggedOut = event.getPlayer();
					event.getPlayer().setHealth(0);
					loggedOut = null;
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (loggedOut.equals(player)) {
			if (!plugin.getConfig().getBoolean("PvPManager Settings.In Combat.Punishments.Drop Itens")) {
				List<ItemStack> drops;
				drops = event.getDrops();
				int i = 0;
				ItemStack[] items = new ItemStack[drops.size()];
				for (ItemStack a : drops) {
					items[i] = a;
					i++;
				}
				player.getInventory().setContents(items);
			}
			if (plugin.getConfig().getBoolean("PvPManager Settings.In Combat.Punishments.Keep Exp")) {
				event.setKeepLevel(true);
			}
		}
	}
}
