package me.NoChance.PvPManager.Listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PunishmentsManager;
import me.NoChance.PvPManager.Others.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

	private PvPManager plugin;
	private PunishmentsManager pm;
	private HashMap<String, ArrayList<ItemStack[]>> noDrop = new HashMap<String, ArrayList<ItemStack[]>>();
	private HashMap<String, Map<String, Integer>> killers = new HashMap<String, Map<String, Integer>>();

	public PlayerListener(PvPManager plugin) {
		this.plugin = plugin;
		this.pm = plugin.getPm();
		if(Variables.killAbuseEnabled)
			cleanKillers();
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
					} else if (Variables.dropInventory && Variables.dropArmor && !Variables.dropExp)
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
					if (Variables.dropExp)
						pm.fakeExpDrop(player);
				}
				if (Variables.fineEnabled)
					pm.applyFine(player);
			}
			plugin.getCm().untag(player);
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
			plugin.getCm().untag(player);
		}
		if (Variables.killAbuseEnabled && player.getKiller() != null) {
			String killer = player.getKiller().getName();
			if (killers.get(killer) == null) {
				killers.put(killer, new HashMap<String, Integer>());
				killers.get(killer).put(player.getName(), 1);
				if (plugin.getCm().getKillAbusers().containsKey(killer))
					plugin.getCm().getKillAbusers().remove(killer);
			}
			if (killers.get(killer).containsKey(player.getName())) {
				int totalKills = killers.get(killer).get(player.getName());
				if (totalKills >= Variables.killAbuseMaxKills) {
					killers.remove(killer);
					plugin.getCm().getKillAbusers().put(killer, player.getName());
					for (String command : Variables.killAbuseCommands) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", killer));
					}
				} else if (totalKills < Variables.killAbuseMaxKills) {
					killers.get(killer).put(player.getName(), totalKills++);
				}
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
		}
		if (Utils.PMAllowed(player.getWorld().getName())) {
			if (!player.hasPlayedBefore()) {
				plugin.getCm().addNewbie(player);
				if (!Variables.defaultPvp)
					plugin.getCm().disablePvp(player);
			}
			if (player.hasPermission("pvpmanager.nopvp"))
				plugin.getCm().disablePvp(player);
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (noDrop.containsKey(player.getName())) {
			player.getInventory().setContents(noDrop.get(event.getPlayer().getName()).get(0));
			player.getInventory().setArmorContents(noDrop.get(event.getPlayer().getName()).get(1));
			noDrop.remove(player.getName());
		}
	}

	@EventHandler
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		if (Variables.announcePvpOnWorldChange && Variables.pvpTimerEnabled) {
			Player p = event.getPlayer();
			if (plugin.getWtm().isPvpTimerWorld(p.getWorld())) {
				plugin.getWtm().getPvpTimer(p.getWorld()).sendWorldChangeMessage(p);
			}
		}
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		Player p = event.getPlayer();
		if (plugin.getCm().isInCombat(p))
			plugin.getCm().untag(p);
	}
	
	private void cleanKillers(){
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
			@Override
			public void run() {
				killers.clear();
			}	
		}, 60, Variables.killAbuseTime);
	}
}
