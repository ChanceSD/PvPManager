package me.NoChance.PvPManager.Listeners;

import java.util.HashMap;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Config.Variables.DropMode;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CancelResult;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.libraryaddict.disguise.DisguiseAPI;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;

public class PlayerListener implements Listener {

	private final PvPManager plugin;
	private final PlayerHandler ph;

	public PlayerListener(final PvPManager plugin) {
		this.plugin = plugin;
		this.ph = plugin.getPlayerHandler();
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public final void onPlayerDamage(final EntityDamageByEntityEvent event) { // NO_UCD
		if (!CombatUtils.isWorldAllowed(event.getEntity().getWorld().getName()))
			return;
		if (Variables.isNewbieGodMode() && event.getEntity() instanceof Player && ph.get((Player) event.getEntity()).isNewbie()) {
			event.setCancelled(true);
			return;
		}
		if (!CombatUtils.isPvP(event))
			return;
		final Player attacker = getAttacker(event);
		final Player attacked = (Player) event.getEntity();
		final CancelResult result = ph.tryCancel(attacker, attacked);

		if (result != CancelResult.FAIL && result != CancelResult.FAIL_OVERRIDE)
			event.setCancelled(true);

		switch (result) {
		case FAIL_OVERRIDE:
		case FAIL:
			break;
		case NEWBIE:
			attacker.sendMessage(Messages.getNewbieProtectionOnHit());
			break;
		case NEWBIE_OTHER:
			attacker.sendMessage(Messages.getNewbieProtectionAtacker().replace("%p", attacked.getName()));
			break;
		case PVPDISABLED:
			attacker.sendMessage(Messages.getAttackDeniedYou());
			break;
		case PVPDISABLED_OTHER:
			attacker.sendMessage(Messages.getAttackDeniedOther().replace("%p", attacked.getName()));
			break;
		default:
			break;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerDamageOverride(final EntityDamageByEntityEvent event) { // NO_UCD
		if (!CombatUtils.isPvP(event) || !CombatUtils.isWorldAllowed(event.getEntity().getWorld().getName()) || !event.isCancelled())
			return;

		if (ph.tryCancel(getAttacker(event), (Player) event.getEntity()).equals(CancelResult.FAIL_OVERRIDE))
			event.setCancelled(false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public final void onPlayerDamageMonitor(final EntityDamageByEntityEvent event) { // NO_UCD
		if (!CombatUtils.isPvP(event) || !CombatUtils.isWorldAllowed(event.getEntity().getWorld().getName()))
			return;
		final Player attacker = getAttacker(event);
		final Player attacked = (Player) event.getEntity();

		final CancelResult result = ph.tryCancel(attacker, attacked);

		if (result == CancelResult.FAIL || result == CancelResult.FAIL_OVERRIDE)
			onDamageActions(attacker, attacked);
	}

	@EventHandler
	public final void onPotionSplash(final PotionSplashEvent event) { // NO_UCD (unused code)
		final ThrownPotion potion = event.getPotion();
		if (event.getAffectedEntities().isEmpty() || !(potion.getShooter() instanceof Player))
			return;

		for (final PotionEffect effect : potion.getEffects()) {
			if (effect.getType().equals(PotionEffectType.POISON)) {
				for (final LivingEntity e : event.getAffectedEntities()) {
					if (e instanceof Player && !ph.get((Player) e).hasPvPEnabled()) {
						event.setIntensity(e, 0);
					}
				}
				return;
			}
		}
	}

	@EventHandler
	public final void onBlockPlace(final BlockPlaceEvent event) { // NO_UCD
		if (Variables.isBlockPlaceBlocks() && ph.get(event.getPlayer()).isInCombat())
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public final void onPlayerLogout(final PlayerQuitEvent event) { // NO_UCD
		final Player player = event.getPlayer();
		final PvPlayer pvPlayer = ph.get(player);
		if (pvPlayer == null)
			return;
		if (pvPlayer.isInCombat()) {
			if (Variables.isLogToFile())
				plugin.getLog().log(Messages.getPvplogBroadcast().replace("%p", player.getName()));
			if (Variables.isBroadcastPvpLog())
				Bukkit.broadcastMessage(Messages.getPvplogBroadcast().replace("%p", player.getName()));
			if (Variables.isPunishmentsEnabled())
				ph.applyPunishments(pvPlayer);
		}
		ph.remove(pvPlayer);
	}

	@SuppressWarnings("null")
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerDeath(final PlayerDeathEvent event) { // NO_UCD (unused code)
		final Player player = event.getEntity();
		if (!CombatUtils.isWorldAllowed(player.getWorld().getName()))
			return;
		final PvPlayer pvPlayer = ph.get(player);
		if (pvPlayer == null)
			return;
		if (pvPlayer.isInCombat())
			ph.untag(pvPlayer);

		// Let's process player's inventory/exp according to config file
		if (pvPlayer.hasPvPLogged()) {
			if (!Variables.isDropExp()) {
				event.setKeepLevel(true);
				event.setDroppedExp(0);
			}
			if (!Variables.isDropInventory() && Variables.isDropArmor()) {
				CombatUtils.fakeItemStackDrop(player, player.getInventory().getArmorContents());
				player.getInventory().setArmorContents(null);
			} else if (Variables.isDropInventory() && !Variables.isDropArmor()) {
				CombatUtils.fakeItemStackDrop(player, player.getInventory().getContents());
				player.getInventory().clear();
			}
			if (!Variables.isDropInventory() || !Variables.isDropArmor())
				event.setKeepInventory(true);
		}

		final Player killer = player.getKiller();
		boolean pvpDeath = killer != null ? true : false;
		// Player died in combat, process that
		if (pvpDeath && !killer.equals(player)) {
			final PvPlayer pKiller = ph.get(killer);
			if (pKiller == null)
				return;
			if (Variables.isKillAbuseEnabled())
				pKiller.addVictim(player.getName());
			if (Variables.getMoneyReward() > 0)
				pKiller.giveReward(player);
			if (Variables.getMoneyPenalty() > 0)
				pvPlayer.applyPenalty();
			if (Variables.isCommandsOnKillEnabled())
				for (final String command : Variables.getCommandsOnKill()) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", killer.getName()).replace("<victim>", player.getName()));
				}
		}
		if (!pvPlayer.hasPvPLogged()) {
			DropMode mode = Variables.getDropMode();
			switch (mode) {
			case DROP:
				if (!pvpDeath)
					event.setKeepInventory(true);
				break;
			case KEEP:
				if (pvpDeath)
					event.setKeepInventory(true);
				break;
			case TRANSFER:
				if (pvpDeath) {
					ItemStack[] drops = event.getDrops().toArray(new ItemStack[event.getDrops().size()]);
					HashMap<Integer, ItemStack> returned = killer.getInventory().addItem(drops);
					CombatUtils.fakeItemStackDrop(player, returned.values().toArray(new ItemStack[returned.values().size()]));
					event.getDrops().clear();
				}
				break;
			default:
				break;
			}
		}
	}

	@EventHandler
	public final void onPlayerInteract(final PlayerInteractEvent e) { // NO_UCD
		final Player player = e.getPlayer();
		if (CombatUtils.isWorldAllowed(player.getWorld().getName())) {
			final ItemStack i = player.getItemInHand();
			if (Variables.isAutoSoupEnabled() && i.getType() == Material.MUSHROOM_SOUP) {
				if (player.getHealth() == player.getMaxHealth())
					return;
				player.setHealth(player.getHealth() + Variables.getSoupHealth() > player.getMaxHealth() ? player.getMaxHealth() : player.getHealth()
						+ Variables.getSoupHealth());
				i.setType(Material.BOWL);
				return;
			}
			final PvPlayer pvplayer = ph.get(player);
			if (pvplayer == null)
				return;
			if ((i.getType().equals(Material.FLINT_AND_STEEL) || i.getType().equals(Material.LAVA_BUCKET)) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				for (final Player p : e.getClickedBlock().getWorld().getPlayers()) {
					if (e.getPlayer().equals(p))
						continue;
					final PvPlayer target = ph.get(p);
					if (target == null)
						continue;
					if ((!target.hasPvPEnabled() || !pvplayer.hasPvPEnabled()) && e.getClickedBlock().getLocation().distanceSquared(p.getLocation()) < 9) {
						pvplayer.message(Messages.getAttackDeniedOther().replace("%p", target.getName()));
						e.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public final void onPlayerPickup(final PlayerPickupItemEvent e) { // NO_UCD
		if (Variables.isNewbieProtectionEnabled() && Variables.isBlockPickNewbies()) {
			final PvPlayer player = ph.get(e.getPlayer());
			if (player.isNewbie())
				e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerJoin(final PlayerJoinEvent event) { // NO_UCD (unused code)
		final Player player = event.getPlayer();
		ph.get(player);
		if (player.isOp() || player.hasPermission("pvpmanager.admin")) {
			if (Variables.isUpdate())
				Messages.updateMessage(player);
			if (Variables.isConfigUpdated())
				Messages.configUpdated(player);
		}
	}

	@EventHandler
	public final void onPlayerKick(final PlayerKickEvent event) { // NO_UCD
		final PvPlayer pvPlayer = ph.get(event.getPlayer());
		if (pvPlayer == null)
			return;
		if (pvPlayer.isInCombat() && !event.getReason().equalsIgnoreCase("Illegal characters in chat"))
			ph.untag(pvPlayer);
	}

	@EventHandler
	public final void onPlayerTeleport(final PlayerTeleportEvent event) { // NO_UCD
		if (event.getCause().equals(TeleportCause.ENDER_PEARL)) {
			final PvPlayer player = ph.get(event.getPlayer());
			if (player == null)
				return;
			if (Variables.isInCombatEnabled() && Variables.isBlockEnderPearl() && player.isInCombat()) {
				player.message(Messages.getEnderpearlBlockedIncombat());
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onCommand(final PlayerCommandPreprocessEvent event) { // NO_UCD
		if (Variables.isStopCommands() && Variables.isInCombatEnabled()) {
			if (plugin.getPlayerHandler().get(event.getPlayer()).isInCombat()) {
				final boolean contains = Variables.getCommandsAllowed().contains(event.getMessage().substring(1).split(" ")[0]);
				if (Variables.isCommandsWhitelist() ? !contains : contains) {
					event.setCancelled(true);
					event.getPlayer().sendMessage(Messages.getCommandDeniedIncombat());
				}
			}
		}
	}

	@EventHandler
	public final void onPlayerRespawn(final PlayerRespawnEvent event) { // NO_UCD
		if (CombatUtils.isWorldAllowed(event.getPlayer().getWorld().getName())) {
			if (Variables.isKillAbuseEnabled() && Variables.getRespawnProtection() != 0) {
				final PvPlayer player = ph.get(event.getPlayer());
				if (player == null)
					return;
				player.setRespawnTime(System.currentTimeMillis());
			}
		}
	}

	private void onDamageActions(final Player attacker, final Player defender) {
		final PvPlayer pvpAttacker = ph.get(attacker);
		final PvPlayer pvpDefender = ph.get(defender);
		if (pvpAttacker == null || pvpDefender == null)
			return;
		if (Variables.isPvpBlood())
			defender.getWorld().playEffect(defender.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		if (!attacker.hasPermission("pvpmanager.nodisable")) {
			if (Variables.isDisableFly()) {
				if (attacker.isFlying() || attacker.getAllowFlight())
					pvpAttacker.disableFly();
				if (defender.isFlying() || defender.getAllowFlight())
					pvpDefender.disableFly();
			}
			if (Variables.isDisableGamemode() && !attacker.getGameMode().equals(GameMode.SURVIVAL))
				attacker.setGameMode(GameMode.SURVIVAL);
			if (Variables.isDisableDisguise()) {
				if (Bukkit.getPluginManager().isPluginEnabled("DisguiseCraft") && DisguiseCraft.getAPI().isDisguised(attacker))
					DisguiseCraft.getAPI().undisguisePlayer(attacker);
				if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises") && DisguiseAPI.isDisguised(attacker))
					DisguiseAPI.undisguiseToAll(attacker);
			}
			if (Variables.isDisableInvisibility() && attacker.hasPotionEffect(PotionEffectType.INVISIBILITY))
				attacker.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
		if (Variables.isInCombatEnabled()) {
			if (Variables.isOnlyTagAttacker()) {
				pvpAttacker.setTagged(true, pvpDefender.getName());
				return;
			}
			pvpAttacker.setTagged(true, pvpDefender.getName());
			pvpDefender.setTagged(false, pvpAttacker.getName());
		}
	}

	private Player getAttacker(final EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Projectile)
			return (Player) ((Projectile) event.getDamager()).getShooter();
		return (Player) event.getDamager();
	}
}
