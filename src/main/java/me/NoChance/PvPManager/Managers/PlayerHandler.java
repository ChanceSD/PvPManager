package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.UUID;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.TeamProfile;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Tasks.CleanKillersTask;
import me.NoChance.PvPManager.Tasks.TagTask;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerHandler {

	private HashMap<String, PvPlayer> players = new HashMap<String, PvPlayer>();
	private ConfigManager configManager;
	private PvPManager plugin;
	private Economy economy;
	private TagTask tagTask = new TagTask();

	public PlayerHandler(PvPManager plugin) {
		this.plugin = plugin;
		this.configManager = plugin.getConfigM();
		if (Variables.killAbuseEnabled)
			new CleanKillersTask(this).runTaskTimer(plugin, 1200, Variables.killAbuseTime * 20);
		if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
			if (setupEconomy()) {
				plugin.getLogger().info("Vault Found! Using it for currency related features");
			} else
				plugin.getLogger().severe("Error! No Economy plugin found");
		} else {
			plugin.getLogger().severe("Vault not found! Features requiring Vault won't work!");
			Variables.fineEnabled = false;
		}
		addOnlinePlayers();
		tagTask.runTaskTimerAsynchronously(plugin, 20, 20);
	}

	private void addOnlinePlayers() {
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			add(p);
		}
	}

	public PvPlayer get(Player player) {
		String name = player.getName();
		return players.containsKey(name) ? players.get(name) : add(player);
	}

	private PvPlayer add(Player player) {
		if (plugin.getServer().getPlayer(player.getUniqueId()) == null)
			return null;
		PvPlayer pvPlayer = new PvPlayer(player, plugin);
		players.put(player.getName(), pvPlayer);
		if (players.size() == 1)
			TeamProfile.setupTeams();
		return pvPlayer;
	}

	public void untag(PvPlayer p) {
		tagTask.getTagged().remove(p);
		p.unTag();
	}
	
	public void tag(PvPlayer p) {
		tagTask.getTagged().add(p);
	}

	public void remove(final PvPlayer player) {
		new BukkitRunnable() {
			public void run() {
				if (player.getPlayer() == null) {
					players.remove(player.getName());
				}
			}
		}.runTaskLater(plugin, Variables.toggleCooldown * 20);
		savePvPState(player.getUUID(), player.hasPvPEnabled());
	}

	public void removeTeams() {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		if (scoreboard.getTeam("InCombat") != null)
			scoreboard.getTeam("InCombat").unregister();

		if (scoreboard.getTeam("PvPOn") != null)
			scoreboard.getTeam("PvPOn").unregister();

		if (scoreboard.getTeam("PvPOff") != null)
			scoreboard.getTeam("PvPOff").unregister();
	}

	public void savePvPState(UUID id, boolean pvpState) {
		configManager.saveUser(id, !pvpState);
	}

	private void applyFine(Player p) {
		if (economy != null) {
			economy.withdrawPlayer(p, Variables.fineAmount);
		} else {
			plugin.getLogger().severe("Tried to apply fine but no Economy plugin found!");
			plugin.getLogger().severe("Disable fines feature or get an Economy plugin to fix this error");
		}
	}

	public void applyPenalty(Player p) {
		if (economy != null) {
			economy.withdrawPlayer(p, Variables.moneyPenalty);
		} else {
			plugin.getLogger().severe("Tried to apply penalty but no Economy plugin found!");
			plugin.getLogger().severe("Disable money penalty on kill or get an Economy plugin to fix this error");
		}
	}

	public void giveReward(Player killer, Player victim) {
		if (economy != null) {
			economy.depositPlayer(killer, Variables.moneyReward);
			killer.sendMessage(Messages.Money_Reward.replace("%m", Double.toString(Variables.moneyReward)).replace("%p", victim.getName()));
		} else {
			plugin.getLogger().severe("Tried to give reward but no Economy plugin found!");
			plugin.getLogger().severe("Disable money reward on kill or get an Economy plugin to fix this error");
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
		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	public HashMap<String, PvPlayer> getPlayers() {
		return players;
	}

	public TagTask getTagTask() {
		return tagTask;
	}

}
