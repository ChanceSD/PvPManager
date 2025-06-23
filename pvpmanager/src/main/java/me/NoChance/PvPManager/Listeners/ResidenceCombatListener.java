package me.noobiedoobie.pvpmanager.listener;

import com.bekvon.bukkit.residence.api.ResidenceEnterLeaveEvent;
import com.bekvon.bukkit.residence.api.ResidenceEventType;
import me.noobiedoobie.pvpmanager.PvPManager;
import me.noobiedoobie.pvpmanager.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener to prevent players in combat from entering Residence-protected areas.
 * Requires Residence plugin and PvPManager's PlayerData combat tracking.
 */
public class ResidenceCombatListener implements Listener {

    private final PvPManager plugin;

    public ResidenceCombatListener(PvPManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onResidenceEnter(ResidenceEnterLeaveEvent event) {
        if (event.getEventType() != ResidenceEventType.ENTER) return;

        Player player = Bukkit.getPlayer(event.getPlayer());
        if (player == null) return;

        PlayerData data = plugin.getPlayerHandler().get(player);
        if (data != null && data.isInCombat()) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("cannot_enter_residence_while_in_combat"));
        }
    }
}
