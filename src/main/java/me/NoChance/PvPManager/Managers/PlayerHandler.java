package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.UUID;
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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Tasks.CleanKillersTask;

public class PlayerHandler {

	private HashMap<String, PvPlayer> players = new HashMap<String, PvPlayer>();
	private ConfigManager configManager;
	private PvPManager plugin;
	private Economy economy;
	private Scoreboard mainScoreboard;

	public PlayerHandler(PvPManager plugin) {
		this.plugin = plugin;
		this.configManager = plugin.getConfigM();
		if (Variables.killAbuseEnabled)
			new CleanKillersTask(this).runTaskTimer(plugin, 1200, Variables.killAbuseTime * 20);
		;
		if (Variables.fineEnabled) {
			if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
				if (setupEconomy()) {
					plugin.getLogger().info("Vault Found! Using it for fines punishment");
				} else
					plugin.getLogger().severe("Error! No Economy plugin found for fines feature!");
			} else {
				plugin.getLogger().severe("Vault not found! Disabling fines feature...");
				Variables.fineEnabled = false;
			}
		}
		setupScoreboardTeam();
		addOnlinePlayers();
	}

	private void addOnlinePlayers() {
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			add(p);
		}
	}

	private void setupScoreboardTeam() {
		Team team = null;
		if (!Variables.nameTagColor.equalsIgnoreCase("none")) {
			mainScoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
			if (mainScoreboard.getTeam("InCombat") == null)
				team = mainScoreboard.registerNewTeam("InCombat");
			else
				team = mainScoreboard.getTeam("InCombat");
			team.setPrefix(ChatColor.translateAlternateColorCodes('&', Variables.nameTagColor));
		}
		PvPlayer.inCombatTeam = team;
	}

	public PvPlayer get(Player player) {
		String name = player.getName();
		if (players.containsKey(name))
			return players.get(name);
		else
			return add(player);
	}

	private PvPlayer add(Player player) {
		PvPlayer pvPlayer = new PvPlayer(player, plugin);
		if (mainScoreboard != null)
			player.setScoreboard(mainScoreboard);
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
		savePvPState(player.getUUID(), player.hasPvPEnabled());
	}

	public void savePvPState(UUID id, boolean pvpState) {
		configManager.saveUser(id, !pvpState);
	}

	private void applyFine(Player p) {
		if (economy != null) {
			economy.withdrawPlayer(p.getName(), Variables.fineAmount);
		} else {
			plugin.getLogger().severe("Tried to apply fine but no Economy plugin found!");
			plugin.getLogger().severe("Disable fines feature or get an Economy plugin to fix this error");
		}
	}

	public void applyPunishments(Player player) {
		PvPlayer pvPlayer = get(player);
		if (Variables.killOnLogout) {
			pvPlayer.setPvpLogged(true);
			ItemStack[] inventory = null;
			ItemStack[] armor = null;
			if (!Variables.dropInventory || !Variables.dropArmor) {
				if (!Variables.dropInventory) {
					inventory = player.getInventory().getContents();
					player.getInventory().clear();
				}
				if (!Variables.dropArmor) {
					armor = player.getInventory().getArmorContents();
					player.getInventory().setArmorContents(null);
				}
			}
			player.setHealth(0);
			player.setHealth(20);
			if (inventory != null)
				player.getInventory().setContents(inventory);
			if (armor != null)
				player.getInventory().setArmorContents(armor);
		} else if (!Variables.killOnLogout) {
			if (Variables.dropInventory) {
				fakeInventoryDrop(player, player.getInventory().getContents());
				player.getInventory().clear();
			}
			if (Variables.dropArmor) {
				fakeInventoryDrop(player, player.getInventory().getArmorContents());
				player.getInventory().setArmorContents(null);
			}
			if (Variables.dropExp)
				fakeExpDrop(player);
		}
		if (Variables.fineEnabled)
			applyFine(player);
	}

	private void fakeInventoryDrop(Player player, ItemStack[] inventory) {
		Location playerLocation = player.getLocation();
		World playerWorld = player.getWorld();
		for (ItemStack itemstack : inventory) {
			if (itemstack != null && !itemstack.getType().equals(Material.AIR))
				playerWorld.dropItemNaturally(playerLocation, itemstack);
		}
	}

	private void fakeExpDrop(Player player) {
		int expdropped = player.getLevel() * 7;
		if (expdropped < 100)
			player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(expdropped);
		else
			player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(100);
		player.setLevel(0);
		player.setExp(0);
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

	public Scoreboard getMainScoreboard() {
		return mainScoreboard;
	}

}
