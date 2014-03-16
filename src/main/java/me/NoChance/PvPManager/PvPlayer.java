package me.NoChance.PvPManager;

import java.util.HashMap;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.NoChance.PvPManager.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class PvPlayer {

	private String name;
	private boolean newbie;
	private boolean tagged;
	private boolean pvpState;
	private boolean pvpLogged;
	private long toggleTime;
	private BukkitTask newbieTask;
	private BukkitTask tagTask;
	private HashMap<String, Integer> victim = new HashMap<String, Integer>();
	public static PlayerHandler ph;

	public PvPlayer(Player player, YamlConfiguration userData) {
		this.name = player.getName();
		if (!player.hasPlayedBefore()) {
			this.pvpState = Variables.defaultPvp;
			if (Variables.newbieProtectionEnabled)
				setNewbie(true);
		} else if (!userData.getStringList("players").contains(name))
			this.pvpState = true;
		if (pvpState != Variables.defaultPvp)
			message("§6[§8PvPManager§6] §6Your PvP Status is §2" + pvpState + " §6do /pvp to change it");
		if (player.hasPermission("pvpmanager.nopvp"))
			this.pvpState = false;
		if (!Variables.nameTagColor.equalsIgnoreCase("none"))
			getPlayer().setScoreboard(ph.getMainScoreboard());
	}

	public String getName() {
		return this.name;
	}

	public Player getPlayer() {
		return Bukkit.getPlayerExact(name);
	}

	public String getWorldName() {
		return getPlayer().getWorld().getName();
	}

	public long getToggleTime() {
		return this.toggleTime;
	}

	public void message(String message) {
		getPlayer().sendMessage(message);
	}

	public void togglePvP() {
		if (!CombatUtils.checkToggleCooldown(toggleTime)) {
			message(Messages.Error_PvP_Cooldown);
			return;
		} else {
			toggleTime = System.currentTimeMillis();
			setPvP(!pvpState);
		}
	}

	public boolean isNewbie() {
		return this.newbie;
	}

	public boolean isInCombat() {
		return this.tagged;
	}

	public boolean hasPvPEnabled() {
		return this.pvpState;
	}

	public boolean hasPvPLogged() {
		return this.pvpLogged;
	}

	public boolean hasOverride() {
		return getPlayer().hasPermission("pvpmanager.override");
	}

	public void disableFly() {
		Player player = getPlayer();
		player.setFlying(false);
		player.setAllowFlight(false);
	}

	public void setNewbie(boolean newbie) {
		if (newbie) {
			message(Messages.Newbie_Protection.replace("%", Integer.toString(Variables.newbieProtectionTime)));
			newbieTask = ph.scheduleNewbieTask(this);
		} else {
			if (Bukkit.getServer().getScheduler().isQueued(newbieTask.getTaskId())) {
				newbieTask.cancel();
				if (Utils.isOnline(name))
					message("§6[§8PvPManager§6] §eYou Removed Your PvP Protection! Be Careful");
			} else if (Utils.isOnline(name))
				message(Messages.Newbie_Protection_End);
		}
		this.newbie = newbie;
	}

	public void setTagged(boolean tag) {
		Player p = getPlayer();
		if (tag) {
			if (tagged) {
				renewTag();
				return;
			}
			if (getPlayer().hasPermission("pvpmanager.nocombat"))
				return;
			tagTask = ph.scheduleTagTask(this);
			if (ph.getTeam() != null)
				ph.getTeam().addPlayer(p);
			if (!Variables.inCombatSilent)
				message(Messages.You_Are_InCombat);
		} else {
			if (Utils.isOnline(name)) {
				if (ph.getTeam() != null)
					ph.getTeam().removePlayer(p);
				if (!Variables.inCombatSilent)
					message(Messages.Out_Of_Combat);
			}
		}
		this.tagged = tag;
	}

	public void renewTag() {
		tagTask.cancel();
		tagTask = ph.scheduleTagTask(this);
	}

	public void setPvP(boolean pvpState) {
		this.pvpState = pvpState;
		if (!pvpState) {
			message(Messages.PvP_Disabled);
			if (Variables.toggleBroadcast)
				Bukkit.getServer().broadcastMessage(Messages.PvPToggle_Off_Broadcast.replace("%p", name));
		} else {
			message(Messages.PvP_Enabled);
			if (Variables.toggleBroadcast)
				Bukkit.getServer().broadcastMessage(Messages.PvPToggle_On_Broadcast.replace("%p", name));
		}
	}

	public void addVictim(String victimName) {
		if (!victim.containsKey(victimName)) {
			victim.put(victimName, 1);
		} else if (victim.containsKey(victimName)) {
			int totalKills = victim.get(victimName);
			if (totalKills < Variables.killAbuseMaxKills) {
				totalKills++;
				victim.put(victimName, totalKills);
			}
			if (totalKills >= Variables.killAbuseMaxKills) {
				for (String command : Variables.killAbuseCommands) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", name));
				}
			}
		}
	}

	public void clearVictims() {
		victim.clear();
	}

	public void setPvpLogged(boolean pvpLogged) {
		this.pvpLogged = pvpLogged;
	}

	public boolean isVictim(String victimName) {
		return victim.containsKey(victimName);
	}
}
