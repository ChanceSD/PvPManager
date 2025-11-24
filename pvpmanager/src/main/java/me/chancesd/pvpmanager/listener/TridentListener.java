package me.chancesd.pvpmanager.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.ItemStack;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.scheduler.SDTask;
import me.chancesd.sdutils.scheduler.ScheduleUtils;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TridentListener implements Listener {
    private final PlayerManager playerManager;

    // Track players with active trident countdown displays to avoid duplicates
    private final Map<UUID, SDTask> tridentCountdownTasks = new ConcurrentHashMap<>();

    // Track thrown tridents to their player owners
    private final Map<Integer, UUID> activeTridents = new ConcurrentHashMap<>();

    // Store original locations for riptide rollback
    private final Map<UUID, Location> riptideOriginalLocations = new ConcurrentHashMap<>();

    // Trident cooldown tracking (UUID -> expiration time in millis)
    private final Map<UUID, Long> tridentCooldowns = new ConcurrentHashMap<>();

    // Configuration values
    private static final int TRIDENT_COOLDOWN_SECONDS = 10; // Default cooldown time
    private static final long UPDATE_INTERVAL = 20L; // Update every second (20 ticks)

    public TridentListener(final PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    private boolean isTridentBanned(Player player) {
        // Check if trident usage is disabled in this world
        return CombatUtils.isWorldExcluded(player.getWorld().getName());
    }

    private boolean isTridentOnCooldown(Player player) {
        Long cooldownEnd = tridentCooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) {
            return false;
        }
        if (System.currentTimeMillis() >= cooldownEnd) {
            tridentCooldowns.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    private void setTridentCooldown(Player player) {
        long cooldownEnd = System.currentTimeMillis() + (TRIDENT_COOLDOWN_SECONDS * 1000L);
        tridentCooldowns.put(player.getUniqueId(), cooldownEnd);
    }

    private int getRemainingTridentCooldown(Player player) {
        Long cooldownEnd = tridentCooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) {
            return 0;
        }
        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000L;
        return (int) Math.max(0, remaining);
    }

    private void refreshCombatOnTridentLand(Player player) {
        // Tag the player in combat when trident lands
        CombatPlayer combatPlayer = playerManager.get(player);
        if (combatPlayer != null && Conf.TIME_IN_COMBAT.asInt() > 0) {
            combatPlayer.tag(null, UntagReason.EXPIRE);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTridentUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();

        // Check if player is right-clicking with a trident
        if (item != null && item.getType() == Material.TRIDENT &&
                (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {

            // Check if trident usage is banned in this world
            if (isTridentBanned(player)) {
                event.setCancelled(true);
                sendBannedMessage(player);
                return;
            }

            // Handle riptide tridents differently - we need to prevent the interaction entirely
            if (item.containsEnchantment(Enchantment.RIPTIDE)) {
                if (isTridentOnCooldown(player)) {
                    event.setCancelled(true);
                    sendCooldownMessage(player);
                    return;
                } else {
                    // Store the player's location before riptide for potential rollback
                    riptideOriginalLocations.put(player.getUniqueId(), player.getLocation().clone());
                }
            } else {
                // Handle non-riptide tridents
                if (isTridentOnCooldown(player)) {
                    event.setCancelled(true);
                    sendCooldownMessage(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRiptideUse(PlayerRiptideEvent event) {
        Player player = event.getPlayer();

        // Check if trident usage is banned in this world
        if (isTridentBanned(player)) {
            sendBannedMessage(player);
            rollbackRiptide(player);
            return;
        }

        // Check if trident is on cooldown
        if (isTridentOnCooldown(player)) {
            sendCooldownMessage(player);
            rollbackRiptide(player);
            return;
        }

        // Set cooldown for riptide usage
        setTridentCooldown(player);

        // Start displaying the countdown
        startTridentCountdown(player);

        // Refresh combat on riptide usage if enabled
        refreshCombatOnTridentLand(player);

        // Clean up the stored location
        riptideOriginalLocations.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Trident && event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();

            // Check if trident usage is banned in this world
            if (isTridentBanned(player)) {
                event.setCancelled(true);
                sendBannedMessage(player);
                return;
            }

            // Check if trident is on cooldown
            if (isTridentOnCooldown(player)) {
                event.setCancelled(true);
                sendCooldownMessage(player);
            } else {
                // Set cooldown when player successfully launches a trident (non-riptide)
                setTridentCooldown(player);

                // Start displaying the countdown for trident cooldown
                startTridentCountdown(player);

                // Track this trident to the player for the hit event
                activeTridents.put(event.getEntity().getEntityId(), player.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Trident) {
            // Get the trident's entity ID
            int tridentId = event.getEntity().getEntityId();

            // Check if we're tracking this trident
            if (activeTridents.containsKey(tridentId)) {
                UUID playerUUID = activeTridents.remove(tridentId);
                Player player = playerManager.getPlugin().getServer().getPlayer(playerUUID);

                if (player != null && player.isOnline()) {
                    // Trident landed, refresh combat if enabled
                    refreshCombatOnTridentLand(player);
                }
            }
        }
    }

    private void rollbackRiptide(Player player) {
        Location originalLocation = riptideOriginalLocations.remove(player.getUniqueId());

        if (originalLocation != null) {
            // Counter the velocity after a short delay
            ScheduleUtils.runLater(() -> {
                if (player.isOnline()) {
                    // Stop any remaining velocity
                    player.setVelocity(player.getVelocity().multiply(0));

                    // Ensure they're at the original location
                    if (player.getLocation().distance(originalLocation) > 5) {
                        player.teleport(originalLocation);
                    }
                }
            }, 2L, TimeUnit.MILLISECONDS, player);
        } else {
            // Fallback: just stop their velocity
            ScheduleUtils.ensureMainThread(() -> {
                player.setVelocity(player.getVelocity().multiply(0));
            }, player);
        }
    }

    /**
     * Starts a separate countdown task for trident cooldown display.
     * This ensures the countdown is shown regardless of combat status.
     */
    private void startTridentCountdown(Player player) {
        if (player == null) return;

        UUID playerUUID = player.getUniqueId();

        // Cancel any existing countdown task for this player
        SDTask existingTask = tridentCountdownTasks.get(playerUUID);
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Create a new countdown task
        SDTask task = ScheduleUtils.runTimer(() -> {
            // Check if player is still online
            if (!player.isOnline()) {
                cancelTridentCountdown(playerUUID);
                return;
            }

            // Check if cooldown is still active
            if (!isTridentOnCooldown(player)) {
                cancelTridentCountdown(playerUUID);
                return;
            }

            // Get remaining time
            int remainingTime = getRemainingTridentCooldown(player);

            // Send cooldown message
            CombatPlayer combatPlayer = playerManager.get(player);
            if (combatPlayer != null && !combatPlayer.isInCombat()) {
                // Only show trident cooldown if not in combat
                player.sendMessage(ChatColor.RED + "Trident cooldown: " + remainingTime + "s");
            }

        }, 0L, UPDATE_INTERVAL, TimeUnit.MILLISECONDS, player);

        // Store the task
        tridentCountdownTasks.put(playerUUID, task);
    }

    /**
     * Cancels and removes the trident countdown task for a player.
     */
    private void cancelTridentCountdown(UUID playerUUID) {
        SDTask task = tridentCountdownTasks.remove(playerUUID);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Helper method to send banned message
     */
    private void sendBannedMessage(Player player) {
        player.sendMessage(ChatColor.RED + "Tridents are disabled in this world!");
    }

    /**
     * Helper method to send cooldown message
     */
    private void sendCooldownMessage(Player player) {
        int remainingTime = getRemainingTridentCooldown(player);
        player.sendMessage(ChatColor.RED + "Trident on cooldown! Wait " + remainingTime + " seconds.");
    }

    /**
     * Cleanup method to cancel all tasks when the plugin is disabled.
     * Call this from your main plugin's onDisable method.
     */
    public void shutdown() {
        tridentCountdownTasks.values().forEach(SDTask::cancel);
        tridentCountdownTasks.clear();
        activeTridents.clear();
        riptideOriginalLocations.clear();
        tridentCooldowns.clear();
    }
}