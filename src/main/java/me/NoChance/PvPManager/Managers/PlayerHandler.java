package me.NoChance.PvPManager.Managers;

import java.util.HashSet;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Utils.Utils;

public class PlayerHandler {

	private HashSet<PvPlayer> players = new HashSet<PvPlayer>();
	private ConfigManager configManager;
	private PvPManager plugin;
	private Economy economy;

	public PlayerHandler(PvPManager plugin) {
		this.plugin = plugin;
		this.configManager = plugin.getConfigM();
		if (Variables.killAbuseEnabled)
			cleanKillersTask();
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
		for (Player p : Bukkit.getOnlinePlayers()) {
			add(p);
		}
	}

	public PvPlayer get(Player player) {
		String name = player.getName();
		for (PvPlayer p : players) {
			if (p.getName().equals(name))
				return p;
		}
		return null;
	}

	public PvPlayer add(Player player) {
		PvPlayer pvPlayer = new PvPlayer(player, configManager.getUserFile());
		players.add(pvPlayer);
		return pvPlayer;
	}

	public void remove(PvPlayer player) {
		players.remove(player);
		savePvPState(player.getName(), player.hasPvPEnabled());
	}

	public void savePvPState(String name, boolean pvpState) {
		if (!pvpState)
			configManager.saveUser(name, true);
		else
			configManager.saveUser(name, false);
	}

	private void cleanKillersTask() {
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				for (PvPlayer p : players) {
					p.clearVictims();
				}
			}
		}, 1200, Variables.killAbuseTime * 20);
	}

	public static int scheduleNewbieTimer(final PvPlayer player) {
		PvPManager plugin = (PvPManager) Bukkit.getPluginManager().getPlugin("PvPManager");
		return plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				player.setNewbie(false);
			}
		}, Variables.newbieProtectionTime * 1200);
	}

	public static int scheduleTagTimer(final PvPlayer player) {
		PvPManager plugin = (PvPManager) Bukkit.getPluginManager().getPlugin("PvPManager");
		return plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				player.setTagged(false);
			}
		}, Variables.timeInCombat * 20);
	}

	public void applyFine(Player p) {
		if (economy != null) {
			economy.withdrawPlayer(p.getName(), Variables.fineAmount);
		} else {
			plugin.getLogger().severe("Tried to apply fine but no Economy plugin found!");
			plugin.getLogger().severe("Disable fines feature or get an Economy plugin to fix this error");
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

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	public HashSet<PvPlayer> getPlayers() {
		return players;
	}

}
