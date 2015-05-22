package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CombatUtils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {

	private final PlayerHandler ph;

	public SignListener(final PlayerHandler playerHandler) {
		this.ph = playerHandler;
	}

	@EventHandler
	public final void onSignPlace(final SignChangeEvent e) { // NO_UCD (unused code)
		final Player p = e.getPlayer();
		if (CombatUtils.isWorldAllowed(p.getWorld().getName())) {
			if (p.hasPermission("pvpmanager.sign")) {
				if (e.getLine(0).equalsIgnoreCase("[PvPManager]")) {
					e.setLine(0, "§5[PvPManager]");
					if (e.getLine(1).isEmpty() && e.getLine(2).isEmpty() && e.getLine(3).isEmpty()) {
						e.setLine(1, "Click This");
						e.setLine(2, "Sign to Change");
						e.setLine(3, "Your PvP Status");
					}
					p.sendMessage(ChatColor.GOLD + "[PvPManager]" + ChatColor.DARK_GREEN + "PvPToggle Sign Created Successfully!");
				}
			}
		}
	}

	@EventHandler
	public final void onSignInteract(final PlayerInteractEvent e) { // NO_UCD (unused code)
		final Player player = e.getPlayer();
		if (CombatUtils.isWorldAllowed(player.getWorld().getName())) {
			final Block clicked = e.getClickedBlock();
			if (clicked != null) {
				if (clicked.getType().equals(Material.SIGN_POST) || clicked.getType().equals(Material.WALL_SIGN)) {
					final Sign sign = (Sign) clicked.getState();
					if (sign.getLine(0).equalsIgnoreCase("§5[PvPManager]")) {
						if (player.hasPermission("pvpmanager.pvpstatus.change")) {
							ph.get(player).togglePvP();
							return;
						}
						player.sendMessage(Messages.getErrorPermission());
					}
				}
			}
		}
	}

}
