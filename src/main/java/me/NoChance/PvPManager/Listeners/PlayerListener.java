package me.NoChance.PvPManager.Listeners;

import java.util.ArrayList;
import java.util.HashMap;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

	private PvPManager plugin;
	public Player loggedOut;
	public ItemStack[] it;
	public ItemStack[] armor;
	public HashMap<String, ArrayList<ItemStack[]>> noDrop = new HashMap<String, ArrayList<ItemStack[]>>();

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
				if (Variables.broadcastPvpLog)
					plugin.getServer().broadcastMessage(Messages.PvPLog_Broadcast.replace("%p", loggedOut.getName()));
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
					player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(event.getDroppedExp());
				}
			}
		}
		if (plugin.inCombat.contains(event.getEntity().getName())) {
			Player player = event.getEntity();
			if (player.hasPermission("pvpmanager.nodrop")) {
				ArrayList<ItemStack[]> inv = new ArrayList<ItemStack[]>();
				inv.add(player.getInventory().getContents());
				inv.add(player.getInventory().getArmorContents());
				noDrop.put(player.getName(), inv);
				event.getDrops().clear();
			}
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player.isOp() || player.hasPermission("pvpmanager.admin")) {
			if (plugin.update) {
				player.sendMessage("§6[§fPvPManager§6] " + "§2An update is available: §e" + plugin.newVersion);
				player.sendMessage("§6[§fPvPManager§6] " + "§2Your current version is: §ePvPManager v"
						+ plugin.getDescription().getVersion());
				player.sendMessage("§2Go to this page to download the latest version:");
				player.sendMessage("§2Link: §ehttp://dev.bukkit.org/bukkit-plugins/pvpmanager/");
			}
		} else if (!player.hasPlayedBefore()) {
			plugin.newbies.add(player.getName());
			player.sendMessage(Messages.Newbie_Protection.replace("%", Integer.toString(Variables.newbieProtectionTime)));
			scheduleNewbieRemoval(player.getName());
		}
		if (player.hasPermission("pvpmanager.nopvp")) {
			plugin.playersStatusOff.add(player.getName());
		}
	}

	private void scheduleNewbieRemoval(final String name) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				plugin.newbies.remove(name);
			}
		}, Variables.newbieProtectionTime * 1200);

	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (noDrop.containsKey(event.getPlayer().getName())) {
			Player player = event.getPlayer();
			player.getInventory().setContents(noDrop.get(event.getPlayer().getName()).get(0));
			player.getInventory().setArmorContents(noDrop.get(event.getPlayer().getName()).get(1));
			noDrop.remove(event.getPlayer().getName());
		}
	}

	@EventHandler
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		if (Variables.announcePvpOnWorldChange) {
			Player p = event.getPlayer();
			plugin.schedulers.get(p.getWorld().getName().toLowerCase()).announcePvP(p);
		}
	}
}
