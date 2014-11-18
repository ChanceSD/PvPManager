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
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class PlayerListener implements Listener {

	private PvPManager plugin;
	private PlayerHandler ph;

	public PlayerListener(PvPManager plugin) {
		this.plugin = plugin;
		this.ph = plugin.getPlayerHandler();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
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
		if (pvPlayer == null)
			return;
		if (pvPlayer.isInCombat()) {
			if (Variables.logToFile)
				plugin.getLog().log(Messages.PvPLog_Broadcast.replace("%p", player.getName()));
			if (Variables.broadcastPvpLog)
				plugin.getServer().broadcastMessage(Messages.PvPLog_Broadcast.replace("%p", player.getName()));
			if (Variables.punishmentsEnabled)
				ph.applyPunishments(player);
			pvPlayer.unTag();
		}
		ph.remove(pvPlayer);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (player.hasMetadata("NPC"))
			return;
		PvPlayer pvPlayer = ph.get(player);
		if (pvPlayer == null)
			return;
		if (pvPlayer.hasPvPLogged() && !Variables.dropExp) {
			event.setKeepLevel(true);
			event.setDroppedExp(0);
		}
		if (pvPlayer.isInCombat())
			pvPlayer.unTag();
		Player killer = player.getKiller();
		if (killer != null && !killer.hasMetadata("NPC")) {
			if (Variables.killAbuseEnabled)
				ph.get(killer).addVictim(player.getName());
			if (Variables.moneyReward > 0)
				ph.giveReward(killer, player);
			if (Variables.commandsOnKillEnabled)
				for (String command : Variables.commandsOnKill) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", killer.getName()));
				}
			if (Variables.moneyPenalty > 0)
				ph.applyPenalty(player);
			if (Variables.transferDrops) {
				for (ItemStack s : killer.getInventory().addItem(event.getDrops().toArray(new ItemStack[event.getDrops().size()])).values()) {
					player.getWorld().dropItem(player.getLocation(), s);
				}
				event.getDrops().clear();
			}
		}
		if (Variables.toggleOffOnDeath && player.hasPermission("pvpmanager.pvpstatus.change") && pvPlayer.hasPvPEnabled())
			pvPlayer.setPvP(false);

	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if (CombatUtils.PMAllowed(player.getWorld().getName())) {
			ItemStack i = player.getItemInHand();
			if (Variables.autoSoupEnabled && i.getType() == Material.MUSHROOM_SOUP) {
				if (player.getHealth() == player.getMaxHealth())
					return;
				player.setHealth(player.getHealth() + Variables.soupHealth > player.getMaxHealth() ? player.getMaxHealth() : player.getHealth()
						+ Variables.soupHealth);
				i.setType(Material.BOWL);
				return;
			}
			PvPlayer pvplayer = ph.get(player);
			if (pvplayer == null)
				return;
			if (i.getType().equals(Material.FLINT_AND_STEEL) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				for (Player p : e.getClickedBlock().getWorld().getPlayers()) {
					if (e.getPlayer().equals(p))
						continue;
					if ((!ph.get(p).hasPvPEnabled() || !pvplayer.hasPvPEnabled()) && e.getClickedBlock().getLocation().distanceSquared(p.getLocation()) < 9) {
						pvplayer.message("§cNope! PvP Disabled!");
						e.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent e) {
		PvPlayer player = ph.get(e.getPlayer());
		if (player == null)
			return;
		if (CombatUtils.PMAllowed(player.getWorldName()) && e.getBucket().equals(Material.LAVA_BUCKET)) {
			for (Player p : e.getBlockClicked().getWorld().getPlayers()) {
				if (e.getPlayer().equals(p))
					continue;
				if ((!ph.get(p).hasPvPEnabled() || !player.hasPvPEnabled()) && e.getBlockClicked().getLocation().distanceSquared(p.getLocation()) < 9) {
					player.message("§cNope! PvP Disabled!");
					e.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onPlayerPickup(PlayerPickupItemEvent e) {
		if (Variables.blockPickNewbies) {
			PvPlayer player = ph.get(e.getPlayer());
			if (player.isNewbie())
				e.setCancelled(true);
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
		if (pvPlayer == null)
			return;
		if (pvPlayer.isInCombat() && !event.getReason().equalsIgnoreCase("Illegal characters in chat"))
			pvPlayer.unTag();
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getCause().equals(TeleportCause.ENDER_PEARL)) {
			PvPlayer player = ph.get(event.getPlayer());
			if (player == null)
				return;
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

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (Variables.killAbuseEnabled && Variables.respawnProtection != 0) {
			PvPlayer player = ph.get(event.getPlayer());
			if (player == null)
				return;
			player.setRespawnTime(System.currentTimeMillis());
		}
	}

	private void onDamageActions(Player attacker, Player defender) {
		PvPlayer pvpAttacker = ph.get(attacker);
		PvPlayer pvpDefender = ph.get(defender);
		if (pvpAttacker == null || pvpDefender == null)
			return;
		if (Variables.pvpBlood)
			defender.getWorld().playEffect(defender.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
		if (!attacker.hasPermission("pvpmanager.nodisable")) {
			if (Variables.disableFly && (attacker.isFlying() || attacker.getAllowFlight())) {
				pvpAttacker.disableFly();
				if (defender.isFlying() || defender.getAllowFlight())
					pvpDefender.disableFly();
			}
			if (Variables.disableGamemode && !attacker.getGameMode().equals(GameMode.SURVIVAL))
				attacker.setGameMode(GameMode.SURVIVAL);
			if (Variables.disableDisguise) {
				if (plugin.getServer().getPluginManager().isPluginEnabled("DisguiseCraft") && DisguiseCraft.getAPI().isDisguised(attacker))
					DisguiseCraft.getAPI().undisguisePlayer(attacker);
				if (plugin.getServer().getPluginManager().isPluginEnabled("LibsDisguises") && DisguiseAPI.isDisguised(attacker))
					DisguiseAPI.undisguiseToAll(attacker);
			}
			if (Variables.disableInvisibility && attacker.hasPotionEffect(PotionEffectType.INVISIBILITY))
				attacker.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
		if (Variables.inCombatEnabled) {
			if (Variables.onlyTagAttacker) {
				pvpAttacker.setTagged(true, pvpDefender.getName());
				return;
			} else {
				pvpAttacker.setTagged(true, pvpDefender.getName());
				pvpDefender.setTagged(false, pvpAttacker.getName());
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
