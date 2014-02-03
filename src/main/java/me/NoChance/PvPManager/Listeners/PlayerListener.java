package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PlayerListener implements Listener {

	private PvPManager plugin;
	private PlayerHandler ph;

	public PlayerListener(PvPManager plugin) {
		this.plugin = plugin;
		this.ph = plugin.getPlayerHandler();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogout(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		PvPlayer pvPlayer = ph.get(player);
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
		ph.remove(pvPlayer);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		PvPlayer pvPlayer = ph.get(player);
		if (pvPlayer.hasPvPLogged()) {
			if (!Variables.dropExp) {
				event.setKeepLevel(true);
				event.setDroppedExp(0);
			}
		}
		if (pvPlayer.isInCombat())
			pvPlayer.setTagged(false);
		if (Variables.killAbuseEnabled && player.getKiller() != null) {
			PvPlayer killer = ph.get(player.getKiller());
			killer.addVictim(player.getName());
		}
		if (Variables.toggleOffOnDeath && player.hasPermission("pvpmanager.pvpstatus.change") && pvPlayer.hasPvPEnabled())
			pvPlayer.setPvP(false);

	}

	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PvPlayer pvpPlayer = ph.add(player);
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
		PvPlayer pvPlayer = ph.get(p);
		if (pvPlayer.isInCombat())
			pvPlayer.setTagged(false);
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getPlayer().hasMetadata("NPC"))
			return;
		PvPlayer player = ph.get(event.getPlayer());
		if (Variables.inCombatEnabled && Variables.blockEnderPearl) {
			if (player.isInCombat() && event.getCause().equals(TeleportCause.ENDER_PEARL)) {
				player.message(Messages.EnderPearl_Blocked_InCombat);
				event.setCancelled(true);
			}
		}

	}

}
