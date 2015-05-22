package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.UUID;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.TeamProfile;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Tasks.CleanKillersTask;
import me.NoChance.PvPManager.Tasks.TagTask;
import me.NoChance.PvPManager.Utils.CancelResult;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerHandler {

	private final HashMap<String, PvPlayer> players = new HashMap<String, PvPlayer>();
	private final ConfigManager configManager;
	private final DependencyManager dependencyManager;
	private final PvPManager plugin;
	private final TagTask tagTask = new TagTask();

	public PlayerHandler(final PvPManager plugin) {
		this.plugin = plugin;
		this.configManager = plugin.getConfigM();
		this.dependencyManager = plugin.getDependencyManager();
		if (Variables.isKillAbuseEnabled())
			new CleanKillersTask(this).runTaskTimer(plugin, 1200, Variables.getKillAbuseTime() * 20);

		addOnlinePlayers();
		tagTask.runTaskTimerAsynchronously(plugin, 20, 20);
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

	private boolean canAttack(final PvPlayer attacker, final PvPlayer defender) {
		if (!(attacker.isInCombat() && defender.isInCombat()))
			return false;
		else
			return dependencyManager.canAttack(attacker.getPlayer(), defender.getPlayer());
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
			pvPlayer.applyFine();
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

	public final HashMap<String, PvPlayer> getPlayers() {
		return players;
	}

	public final TagTask getTagTask() {
		return tagTask;
	}

}
