package me.chancesd.pvpmanager.command;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;
import me.chancesd.sdutils.display.chat.ChatMenu;
import me.chancesd.sdutils.display.chat.NavigationButtons;
import me.chancesd.sdutils.display.chat.content.StaticContentProvider;

public class Help extends BaseCommand {

	private ChatMenu helpMenu;
	// Styling constants for consistency with BaseCommand
	private static final String PLAYER_COMMAND_COLOR = "#4CAF50";
	private static final String STAFF_COMMAND_COLOR = "#FF5722";
	private static final String DESCRIPTION_COLOR = "#9E9E9E";
	
	// Category constants
	private static final String PLAYER_CATEGORY = "player";
	private static final String STAFF_CATEGORY = "staff";
	
	// Command name constants
	private static final String PVPMANAGER_COMMAND = "pvpmanager";
	private static final String HELP_MENU_DESCRIPTION = "Show this global command menu";

	public Help() {
		this.description(HELP_MENU_DESCRIPTION)
				.usage("/pmr menu [page]")
				.permission(Permissions.COMMAND_MENU.getPermission())
				.argument("page", ArgumentType.INTEGER).endArgument();
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		// Handle page number: /pmr menu <page>
		if (!args.isEmpty()) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("§cNavigation is only available for players!");
				return;
			}
			final Player player = (Player) sender;

			try {
				final int page = Integer.parseInt(args.get(0).getValue());
				if (page > 0) {
					helpMenu(player, page);
					return;
				}
			} catch (final NumberFormatException e) {
				sender.sendMessage("§cInvalid page number!");
				return;
			}
		}

		// Default: show help menu
		helpMenu(sender, 1);
	}

	public void helpMenu(final CommandSender sender, final int page) {
		// Initialize help menu if needed
		if (helpMenu == null) {
			helpMenu = createHelpMenu(sender);
		}

		helpMenu.show(sender);
	}

	/**
	 * Build hover text for a command with automatically retrieved permissions and usage.
	 * @param actionText The action text (e.g., "Click to execute command" or "Click to see command usage")
	 * @param description The command description
	 * @param command The BaseCommand for permission and usage lookup
	 * @return The formatted hover text
	 */
	private String buildHoverText(final String actionText, final String description, final BaseCommand command) {
		final Set<String> permissions = command.getPermissions();
		final String usage = command != null && command.getUsage() != null && !command.getUsage().isEmpty() 
			? command.getUsage() 
			: null;
		
		final StringBuilder hoverText = new StringBuilder();
		hoverText.append("&7").append(actionText);
		hoverText.append("\n#812c6c► ").append(description);

		boolean hasUsage = usage != null;
		boolean hasPermissions = !permissions.isEmpty();

		if (hasUsage || hasPermissions) {
			hoverText.append("\n");
		}

		if (hasUsage) {
			hoverText.append("\n#4CAF50&lUsage: &8").append(usage);
		}

		if (hasPermissions) {
			hoverText.append("\n#FF5722&lRequires: #c98d81").append(permissions);
		}
		
		return hoverText.toString();
	}

	/**
	 * Build hover text for executable commands
	 */
	private String buildExecuteHoverText(final String description, final BaseCommand command) {
		return buildHoverText("Click to execute command", description, command);
	}

	/**
	 * Build hover text for syntax commands (commands that need parameters)
	 */
	private String buildSyntaxHoverText(final String description, final BaseCommand command) {
		return buildHoverText("Click to see command usage", description, command);
	}

	/**
	 * Get command description from BaseCommand.
	 * Falls back to a default message if not available.
	 */
	private String getCommandDescription(final BaseCommand command) {
		if (command != null && command.getDescription() != null && !command.getDescription().isEmpty()) {
			return command.getDescription();
		}
		return "No description available";
	}

	/**
	 * Get command usage from BaseCommand.
	 * Falls back to basic command format if not available.
	 */
	private String getCommandUsage(final BaseCommand command, final String commandName) {
		if (command != null && command.getUsage() != null && !command.getUsage().isEmpty()) {
			return command.getUsage();
		}
		return "/" + commandName;
	}

	/**
	 * Build hover text automatically using BaseCommand metadata.
	 * Determines if command needs parameters and builds appropriate hover.
	 */
	private String buildAutoHoverText(final BaseCommand command, final String description) {
		final String usage = getCommandUsage(command, "");
		final boolean needsParameters = usage.contains("<") || usage.contains("[");
		
		if (needsParameters) {
			return buildSyntaxHoverText(description, command);
		} else {
			return buildExecuteHoverText(description, command);
		}
	}

	/**
	 * Add a command to the content provider if the sender has permission.
	 * Uses BaseCommand metadata for description and usage, with manual categorization.
	 * 
	 * @param content The content provider to add to
	 * @param sender The command sender (for permission checking)
	 * @param commandName The command name for BaseCommand lookup
	 * @param category "player" or "staff" for color coding
	 */
	private void addCommandIfAvailable(final StaticContentProvider content, final CommandSender sender, 
	                                 final String commandName, final String category) {
		final PvPManager plugin = PvPManager.getInstance();
		final BaseCommand command = plugin.getRegisteredCommand(commandName);
		
		if (command == null || !command.hasPermission(sender)) {
			return; // Skip if command not found or no permission
		}
		
		final String description = getCommandDescription(command);
		final String usage = getCommandUsage(command, commandName);
		final String hoverText = buildAutoHoverText(command, description);
		final String color = PLAYER_CATEGORY.equals(category) ? PLAYER_COMMAND_COLOR : STAFF_COMMAND_COLOR;
		
		final String displayText = String.format("  %s%s %s- &f%s", color, usage, DESCRIPTION_COLOR, description);
		content.addLine(displayText, commandName, hoverText);
	}

	/**
	 * Add a PMR subcommand to the content provider.
	 * Shows PMR subcommands in the format "/pmr subcommand" with descriptions from BaseCommand.
	 * 
	 * @param content The content provider to add to
	 * @param sender The command sender (for permission checking)
	 * @param subcommandName The subcommand name
	 */
	private void addPmrSubcommandIfAvailable(final StaticContentProvider content, final CommandSender sender,
	                                       final String subcommandName) {
		final PvPManager plugin = PvPManager.getInstance();
		final BaseCommand pmrCommand = plugin.getRegisteredCommand(PVPMANAGER_COMMAND);
		
		if (pmrCommand == null) {
			return;
		}
		
		// Find the specific subcommand
		BaseCommand subcommand = null;
		for (final BaseCommand sub : pmrCommand.getSubCommands()) {
			if (sub.getAliases().contains(subcommandName)) {
				subcommand = sub;
				break;
			}
		}
		
		if (subcommand == null || !subcommand.hasPermission(sender)) {
			return; // Skip if subcommand not found or no permission
		}
		
		// Get description directly from the subcommand
		String description = subcommand.getDescription();
		if (description == null || description.isEmpty()) {
			description = "No description available";
		}
		
		// PMR subcommands use the staff color and format
		final String usage = "/pmr " + subcommandName;
		final String hoverText = buildAutoHoverText(subcommand, description);
		final String displayText = String.format("  %s%s %s- &f%s", STAFF_COMMAND_COLOR, usage, DESCRIPTION_COLOR, description);
		content.addLine(displayText, subcommandName, hoverText);
	}

	private ChatMenu createHelpMenu(final CommandSender sender) {
		final StaticContentProvider contentProvider = new StaticContentProvider();

		// Player Commands
		contentProvider.addLine("#f7e758&l► Player Commands:", null, null);
		addCommandIfAvailable(contentProvider, sender, "pvp", PLAYER_CATEGORY);
		addCommandIfAvailable(contentProvider, sender, "pvpinfo", PLAYER_CATEGORY);
		addCommandIfAvailable(contentProvider, sender, "pvpstatus", PLAYER_CATEGORY);
		addCommandIfAvailable(contentProvider, sender, "pvplist", PLAYER_CATEGORY);
		addCommandIfAvailable(contentProvider, sender, "newbie", PLAYER_CATEGORY);

		// Spacer
		contentProvider.addLine("", null, null);

		// Staff Commands
		contentProvider.addLine("#FF5722&l► Staff Commands:", null, null);
		addCommandIfAvailable(contentProvider, sender, "pvpoverride", STAFF_CATEGORY);
		addCommandIfAvailable(contentProvider, sender, "pvpglobal", STAFF_CATEGORY);
		addCommandIfAvailable(contentProvider, sender, "pvptag", STAFF_CATEGORY);
		addCommandIfAvailable(contentProvider, sender, "untag", STAFF_CATEGORY);
		addCommandIfAvailable(contentProvider, sender, "announce", STAFF_CATEGORY);
		
		// PMR subcommands - check if sender has permission for the main pmr command
		final PvPManager plugin = PvPManager.getInstance();
		final BaseCommand pmrCommand = plugin.getRegisteredCommand(PVPMANAGER_COMMAND);
		if (pmrCommand != null && pmrCommand.hasPermission(sender)) {
			addPmrSubcommandIfAvailable(contentProvider, sender, "menu");
			addPmrSubcommandIfAvailable(contentProvider, sender, "help");
			addPmrSubcommandIfAvailable(contentProvider, sender, "worlds");
			addPmrSubcommandIfAvailable(contentProvider, sender, "reload");
			addPmrSubcommandIfAvailable(contentProvider, sender, "cleanup");
			addPmrSubcommandIfAvailable(contentProvider, sender, "update");
			addPmrSubcommandIfAvailable(contentProvider, sender, "convert");
			addPmrSubcommandIfAvailable(contentProvider, sender, "debug");
			addPmrSubcommandIfAvailable(contentProvider, sender, "locale");
		}

		return ChatMenu.builder()
				.header("#607D8B&l╔══════ §4§lPvP§8§lManager #f7e758&lv4.10" + "" + " #9E9E9E(Page {page}/{total}) #607D8B&l══════╗")
				.footer("#607D8B&l╚══════════════════════════════╝")
				.linesPerPage(15)
				.contentProvider(contentProvider)
				.navigation(NavigationButtons.builder()
						.navigationPrefix("/pmr help")
						.previousText("&7[&c« Previous Page&7]")
						.nextText("&7[&cNext Page »&7]")
						.build())
				.build();
	}
}
