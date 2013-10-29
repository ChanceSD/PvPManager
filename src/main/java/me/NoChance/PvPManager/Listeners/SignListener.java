package me.NoChance.PvPManager.Listeners;

import me.NoChance.PvPManager.GlobalManager;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Messages;

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

	private PvPManager plugin;

	public SignListener(GlobalManager globalManager) {
		this.plugin = globalManager;
		globalManager.getServer().getPluginManager().registerEvents(this, globalManager);
	}

	@EventHandler
	public void onSignPlace(SignChangeEvent e) {
		if (e.getPlayer().hasPermission("pvpmanager.sign")) {
			String firstLine = e.getLine(0);
			if (firstLine.equalsIgnoreCase("[PvPManager]")) {
				e.setLine(0, "§5[PvPManager]");
				if (e.getLine(1).isEmpty() && e.getLine(2).isEmpty() && e.getLine(3).isEmpty()) {
					e.setLine(1, "Click This");
					e.setLine(2, "Sign to Change");
					e.setLine(3, "Your PvP Status");
				}
				e.getPlayer().sendMessage(ChatColor.GOLD + "[PvPManager]" + ChatColor.DARK_GREEN + "PvPToggle Sign Created Successfully!");
			}
		}
	}

	@EventHandler
	public void onSignInteract(PlayerInteractEvent e) {
		Block clicked = e.getClickedBlock();
		if (clicked != null) {
			if (clicked.getType().equals(Material.SIGN_POST) || clicked.getType().equals(Material.WALL_SIGN)) {
				Player player = e.getPlayer();
				if (player.hasPermission("pvpmanager.pvpstatus.change")) {
					Sign sign = (Sign) clicked.getState();
					if (sign.getLine(0).equalsIgnoreCase("§5[PvPManager]")) {
						if (plugin.hasPvpEnabled(player.getName())) {
							plugin.playersStatusOff.add(player.getName());
							player.sendMessage(Messages.PvP_Disabled);
						} else {
							plugin.playersStatusOff.remove(player.getName());
							player.sendMessage(Messages.PvP_Enabled);
						}
					}
				} else {
					player.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
				}
			}
		}
	}

}
