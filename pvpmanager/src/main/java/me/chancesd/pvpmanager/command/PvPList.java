package me.chancesd.pvpmanager.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.storage.Storage;
import me.chancesd.pvpmanager.storage.fields.UserDataFields;
import me.chancesd.pvpmanager.utils.ChatUtils;
import me.chancesd.sdutils.scheduler.ScheduleUtils;

public class PvPList implements TabExecutor {

	private final PlayerManager ph;

	public PvPList(final PlayerManager ph) {
		this.ph = ph;
	}

	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		final boolean isPlayer = sender instanceof Player;
		if (args.length == 0) {
			sendList(sender, isPlayer);
		} else if (args.length == 1 && args[0].equalsIgnoreCase("offline")) {
			ScheduleUtils.runAsync(() -> {
				sender.sendMessage(Lang.PVP_LIST_TITLE.msg());
				sender.sendMessage(ChatColor.DARK_GRAY + "Gathering all offline players with PvP disabled, please wait...");
				sender.sendMessage(Lang.PVP_LIST_DISABLED.msg());
				sender.sendMessage(ChatColor.GRAY + "  " + pvpListOffline());
			});
		}
		return true;
	}

	private void sendList(final CommandSender sender, final boolean isPlayer) {
		sender.sendMessage(Lang.PVP_LIST_TITLE.msg());

		sender.sendMessage(Lang.PVP_LIST_ENABLED.msg());
		sender.sendMessage(ChatColor.GRAY + "  " + pvpList(sender, true, !isPlayer));

		sender.sendMessage(Lang.PVP_LIST_DISABLED.msg());
		sender.sendMessage(ChatColor.GRAY + "  " + pvpList(sender, false, !isPlayer));
	}

	private String pvpListOffline() {
		final StringBuilder list = new StringBuilder();
		final Storage storage = ph.getPlugin().getStorageManager().getStorage();
		for (final Map<String, Object> userData : storage.getAllUserData()) {
			final String name = (String) userData.get(UserDataFields.NAME);
			final Object pvpstatus = userData.get(UserDataFields.PVPSTATUS);
			boolean pvpState = true;
			if (pvpstatus instanceof Integer) {
				pvpState = (int) pvpstatus != 0;
			} else if (pvpstatus instanceof Boolean) {
				pvpState = (boolean) pvpstatus;
			}
			if (!pvpState && name != null)
				list.append(name).append(", ");
		}
		if (list.toString().isEmpty())
			return Lang.PVP_LIST_NO_RESULTS.msg();
		list.delete(list.length() - 2, list.length());
		return list.toString();
	}

	private String pvpList(final CommandSender sender, final boolean enabled, final boolean console) {
		final StringBuilder list = new StringBuilder();
		for (final CombatPlayer p : ph.getPlayers().values()) {
			final Player player = p.getPlayer();
			if (enabled == p.hasPvPEnabled() && (console || ((Player) sender).canSee(player))) {
				list.append(p.getName()).append(", ");
			}
		}
		if (list.toString().isEmpty())
			return Lang.PVP_LIST_NO_RESULTS.msg();
		list.delete(list.length() - 2, list.length());
		return list.toString();
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 1) {
			return ChatUtils.getMatchingEntries(args[0], Lists.newArrayList("offline"));
		}
		return Collections.emptyList();
	}

}
