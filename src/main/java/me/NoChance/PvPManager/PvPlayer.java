package me.NoChance.PvPManager;

import java.util.HashMap;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Tasks.NewbieTask;
import me.NoChance.PvPManager.Tasks.TagTask;
import me.NoChance.PvPManager.Utils.CombatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

public class PvPlayer {

	private String name;
	private boolean newbie;
	private boolean tagged;
	private boolean pvpState;
	private boolean pvpLogged;
	private long toggleTime;
	private NewbieTask newbieTask;
	private BukkitTask tagTask;
	private HashMap<String, Integer> victim = new HashMap<String, Integer>();
	private PvPManager plugin;
	private Team previousTeam;
	public static Team inCombatTeam;

	public PvPlayer(Player player, PvPManager plugin) {
		this.name = player.getName();
		this.plugin = plugin;
		this.newbieTask = new NewbieTask(this);
		if (!player.hasPlayedBefore()) {
			this.pvpState = Variables.defaultPvp;
			if (Variables.newbieProtectionEnabled)
				setNewbie(true);
		} else if (!plugin.getConfigM().getUserFile().getStringList("players").contains(name))
			this.pvpState = true;
		if (pvpState != Variables.defaultPvp)
			message("§6[§8PvPManager§6] §6Your PvP Status is §2" + pvpState + " §6do /pvp to change it");
		if (player.hasPermission("pvpmanager.nopvp"))
			this.pvpState = false;
	}

	public String getName() {
		return this.name;
	}

	@SuppressWarnings("deprecation")
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
		if (isOnline())
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
	
	public boolean isOnline() {
		return getPlayer() != null;
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
			newbieTask.runTaskLater(plugin, Variables.newbieProtectionTime * 1200);
		} else {
			if (Bukkit.getServer().getScheduler().isQueued(newbieTask.getTaskId())) {
				newbieTask.cancel();
				message("§6[§8PvPManager§6] §eYou Removed Your PvP Protection! Be Careful");
			} else
				message(Messages.Newbie_Protection_End);
		}
		this.newbie = newbie;
	}

	public void setTagged(boolean tag) {
		Player p = getPlayer();
		if (tag) {
			if (getPlayer().hasPermission("pvpmanager.nocombat"))
				return;
			if (tagged) {
				renewTag();
				return;
			}
			tagTask = new TagTask(this).runTaskLater(plugin, Variables.timeInCombat * 20);
			if (inCombatTeam != null){
				previousTeam = p.getScoreboard().getPlayerTeam(p);
				inCombatTeam.addPlayer(p);
			}
			if (!Variables.inCombatSilent)
				message(Messages.You_Are_InCombat);
		} else {
			if (isOnline()) {
				if (inCombatTeam != null){
					inCombatTeam.removePlayer(p);
					if(previousTeam != null)
						previousTeam.addPlayer(p);
				}
				if (!Variables.inCombatSilent)
					message(Messages.Out_Of_Combat);
			}
		}
		this.tagged = tag;
	}

	public void renewTag() {
		tagTask.cancel();
		tagTask = new TagTask(this).runTaskLater(plugin, Variables.timeInCombat * 20);
	}

	public void setPvP(boolean pvpState) {
		this.pvpState = pvpState;
		if (!pvpState) {
			message(Messages.PvP_Disabled);
			if (Variables.toggleBroadcast)
				Bukkit.broadcastMessage(Messages.PvPToggle_Off_Broadcast.replace("%p", name));
		} else {
			message(Messages.PvP_Enabled);
			if (Variables.toggleBroadcast)
				Bukkit.broadcastMessage(Messages.PvPToggle_On_Broadcast.replace("%p", name));
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
