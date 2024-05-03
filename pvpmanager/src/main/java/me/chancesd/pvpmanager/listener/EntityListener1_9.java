package me.chancesd.pvpmanager.listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.setting.Settings;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.pvpmanager.utils.ScheduleUtils;

public class EntityListener1_9 implements Listener {

	private final PlayerManager ph;
	private final Cache<UUID, Set<AreaEffectCloud>> potionMessageCache = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.SECONDS).build();

	public EntityListener1_9(final PlayerManager ph) {
		this.ph = ph;
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityGlide(final EntityToggleGlideEvent event) {
		if (!Settings.isBlockGlide() || !event.isGliding())
			return;
		final CombatPlayer combatPlayer = ph.get((Player) event.getEntity());
		if (combatPlayer.isInCombat()) {
			combatPlayer.sendActionBar(Messages.getElytraBlockedInCombat(), 1000);
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onLingeringPotionSplash(final AreaEffectCloudApplyEvent event) {
		if (CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()))
			return;
		final AreaEffectCloud areaCloud = event.getEntity();
		final ProjectileSource areaCloudSource = areaCloud.getSource();
		if (event.getAffectedEntities().isEmpty() || !(areaCloudSource instanceof Player))
			return;

		if (!CombatUtils.hasHarmfulPotion(areaCloud))
			return;

		final Player player = (Player) areaCloudSource;
		final List<Entity> toRemove = new ArrayList<>();
		for (final LivingEntity e : event.getAffectedEntities()) {
			if (e.getType() != EntityType.PLAYER || e.equals(player)) {
				continue;
			}
			final Player attacked = (Player) e;
			final ProtectionResult result = ph.checkProtection(player, attacked);

			if (result.isProtected()) {
				toRemove.add(e);
				final Set<AreaEffectCloud> clouds = potionMessageCache.getIfPresent(player.getUniqueId());
				if (clouds == null || !clouds.contains(areaCloud)) {
					Messages.messageProtection(result, player, attacked);
					final Set<AreaEffectCloud> newClouds = new HashSet<>();
					newClouds.add(areaCloud);
					if (clouds != null) {
						newClouds.addAll(clouds);
					}
					potionMessageCache.put(player.getUniqueId(), newClouds);
				}
			}
		}
		event.getAffectedEntities().removeAll(toRemove);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public final void onLingeringPotionSplashMon(final AreaEffectCloudApplyEvent event) {
		if (CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()))
			return;
		final AreaEffectCloud areaCloud = event.getEntity();
		final ProjectileSource areaCloudSource = areaCloud.getSource();
		if (event.getAffectedEntities().isEmpty() || !(areaCloudSource instanceof Player))
			return;

		if (!CombatUtils.hasHarmfulPotion(areaCloud))
			return;

		final Player player = (Player) areaCloudSource;
		for (final LivingEntity e : event.getAffectedEntities()) {
			if (e.getType() != EntityType.PLAYER || e.equals(player)) {
				continue;
			}
			final Player attacked = (Player) e;
			final CancelResult result = ph.tryCancel(player, attacked);

			if (result.canAttack()) {
				ph.getPlugin().getEntityListener().processDamage(player, attacked);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onConsume(final PlayerItemConsumeEvent e) {
		if (!Settings.isRecyclePotionBottles() && !Settings.isRecycleMilkBucket())
			return;

		final Material type = e.getItem().getType();
		if ((type != Material.POTION || !Settings.isRecyclePotionBottles()) && (type != Material.MILK_BUCKET || !Settings.isRecycleMilkBucket()))
			return;
		final Player player = e.getPlayer();
		final PlayerInventory inventory = player.getInventory();
		final int heldSlot = inventory.getHeldItemSlot();
		ScheduleUtils.runPlatformTaskLater(() -> {
			final ItemStack held = inventory.getItem(heldSlot);
			final ItemStack off = inventory.getItemInOffHand();
			if (held != null && (held.getType() == Material.GLASS_BOTTLE || held.getType() == Material.BUCKET))
				held.setAmount(0);
			if (off.getType() == Material.GLASS_BOTTLE || off.getType() == Material.BUCKET)
				off.setAmount(0);
		}, player, 1);
	}

}
