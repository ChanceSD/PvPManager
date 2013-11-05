package me.NoChance.PvPManager.Managers;

import java.util.HashSet;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Variables;

public class PunishmentsManager {

	private PvPManager plugin;
	private Economy vault;
	private boolean vaultEnabled = false;
	private HashSet<String> pvpLogPlayers = new HashSet<String>();

	public PunishmentsManager(PvPManager plugin) {
		this.plugin = plugin;
		if (Variables.fineEnabled) {
			if (setupEconomy()){
				vaultEnabled = true;
				plugin.getLogger().info("Vault Found! Using it for fines punishment");
			}
			else
				plugin.getLogger().severe("Error! Need Vault for fines feature!");
		}
	}

	public void fakeInventoryDrop(Player player, ItemStack[] inventory) {
		Location playerLocation = player.getLocation();
		World playerWorld = player.getWorld();
		for (ItemStack itemstack : inventory) {
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
		ItemStack[] inventory = null;
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
		if (vaultEnabled) {
			vault.bankWithdraw(p.getName(), Variables.fineAmount);
		}
		plugin.getLogger().severe("Tried to apply fine but Vault not detected! " +
				"Disable fines feature or get Vault to fix this error.");
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			vault = economyProvider.getProvider();
		}
		return (vault != null);
	}
	
	public void addPvpLog(Player p){
		pvpLogPlayers.add(p.getName());
	}
	
	public boolean pvplogged(Player p){
		return pvpLogPlayers.contains(p.getName());
	}
}
