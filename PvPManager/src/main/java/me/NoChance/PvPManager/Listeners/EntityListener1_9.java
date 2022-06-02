package me.NoChance.PvPManager.Listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.potion.PotionEffectType;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Player.CancelResult;
import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;

public class EntityListener1_9 implements Listener {

	private final PlayerHandler ph;
	private final Cache<UUID, Set<AreaEffectCloud>> potionMessageCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build();

	public EntityListener1_9(final PlayerHandler ph) {
		this.ph = ph;
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityGlide(final EntityToggleGlideEvent event) {
		if (!Settings.isBlockGlide() || !event.isGliding())
			return;
		if (ph.get((Player) event.getEntity()).isInCombat()) {
			// TODO add feedback message to player
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public final void onLingeringPotionSplash(final AreaEffectCloudApplyEvent event) {
		if (CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()))
			return;
		final AreaEffectCloud areaCloud = event.getEntity();
		if (event.getAffectedEntities().isEmpty() || !(areaCloud.getSource() instanceof Player))
			return;

		final PotionEffectType potionType = areaCloud.getBasePotionData().getType().getEffectType();
		if (potionType == null || !CombatUtils.isHarmfulPotion(potionType))
			return;

		final Player player = (Player) areaCloud.getSource();
		final List<Entity> toRemove = new ArrayList<>();
		for (final LivingEntity e : event.getAffectedEntities()) {
			if (e.getType() != EntityType.PLAYER || e.equals(player)) {
				continue;
			}
			final Player attacked = (Player) e;
			final CancelResult result = ph.tryCancel(player, attacked);

			if (result != CancelResult.FAIL && result != CancelResult.FAIL_OVERRIDE) {
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
			} else {
				ph.getPlugin().getEntityListener().onDamageActions(player, attacked);
			}
		}
		event.getAffectedEntities().removeAll(toRemove);
	}

}
