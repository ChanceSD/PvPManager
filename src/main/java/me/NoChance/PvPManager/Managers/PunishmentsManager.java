package me.NoChance.PvPManager.Managers;

import java.util.HashSet;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Utils;
import me.NoChance.PvPManager.Config.Variables;

public class PunishmentsManager {

	private PvPManager plugin;
	private Economy economy;
	private HashSet<String> pvpLogPlayers = new HashSet<String>();

	public PunishmentsManager(PvPManager plugin) {
		this.plugin = plugin;
		if (Variables.fineEnabled) {
			if (Utils.isVaultEnabled()) {
				if (setupEconomy()) {
					plugin.getLogger().info("Vault Found! Using it for fines punishment");
				} else
					plugin.getLogger().severe("Error! No Economy plugin found for fines feature!");
			} else {
				plugin.getLogger().severe("Vault not found! Disabling fines feature...");
				Variables.fineEnabled = false;
			}
		}
	}

	public void fakeInventoryDrop(Player player, ItemStack[] inventory) {
		Location playerLocation = player.getLocation();
		World playerWorld = player.getWorld();
		for (ItemStack itemstack : inventory) {
			if (itemstack != null && !itemstack.getType().equals(Material.AIR))
				playerWorld.dropItemNaturally(playerLocation, itemstack);
		}
	}

	public void fakeExpDrop(Player player) {
		int expdropped = player.getLevel() * 7;
		if (expdropped < 100)
			player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(expdropped);
		else
			player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(100);
		player.setLevel(0);
		player.setExp(0);
	}

	public void noDropKill(Player player) {
		ItemStack[] inventory = new ItemStack[36];
		ItemStack[] armor = null;
		if (!Variables.dropInventory) {
			inventory = player.getInventory().getContents();
			player.getInventory().clear();
		}
		if (!Variables.dropArmor) {
			armor = player.getInventory().getArmorContents();
			player.getInventory().setArmorContents(null);
		}
		player.setHealth(0);
		player.setHealth(20);
		player.getInventory().setContents(inventory);
		player.getInventory().setArmorContents(armor);
	}

	public void applyFine(Player p) {
		if (economy != null) {
			economy.bankWithdraw(p.getName(), Variables.fineAmount);
		} else {
			plugin.getLogger().severe("Tried to apply fine but no Economy plugin found!");
			plugin.getLogger().severe("Disable fines feature or get an Economy plugin to fix this error");
		}
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	public void addPvpLog(Player p) {
		pvpLogPlayers.add(p.getName());
	}

	public boolean pvplogged(Player p) {
		return pvpLogPlayers.contains(p.getName());
	}
}
