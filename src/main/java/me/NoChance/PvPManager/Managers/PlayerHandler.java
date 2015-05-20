package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.UUID;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.TeamProfile;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Listeners.WGListener;
import me.NoChance.PvPManager.Tasks.CleanKillersTask;
import me.NoChance.PvPManager.Tasks.TagTask;
import me.NoChance.PvPManager.Utils.CancelResult;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import com.massivecraft.factions.entity.MPlayer;

public class PlayerHandler {

	private final HashMap<String, PvPlayer> players = new HashMap<String, PvPlayer>();
	private final ConfigManager configManager;
	private final PvPManager plugin;
	private Economy economy;
	private final TagTask tagTask = new TagTask();
	private boolean useFactions;

	public PlayerHandler(final PvPManager plugin) {
		this.plugin = plugin;
		this.configManager = plugin.getConfigM();
		if (Variables.isKillAbuseEnabled())
			new CleanKillersTask(this).runTaskTimer(plugin, 1200, Variables.getKillAbuseTime() * 20);
		if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
			if (setupEconomy()) {
				plugin.getLogger().info("Vault Found! Using it for currency related features");
			} else
				plugin.getLogger().severe("Error! No Economy plugin found");
		} else {
			plugin.getLogger().severe("Vault not found! Features requiring Vault won't work!");
			Variables.setFineEnabled(false);
		}
		addOnlinePlayers();
		tagTask.runTaskTimerAsynchronously(plugin, 20, 20);



		if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
			plugin.registerListener(new WGListener(this));
			plugin.getLogger().info("WorldGuard Found! Enabling WorldGuard Support");
		}
		final Plugin factions = Bukkit.getPluginManager().getPlugin("Factions");

		try {
			if (factions != null) {
				if (Integer.valueOf(factions.getDescription().getVersion().replace(".", "")) >= 270) {
					useFactions = true;
					Class.forName("com.massivecraft.factions.entity.MPlayer");
					plugin.getLogger().info("Factions Found! Hooked successfully");
				} else
					plugin.getLogger().info("Update your Factions plugin to the latest version if you want PvPManager to hook into it successfully");
			}
		} catch (final NumberFormatException e) {
			plugin.getLogger().warning("Couldn't read Factions version, maybe it's a fork?");
		} catch (final ClassNotFoundException e) {
			plugin.getLogger().warning("Factions broke their API again, an updated version of PvPManager should fix this soon");
			useFactions = false;
		}
	}

	public final CancelResult tryCancel(final Player damager, final Player defender) {
		final PvPlayer attacker = get(damager);
		final PvPlayer attacked = get(defender);
		if (attacker.hasOverride() || Variables.isStopBorderHopping() && canAttack(attacker, attacked))
			return CancelResult.FAIL_OVERRIDE;
		if (attacked.hasRespawnProtection() || attacker.hasRespawnProtection())
			return CancelResult.RESPAWN_PROTECTION;
		if (attacked.isNewbie())
			return CancelResult.NEWBIE_OTHER;
		if (attacker.isNewbie())
			return CancelResult.NEWBIE;
		if (!attacked.hasPvPEnabled())
			return CancelResult.PVPDISABLED_OTHER;
		if (!attacker.hasPvPEnabled())
			return CancelResult.PVPDISABLED;

		return CancelResult.FAIL;
	}

	// When stopping border hopping let's check if players are from different
	// Factions
	private boolean canAttack(final PvPlayer attacker, final PvPlayer attacked) {
		if (!(attacker.isInCombat() && attacked.isInCombat()))
			return false;
		else if (useFactions) {
			final MPlayer fAttacker = MPlayer.get(attacker.getPlayer());
			final MPlayer fAttacked = MPlayer.get(attacked.getPlayer());
			if (!fAttacker.hasFaction() || !fAttacked.hasFaction())
				return true;
			return !fAttacker.getFactionId().equalsIgnoreCase(fAttacked.getFactionId());
		}
		return true;
	}

	private void addOnlinePlayers() {
		for (final Player p : plugin.getServer().getOnlinePlayers()) {
			add(p);
		}
	}

	public final PvPlayer get(final Player player) {
		final String name = player.getName();
		return players.containsKey(name) ? players.get(name) : add(player);
	}

	private PvPlayer add(final Player player) {
		if (plugin.getServer().getPlayer(player.getUniqueId()) == null)
			return null;
		final PvPlayer pvPlayer = new PvPlayer(player, plugin);
		players.put(player.getName(), pvPlayer);
		if ((Variables.isUseNameTag() || Variables.isToggleNametagsEnabled()) && players.size() == 1)
			TeamProfile.setupTeams();
		pvPlayer.loadPvPState();
		return pvPlayer;
	}

	public final void untag(final PvPlayer p) {
		tagTask.getTagged().remove(p);
		p.unTag();
	}

	public final void tag(final PvPlayer p) {
		tagTask.getTagged().add(p);
	}

	public final void remove(final PvPlayer player) {
		new BukkitRunnable() {
			public void run() {
				if (player.getPlayer() == null) {
					players.remove(player.getName());
				}
			}
		}.runTaskLater(plugin, Variables.getToggleCooldown() * 20);
		savePvPState(player.getUUID(), player.hasPvPEnabled());
	}

	public final void removeTeams() {
		final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		if (scoreboard.getTeam("InCombat") != null)
			scoreboard.getTeam("InCombat").unregister();

		if (scoreboard.getTeam("PvPOn") != null)
			scoreboard.getTeam("PvPOn").unregister();

		if (scoreboard.getTeam("PvPOff") != null)
			scoreboard.getTeam("PvPOff").unregister();
	}

	public final void savePvPState(final UUID id, final boolean pvpState) {
		configManager.saveUser(id, !pvpState);
	}

	private void applyFine(final Player p) {
		if (economy != null) {
			economy.withdrawPlayer(p, Variables.getFineAmount());
		} else {
			plugin.getLogger().severe("Tried to apply fine but no Economy plugin found!");
			plugin.getLogger().severe("Disable fines feature or get an Economy plugin to fix this error");
		}
	}

	public final void applyPenalty(final Player p) {
		if (economy != null) {
			economy.withdrawPlayer(p, Variables.getMoneyPenalty());
		} else {
			plugin.getLogger().severe("Tried to apply penalty but no Economy plugin found!");
			plugin.getLogger().severe("Disable money penalty on kill or get an Economy plugin to fix this error");
		}
	}

	public final void giveReward(final Player killer, final Player victim) {
		if (economy != null) {
			economy.depositPlayer(killer, Variables.getMoneyReward());
			killer.sendMessage(Messages.getMoneyReward().replace("%m", Double.toString(Variables.getMoneyReward())).replace("%p", victim.getName()));
		} else {
			plugin.getLogger().severe("Tried to give reward but no Economy plugin found!");
			plugin.getLogger().severe("Disable money reward on kill or get an Economy plugin to fix this error");
		}
	}

	public final void applyPunishments(final Player player) {
		final PvPlayer pvPlayer = get(player);
		if (Variables.isKillOnLogout()) {
			pvPlayer.setPvpLogged(true);
			ItemStack[] inventory = null;
			ItemStack[] armor = null;
			if (!Variables.isDropInventory() || !Variables.isDropArmor()) {
				if (!Variables.isDropInventory()) {
					inventory = player.getInventory().getContents();
					player.getInventory().clear();
				}
				if (!Variables.isDropArmor()) {
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
		} else if (!Variables.isKillOnLogout()) {
			if (Variables.isDropInventory()) {
				fakeInventoryDrop(player, player.getInventory().getContents());
				player.getInventory().clear();
			}
			if (Variables.isDropArmor()) {
				fakeInventoryDrop(player, player.getInventory().getArmorContents());
				player.getInventory().setArmorContents(null);
			}
			if (Variables.isDropExp())
				fakeExpDrop(player);
		}
		if (Variables.isFineEnabled())
			applyFine(player);
	}

	private void fakeInventoryDrop(final Player player, final ItemStack[] inventory) {
		final Location playerLocation = player.getLocation();
		final World playerWorld = player.getWorld();
		for (final ItemStack itemstack : inventory) {
			if (itemstack != null && !itemstack.getType().equals(Material.AIR))
				playerWorld.dropItemNaturally(playerLocation, itemstack);
		}
	}

	private void fakeExpDrop(final Player player) {
		final int expdropped = player.getLevel() * 7;
		if (expdropped < 100)
			player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(expdropped);
		else
			player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(100);
		player.setLevel(0);
		player.setExp(0);
	}

	private boolean setupEconomy() {
		final RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	public final HashMap<String, PvPlayer> getPlayers() {
		return players;
	}

	public final TagTask getTagTask() {
		return tagTask;
	}

}
