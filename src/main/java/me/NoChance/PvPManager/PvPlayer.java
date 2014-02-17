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

public class PvPlayer {

	private String name;
	private boolean newbie;
	private boolean tagged;
	private boolean pvpState;
	private boolean pvpLogged;
	private long toggleTime;
	private int newbieTimer;
	private int tagTimer;
	private HashMap<String, Integer> victim = new HashMap<String, Integer>();

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
			message("§6[§8PvPManager§6] §cYou can't toggle it yet!");
			return;
		} else {
			toggleTime = System.currentTimeMillis();
			if (hasPvPEnabled())
				setPvP(false);
			else
				setPvP(true);
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

	public boolean overrideAll() {
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
			newbieTimer = PlayerHandler.scheduleNewbieTimer(this);
		} else {
			if (Bukkit.getServer().getScheduler().isQueued(newbieTimer)) {
				Bukkit.getServer().getScheduler().cancelTask(newbieTimer);
				if (Utils.isOnline(name))
					message("§6[§8PvPManager§6] §eYou Removed Your PvP Protection! Be Careful");
			} else if (Utils.isOnline(name))
				message(Messages.Newbie_Protection_End);
		}
		this.newbie = newbie;
	}

	public void setTagged(boolean tagged) {
		if (tagged) {
			if (getPlayer().hasPermission("pvpmanager.nocombat"))
				return;
			tagTimer = PlayerHandler.scheduleTagTimer(this);
			message(Messages.You_Are_InCombat);
		} else {
			if (Utils.isOnline(name))
				message(Messages.Out_Of_Combat);
		}
		this.tagged = tagged;
	}

	public void renewTag() {
		if (isInCombat()) {
			Bukkit.getServer().getScheduler().cancelTask(tagTimer);
			tagTimer = PlayerHandler.scheduleTagTimer(this);
		}
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
