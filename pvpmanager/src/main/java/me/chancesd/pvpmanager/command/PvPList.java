package me.chancesd.pvpmanager.command;

import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.storage.Storage;
import me.chancesd.pvpmanager.storage.fields.UserDataFields;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;
import me.chancesd.sdutils.scheduler.ScheduleUtils;

public class PvPList extends BaseCommand {

	private final PlayerManager ph;

	public PvPList(final PluginCommand pluginCommand, final PlayerManager ph) {
		super(pluginCommand);
		this.ph = ph;
		this.description("List the PvP state of all players")
				.usage("/pvplist [alloffline]").permission(Permissions.COMMAND_PVPLIST.getPermission())
				.argument("alloffline", ArgumentType.STRING).endArgument();
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		final boolean isPlayer = sender instanceof Player;

		if (args.isEmpty()) {
			sendList(sender, isPlayer);
		} else {
			final CommandArgument modeArg = args.get(0);
			if (modeArg.getValue().equalsIgnoreCase("alloffline")) {
				ScheduleUtils.runAsync(() -> {
					sender.sendMessage(Lang.PVP_LIST_TITLE.msg());
					sender.sendMessage(ChatColor.DARK_GRAY + "Gathering all offline players with PvP disabled, please wait...");
					sender.sendMessage(Lang.PVP_LIST_DISABLED.msg());
					sender.sendMessage(ChatColor.GRAY + "  " + pvpListOffline());
				});
			} else {
				sendList(sender, isPlayer);
			}
		}
	}

	private void sendList(final CommandSender sender, final boolean isPlayer) {
		sender.sendMessage(Lang.PVP_LIST_TITLE.msg());
		sender.sendMessage(ChatColor.DARK_GRAY + "You can use " + ChatColor.YELLOW + "/pvplist alloffline" + ChatColor.DARK_GRAY
				+ " to see the PvP status of all offline players.");

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
}
