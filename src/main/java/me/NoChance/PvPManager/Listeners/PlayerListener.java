package me.NoChance.PvPManager.Listeners;

import java.util.ArrayList;
import java.util.HashMap;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Others.Utils;
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
	private PlayerHandler ph;
	private HashMap<String, ArrayList<ItemStack[]>> noDrop = new HashMap<String, ArrayList<ItemStack[]>>();

	public PlayerListener(PvPManager plugin) {
		this.plugin = plugin;
		this.ph = PlayerHandler.getInstance();
	}

	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		PvPlayer pvPlayer = PlayerHandler.get(player);
		if (pvPlayer.isInCombat()) {
			if (Variables.broadcastPvpLog)
				plugin.getServer().broadcastMessage(Messages.PvPLog_Broadcast.replace("%p", player.getName()));
			if (Variables.punishmentsEnabled) {
				boolean dead = false;
				if (Variables.killOnLogout) {
					pvPlayer.setPvpLogged(true);
					if (!Variables.dropInventory || !Variables.dropArmor) {
						ph.noDropKill(player);
						dead = true;
					}
					if (Variables.dropExp) {
						ph.fakeExpDrop(player);
						if (!dead)
							player.setHealth(0);
					} else if (Variables.dropInventory && Variables.dropArmor && !Variables.dropExp)
						player.setHealth(0);
				} else if (!Variables.killOnLogout) {
					if (Variables.dropInventory) {
						ph.fakeInventoryDrop(player, player.getInventory().getContents());
						player.getInventory().clear();
					}
					if (Variables.dropArmor) {
						ph.fakeInventoryDrop(player, player.getInventory().getArmorContents());
						player.getInventory().setArmorContents(null);
					}
					if (Variables.dropExp)
						ph.fakeExpDrop(player);
				}
				if (Variables.fineEnabled)
					ph.applyFine(player);
			}
			pvPlayer.setTagged(false);
		}
		PlayerHandler.getInstance().remove(player);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		PvPlayer pvPlayer = PlayerHandler.get(player);
		if (pvPlayer.hasPvPLogged()) {
			if (!Variables.dropExp) {
				event.setKeepLevel(true);
				event.setDroppedExp(0);
			}
		}
		if (pvPlayer.isInCombat()) {
			if (player.hasPermission("pvpmanager.nodrop")) {
				ArrayList<ItemStack[]> inv = new ArrayList<ItemStack[]>();
				inv.add(player.getInventory().getContents());
				inv.add(player.getInventory().getArmorContents());
				noDrop.put(player.getName(), inv);
				event.getDrops().clear();
			}
			pvPlayer.setTagged(false);
		}
		if (Variables.killAbuseEnabled && player.getKiller() != null) {
			PvPlayer killer = PlayerHandler.get(player.getKiller());
			killer.addVictim(player.getName());
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PvPlayer pvpPlayer = PlayerHandler.getInstance().add(player);
		if (player.isOp() || player.hasPermission("pvpmanager.admin")) {
			if (Variables.update) {
				Messages.updateMessage(player);
			}
		}
		if (Utils.PMAllowed(player.getWorld().getName())) {
			if (player.hasPermission("pvpmanager.nopvp"))
				pvpPlayer.setPvP(false);
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
		PvPlayer pvPlayer = PlayerHandler.get(p);
		if (pvPlayer.isInCombat())
			pvPlayer.setTagged(false);
		PlayerHandler.getInstance().remove(p);
	}

}
