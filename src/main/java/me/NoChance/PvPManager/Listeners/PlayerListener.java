package me.NoChance.PvPManager.Listeners;

import java.util.ArrayList;
import java.util.HashMap;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PunishmentsManager;
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
	private PunishmentsManager pm;
	private HashMap<String, ArrayList<ItemStack[]>> noDrop = new HashMap<String, ArrayList<ItemStack[]>>();

	public PlayerListener(PvPManager plugin) {
		this.plugin = plugin;
		this.pm = plugin.getPm();
	}

	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (plugin.getCm().isInCombat(player)) {
			if (Variables.broadcastPvpLog)
				plugin.getServer().broadcastMessage(Messages.PvPLog_Broadcast.replace("%p", player.getName()));
			if (Variables.punishmentsEnabled) {
				boolean dead = false;
				if (Variables.killOnLogout) {
					pm.addPvpLog(player);
					if (!Variables.dropInventory || !Variables.dropArmor) {
						pm.noDropKill(player);
						dead = true;
					}
					if (Variables.dropExp) {
						pm.fakeExpDrop(player);
						if (!dead)
							player.setHealth(0);
					} else if(Variables.dropInventory && Variables.dropArmor && !Variables.dropExp)
						player.setHealth(0);
				} else if (!Variables.killOnLogout) {

					if (Variables.dropInventory) {
						pm.fakeInventoryDrop(player, player.getInventory().getContents());
						player.getInventory().clear();
					}
					if (Variables.dropArmor) {
						pm.fakeInventoryDrop(player, player.getInventory().getArmorContents());
						player.getInventory().setArmorContents(null);
					}
					if (Variables.dropExp) {
						pm.fakeExpDrop(player);
					}
				}
				if (Variables.fineEnabled)
					pm.applyFine(player);
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (pm.pvplogged(player)) {
			if (!Variables.dropExp) {
				event.setKeepLevel(true);
				event.setDroppedExp(0);
			}
		}
		if (plugin.getCm().isInCombat(player)) {
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
			if (Variables.update) {
				Messages.updateMessage(player);
			}
		} else if (!player.hasPlayedBefore()) {
			plugin.getCm().addNewbie(player);
			plugin.getCm().removeNewbieTimer(player.getName());
		}
		if (player.hasPermission("pvpmanager.nopvp")) {
			plugin.getCm().disablePvp(player);
		}
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
			if (plugin.getWtm().isPvpTimerWorld(event.getPlayer().getWorld())) {
				Player p = event.getPlayer();
				plugin.getWtm().getPvpTimer(p.getWorld()).sendWorldChangeMessage(p);
			}
		}
	}
}
