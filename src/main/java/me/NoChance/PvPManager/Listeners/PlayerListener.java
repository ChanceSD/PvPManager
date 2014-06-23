package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.CombatUtils.CancelResult;
import me.libraryaddict.disguise.DisguiseAPI;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class PlayerListener implements Listener {

	private PvPManager plugin;
	private PlayerHandler ph;

	public PlayerListener(PvPManager plugin) {
		this.plugin = plugin;
		this.ph = plugin.getPlayerHandler();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		if (!CombatUtils.isPvP(event) || !CombatUtils.PMAllowed(event.getEntity().getWorld().getName()))
			return;
		Player attacker = getAttacker(event);
		Player attacked = (Player) event.getEntity();
		CancelResult result = CombatUtils.tryCancel(attacker, attacked);

		if (result != CancelResult.FAIL && result != CancelResult.FAIL_OVERRIDE)
			event.setCancelled(true);

		switch (result) {
		case FAIL_OVERRIDE:
			if (event.isCancelled())
				event.setCancelled(false);
		case FAIL:
			if (!event.isCancelled())
				onDamageActions(attacker, attacked);
			break;
		case NEWBIE:
			attacker.sendMessage(Messages.Newbie_Protection_On_Hit);
			break;
		case NEWBIE_OTHER:
			attacker.sendMessage(Messages.Newbie_Protection_Atacker.replace("%p", attacked.getName()));
			break;
		case PVPDISABLED:
			attacker.sendMessage(Messages.Attack_Denied_You);
			break;
		case PVPDISABLED_OTHER:
			attacker.sendMessage(Messages.Attack_Denied_Other.replace("%p", attacked.getName()));
			break;
		default:
			break;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogout(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		PvPlayer pvPlayer = ph.get(player);
		if (pvPlayer.isInCombat()) {
			if (Variables.broadcastPvpLog)
				plugin.getServer().broadcastMessage(Messages.PvPLog_Broadcast.replace("%p", player.getName()));
			if (Variables.punishmentsEnabled)
				ph.applyPunishments(player);

			pvPlayer.setTagged(false);
		}
		ph.remove(pvPlayer);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		PvPlayer pvPlayer = ph.get(player);
		if (pvPlayer.hasPvPLogged() && !Variables.dropExp) {
			event.setKeepLevel(true);
			event.setDroppedExp(0);
		}
		if (pvPlayer.isInCombat())
			pvPlayer.setTagged(false);
		if (player.getKiller() != null && !player.getKiller().hasMetadata("NPC")) {
			if (Variables.killAbuseEnabled) {
				PvPlayer killer = ph.get(player.getKiller());
				killer.addVictim(player.getName());
			}
			if (Variables.playerKillsEnabled) {
				if (Variables.moneyReward > 0)
					ph.giveReward(player);
				if (Variables.commandsOnKillEnabled)
					for (String command : Variables.commandsOnKill) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", pvPlayer.getName()));
					}
			}
		}
		if (Variables.toggleOffOnDeath && player.hasPermission("pvpmanager.pvpstatus.change") && pvPlayer.hasPvPEnabled())
			pvPlayer.setPvP(false);

	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if (CombatUtils.PMAllowed(player.getWorld().getName()) && Variables.autoSoupEnabled) {
			if (player.getHealth() == player.getMaxHealth())
				return;
			if (player.getItemInHand().getType() == Material.MUSHROOM_SOUP) {
				player.setHealth(player.getHealth() + Variables.soupHealth > player.getMaxHealth() ? player.getMaxHealth() : player
						.getHealth() + Variables.soupHealth);
				player.getItemInHand().setType(Material.BOWL);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		ph.get(player);
		if (player.isOp() || player.hasPermission("pvpmanager.admin")) {
			if (Variables.update)
				Messages.updateMessage(player);
			if (Variables.configUpdated)
				Messages.configUpdated(player);
		}
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		PvPlayer pvPlayer = ph.get(event.getPlayer());
		if (pvPlayer.isInCombat())
			pvPlayer.setTagged(false);
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getCause().equals(TeleportCause.ENDER_PEARL)) {
			PvPlayer player = ph.get(event.getPlayer());
			if (Variables.inCombatEnabled && Variables.blockEnderPearl && player.isInCombat()) {
				player.message(Messages.EnderPearl_Blocked_InCombat);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (Variables.stopCommands && Variables.inCombatEnabled) {
			if (plugin.getPlayerHandler().get(event.getPlayer()).isInCombat()) {
				if (!Variables.commandsAllowed.contains(event.getMessage().substring(1).split(" ")[0])) {
					event.setCancelled(true);
					event.getPlayer().sendMessage(Messages.Command_Denied_InCombat);
				}
			}
		}
	}

	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (Variables.respawnProtection != 0) {
			PvPlayer player = ph.get(event.getPlayer());
			player.setRespawnTime(System.currentTimeMillis());
		}
	}

	private void onDamageActions(Player attacker, Player attacked) {
		PvPlayer pvpAttacker = ph.get(attacker);
		PvPlayer pvpAttacked = ph.get(attacked);
		if (Variables.pvpBlood)
			attacked.getWorld().playEffect(attacked.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
		if (!attacker.hasPermission("pvpmanager.nodisable")) {
			if (Variables.disableFly && (attacker.isFlying() || attacker.getAllowFlight()))
				pvpAttacker.disableFly();
			if (Variables.disableGamemode && !attacker.getGameMode().equals(GameMode.SURVIVAL))
				attacker.setGameMode(GameMode.SURVIVAL);
			if (Variables.disableDisguise) {
				if (plugin.getServer().getPluginManager().isPluginEnabled("DisguiseCraft") && DisguiseCraft.getAPI().isDisguised(attacker))
					DisguiseCraft.getAPI().undisguisePlayer(attacker);
				if (plugin.getServer().getPluginManager().isPluginEnabled("LibsDisguises") && DisguiseAPI.isDisguised(attacker))
					DisguiseAPI.undisguiseToAll(attacker);
			}
		}
		if (Variables.inCombatEnabled) {
			if (Variables.onlyTagAttacker) {
				pvpAttacker.setTagged(true);
				return;
			} else {
				pvpAttacker.setTagged(true);
				pvpAttacked.setTagged(true);
			}
		}
	}

	private Player getAttacker(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Projectile)
			return (Player) ((Projectile) event.getDamager()).getShooter();
		else
			return (Player) event.getDamager();
	}
}
