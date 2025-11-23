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
import me.chancesd.sdutils.display.chat.ChatMenu;
import me.chancesd.sdutils.display.chat.NavigationButtons;
import me.chancesd.sdutils.display.chat.content.StaticContentProvider;
import me.chancesd.sdutils.scheduler.ScheduleUtils;

public class PvPList extends BaseCommand {

	private static final int LINES_PER_PAGE = 16;
	private final PlayerManager ph;

	public PvPList(final PluginCommand pluginCommand, final PlayerManager ph) {
		super(pluginCommand);
		this.ph = ph;
		this.description("List the PvP state of all players")
				.usage("/pvplist [offline]")
				.permission(Permissions.COMMAND_PVPLIST.getPermission())
				.argument("mode", ArgumentType.STRING).tabComplete("offline").endArgument()
				.argument("page", ArgumentType.INTEGER).defaultValue("1").dependsOn("mode").endArgument();
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		final boolean isPlayer = sender instanceof Player;

		boolean offlineMode = false;
		int page = 1;

		if (!args.isEmpty()) {
			final CommandArgument firstArg = getArgument(args, "mode");

			if (firstArg.getValue().equalsIgnoreCase("offline")) {
				if (!Permissions.COMMAND_PVPLIST_OFFLINE.hasPerm(sender)) {
					return;
				}
				offlineMode = true;
				if (args.size() > 1) {
					page = getArgument(args, "page").getAsInt();
				}
			} else {
				try {
					page = Integer.parseInt(firstArg.getValue());
				} catch (final NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.YELLOW + "/pvplist [page]"
							+ ChatColor.GRAY + " or " + ChatColor.YELLOW + "/pvplist offline [page]");
					return;
				}
			}
		}

		if (offlineMode) {
			showOfflineList(sender, page);
		} else {
			showOnlineList(sender, isPlayer, page);
		}
	}

	private void showOnlineList(final CommandSender sender, final boolean isPlayer, final int page) {
		final StaticContentProvider contentProvider = new StaticContentProvider();

		// Add hint about offline command if user has permission
		if (Permissions.COMMAND_PVPLIST_OFFLINE.hasPerm(sender)) {
			contentProvider.addLine(ChatColor.DARK_GRAY + "Tip: Use " + ChatColor.YELLOW + "/pvplist offline"
					+ ChatColor.DARK_GRAY + " to see all offline players.", null, null);
			contentProvider.addLine("");
		}

		// Add enabled players section
		contentProvider.addLine(Lang.PVP_LIST_ENABLED.msg(), null, null);
		addPlayerList(sender, contentProvider, true, !isPlayer);

		// Add disabled players section
		contentProvider.addLine("");
		contentProvider.addLine(Lang.PVP_LIST_DISABLED.msg(), null, null);
		addPlayerList(sender, contentProvider, false, !isPlayer);

		final ChatMenu menu = ChatMenu.builder()
				.header(Lang.PVP_LIST_TITLE.msg() + " #9E9E9E(Page {page}/{total})")
				.linesPerPage(LINES_PER_PAGE)
				.contentProvider(contentProvider)
				.navigation(NavigationButtons.builder()
						.navigationPrefix("/pvplist")
						.build())
				.build();

		menu.show(sender, page);
	}

	private void addPlayerList(final CommandSender sender, final StaticContentProvider contentProvider, final boolean enabled, final boolean console) {
		int count = 0;
		for (final CombatPlayer p : ph.getPlayers().values()) {
			final Player player = p.getPlayer();
			if (enabled == p.hasPvPEnabled() && (console || ((Player) sender).canSee(player))) {
				contentProvider.addLine(ChatColor.GRAY + "  • " + ChatColor.GRAY + player.getDisplayName());
				count++;
			}
		}
		if (count == 0) {
			contentProvider.addLine(ChatColor.GRAY + "  " + Lang.PVP_LIST_NO_RESULTS.msg());
		}
	}

	private void showOfflineList(final CommandSender sender, final int page) {
		sender.sendMessage(ChatColor.DARK_GRAY + "Gathering all offline players with PvP disabled...");

		ScheduleUtils.runAsync(() -> {
			final StaticContentProvider contentProvider = new StaticContentProvider();
			final Storage storage = ph.getPlugin().getStorageManager().getStorage();

			contentProvider.addLine(Lang.PVP_LIST_DISABLED.msg(), null, null);

			int count = 0;
			for (final Map<String, Object> userData : storage.getAllUserData()) {
				final String displayName = (String) userData.get(UserDataFields.DISPLAYNAME);
				final Object pvpstatus = userData.get(UserDataFields.PVPSTATUS);
				boolean pvpState = true;

				if (pvpstatus instanceof Integer) {
					pvpState = (int) pvpstatus != 0;
				} else if (pvpstatus instanceof Boolean) {
					pvpState = (boolean) pvpstatus;
				}

				if (!pvpState && displayName != null) {
					contentProvider.addLine(ChatColor.GRAY + "  • " + ChatColor.GRAY + displayName);
					count++;
				}
			}

			if (count == 0) {
				contentProvider.addLine(ChatColor.GRAY + "  " + Lang.PVP_LIST_NO_RESULTS.msg(), null, null);
			}

			final ChatMenu menu = ChatMenu.builder()
					.header(Lang.PVP_LIST_TITLE.msg() + " #FF5722(Offline) #9E9E9E(Page {page}/{total})")
					.linesPerPage(LINES_PER_PAGE)
					.contentProvider(contentProvider)
					.navigation(NavigationButtons.builder()
							.navigationPrefix("/pvplist offline")
							.build())
					.build();

			menu.show(sender, page);
		});
	}
}