package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Utils.Utils;

public class PlayerHandler {

	private HashMap<String, PvPlayer> players = new HashMap<String, PvPlayer>();
	private ConfigManager configManager;
	private PvPManager plugin;
	private Economy economy;
	private Team team;
	private Scoreboard mainScoreboard;

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
		mainScoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
		if (!Variables.nameTagColor.equalsIgnoreCase("none")) {
			if (mainScoreboard.getTeam("InCombat") == null)
				team = mainScoreboard.registerNewTeam("InCombat");
			else
				team = mainScoreboard.getTeam("InCombat");
			team.setPrefix(ChatColor.translateAlternateColorCodes('&', Variables.nameTagColor));
		}else
			team = null;
		PvPlayer.ph = this;
		addOnlinePlayers();
	}

	private void addOnlinePlayers() {
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			add(p);
		}
	}

	public PvPlayer get(Player player) {
		String name = player.getName();
		if (players.containsKey(name))
			return players.get(name);
		else
			return add(player);
	}

	private PvPlayer add(Player player) {
		PvPlayer pvPlayer = new PvPlayer(player, configManager.getUserFile());
		players.put(player.getName(), pvPlayer);
		return pvPlayer;
	}

	public void remove(final PvPlayer player) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BukkitRunnable() {
			public void run() {
				if (player.getPlayer() == null) {
					players.remove(player.getName());
				}
			}
		}, 1800);
		savePvPState(player.getName(), player.hasPvPEnabled());
	}

	public void savePvPState(String name, boolean pvpState) {
		if (!pvpState)
			configManager.saveUser(name, true);
		else
			configManager.saveUser(name, false);
	}

	private void cleanKillersTask() {
		new BukkitRunnable() {
			public void run() {
				for (PvPlayer p : players.values()) {
					p.clearVictims();
				}
			}
		}.runTaskTimer(plugin, 1200, Variables.killAbuseTime * 20);
	}

	public BukkitTask scheduleNewbieTask(final PvPlayer player) {
		return new BukkitRunnable() {
			public void run() {
				player.setNewbie(false);
			}
		}.runTaskLater(plugin, Variables.newbieProtectionTime * 1200);
	}

	public BukkitTask scheduleTagTask(final PvPlayer player) {
		return new BukkitRunnable() {
			public void run() {
				player.setTagged(false);
			}
		}.runTaskLater(plugin, Variables.timeInCombat * 20);
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

	public HashMap<String, PvPlayer> getPlayers() {
		return players;
	}

	public Team getTeam() {
		return team;
	}

	public Scoreboard getMainScoreboard() {
		return mainScoreboard;
	}

}
