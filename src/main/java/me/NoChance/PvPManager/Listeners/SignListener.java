package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.Utils;

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

	private PlayerHandler ph;

	public SignListener(PlayerHandler playerHandler) {
		this.ph = playerHandler;
	}

	@EventHandler
	public void onSignPlace(SignChangeEvent e) {
		Player p = e.getPlayer();
		if (Utils.PMAllowed(p.getWorld().getName())) {
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
	public void onSignInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if (Utils.PMAllowed(player.getWorld().getName())) {
			Block clicked = e.getClickedBlock();
			if (clicked != null) {
				if (clicked.getType().equals(Material.SIGN_POST) || clicked.getType().equals(Material.WALL_SIGN)) {
					Sign sign = (Sign) clicked.getState();
					if (sign.getLine(0).equalsIgnoreCase("§5[PvPManager]")) {
						if (player.hasPermission("pvpmanager.pvpstatus.change")) {
							ph.get(player).togglePvP();
							return;
						} else
							player.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
					}
				}
			}
		}
	}

}
