package me.chancesd.pvpmanager.command;

import com.google.common.base.Strings;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.world.CombatWorld;
import me.chancesd.pvpmanager.player.world.CombatWorld.WorldOptionState;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.sdutils.command.ArgumentType;
import me.chancesd.sdutils.command.BaseCommand;
import me.chancesd.sdutils.command.CommandArgument;
import me.chancesd.sdutils.utils.Log;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Worlds extends BaseCommand {

	private final PvPManager plugin;

	public Worlds(final PvPManager plugin) {
		this.plugin = plugin;

		// Configure command
		this.description("Control panel to manage world PvP")
				.usage("/pmr worlds [list|set] [world] [option] [value]")
				.permission(Permissions.ADMIN.getPermission());

		// Add subcommands
		subCommand("list", new WorldsListCommand());
		subCommand("set", new WorldsSetCommand());
	}

	@Override
	public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
		// If no arguments provided, show world list/menu
		if (args.isEmpty()) {
			if (sender instanceof final Player player) {
				createWorldMenu(player);
			} else {
				sender.sendMessage("§cConsole must use '/pmr worlds list' to see world information.");
			}
			return;
		}

		// This should never be reached since subcommands handle everything
		sender.sendMessage(Lang.ERROR_COMMAND.msg());
	}

	public void createWorldMenu(final Player player) {
		final TextComponent menuHeader = new TextComponent(
				"" + ChatColor.GREEN + ChatColor.BOLD + "World Combat Manager" + "\n");
		player.spigot().sendMessage(menuHeader);

		for (final CombatWorld combatWorld : plugin.getWorldManager().getWorlds()) {
			final TextComponent worldItem = new TextComponent(
					ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "World: " + ChatColor.GOLD + combatWorld.getName() + ChatColor.AQUA + " \n  ");

			final World world = Bukkit.getWorld(combatWorld.getName());
			if (world == null) {
				Log.warning("Null world in world list!");
				continue;
			}
			final List<CombatPlayer> pvPlayerList = world.getPlayers().stream().map(p -> plugin.getPlayerManager().get(p)).toList();
			worldItem.addExtra(createInfoText("Players", pvPlayerList.size()));
			worldItem.addExtra(createSeparator());
			worldItem.addExtra(createInfoText("In Combat", pvPlayerList.stream().filter(CombatPlayer::isInCombat).count()));
			worldItem.addExtra(createSeparator());
			worldItem.addExtra(createInfoText("PvP On", pvPlayerList.stream().filter(CombatPlayer::hasPvPEnabled).count()));
			worldItem.addExtra(createSeparator());
			worldItem.addExtra(createInfoText("PvP Off", pvPlayerList.stream().filter(pvPlayer -> !pvPlayer.hasPvPEnabled()).count()));
			player.spigot().sendMessage(worldItem);
			player.sendMessage(ChatColor.DARK_AQUA + "  Options");
			final String pvpHoverText = ChatColor.BLUE + "" + ChatColor.BOLD + "Enables or disables PvP in " + ChatColor.YELLOW
					+ combatWorld.getName() + "\n\n"
					+ ChatColor.GREEN + "On - Enables PvP\n"
					+ ChatColor.RED + "Off - Disables PvP";
			final BaseComponent[] pvpEnabled = createFeature(
					String.format("/pmr worlds set %s PvP", combatWorld.getName()), pvpHoverText, "PvP",
					WorldOptionState.fromBoolean(combatWorld.isCombatAllowed()), false);
			final String forcepvpHoverText = ChatColor.BLUE + "" + ChatColor.BOLD + "Forces a PvP state in " + ChatColor.YELLOW
					+ combatWorld.getName() + "\n\n"
					+ ChatColor.GREEN + "On - Forces PvP to enabled and blocks players from turning it off\n"
					+ ChatColor.RED + "Off - Forces PvP to disabled and blocks players from turning it on\n"
					+ ChatColor.DARK_PURPLE + "Neutral - Does nothing, just leaves the player with the PvP state they already had";
			final BaseComponent[] forcePvP = createFeature(
					String.format("/pmr worlds set %s ForcePvP", combatWorld.getName()), forcepvpHoverText, "Force PvP",
					combatWorld.isPvPForced(), true);
			player.spigot().sendMessage(pvpEnabled);
			player.spigot().sendMessage(forcePvP);
		}
	}

	private BaseComponent[] createFeature(final String command, final String hoverText, final String displayName, final WorldOptionState state,
			final boolean thirdState) {
		final ComponentBuilder builder = new ComponentBuilder();
		builder.append(ChatColor.DARK_AQUA + "    - " + ChatColor.GRAY + displayName).color(ChatColor.GRAY);
		builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
		builder.append(ChatColor.DARK_GRAY + Strings.repeat(".", (int) (75 - displayName.length() * 2.86)) + " ");

		final BaseComponent[] onButton = createToggleButton(command, "On", ChatColor.GREEN, state, WorldOptionState.ON);
		final BaseComponent[] offButton = createToggleButton(command, "Off", ChatColor.RED, state, WorldOptionState.OFF);

		builder.append(onButton);
		builder.append(" ", ComponentBuilder.FormatRetention.NONE);
		builder.append(offButton);
		if (thirdState) {
			final BaseComponent[] neutralButton = createToggleButton(command, "Neutral", ChatColor.DARK_PURPLE, state, WorldOptionState.NONE);
			builder.append(" ", ComponentBuilder.FormatRetention.NONE);
			builder.append(neutralButton);
		}

		return builder.create();
	}

	private BaseComponent[] createToggleButton(final String command, final String text, final ChatColor color, final WorldOptionState state,
			final WorldOptionState selectedState) {
		BaseComponent[] stateComp;
		String hoverText = "";
		String finalCommand = "";
		if (state == selectedState) {
			stateComp = new ComponentBuilder(text).color(color).create();
			hoverText = ChatColor.DARK_RED + "Click to unselect this option";
			finalCommand = command + " " + WorldOptionState.getOpposite(selectedState) + " --menu";
		} else {
			stateComp = new ComponentBuilder(text).color(ChatColor.DARK_GRAY).create();
			hoverText = ChatColor.DARK_GREEN + "Click to select this option";
			finalCommand = command + " " + selectedState + " --menu";
		}

		final ComponentBuilder builder = new ComponentBuilder();
		builder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, finalCommand));
		builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
		builder.append("[").color(ChatColor.DARK_GRAY).append(stateComp).append("]").color(ChatColor.DARK_GRAY);

		return builder.create();
	}

	private TextComponent createInfoText(final String text, final long value) {
		return new TextComponent(ChatColor.DARK_AQUA + text + ": " + ChatColor.YELLOW + value);
	}

	private TextComponent createSeparator() {
		return new TextComponent(ChatColor.DARK_GRAY + " | ");
	}

	// Subcommand for listing worlds
	private class WorldsListCommand extends BaseCommand {

		public WorldsListCommand() {
			this.description("List all worlds and their PvP settings")
					.usage("/pmr worlds list")
					.permission(Permissions.ADMIN.getPermission());
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			sender.sendMessage("§6§l--- World PvP Settings ---");
			for (final CombatWorld combatWorld : plugin.getWorldManager().getWorlds()) {
				final String pvpStatus = combatWorld.isCombatAllowed() ? "§a✓ Enabled" : "§c✗ Disabled";
				final String forceStatus = switch (combatWorld.isPvPForced()) {
				case ON -> "§a[Force ON]";
				case OFF -> "§c[Force OFF]";
				case NONE -> "§7[Normal]";
				};
				sender.sendMessage(String.format("§e%s: §fPvP %s %s",
						combatWorld.getName(), pvpStatus, forceStatus));
			}
		}
	}

	// Subcommand for setting world options
	private class WorldsSetCommand extends BaseCommand {
		public WorldsSetCommand() {
			this.description("Set world PvP options")
					.usage("/pmr worlds set <world> <option> <value> [--menu]")
					.permission(Permissions.ADMIN.getPermission())
					.argument("world", ArgumentType.WORLD).required().endArgument()
					.argument("option", ArgumentType.STRING).required().tabComplete("PvP", "ForcePvP").endArgument()
					.argument("value", ArgumentType.STRING).required().tabComplete("ON", "OFF", "NONE").endArgument()
					.argument("refreshMenu", ArgumentType.STRING).tabComplete("--menu").endArgument();
		}

		@Override
		public void execute(final CommandSender sender, final String label, final List<CommandArgument> args) {
			if (args.size() < 3) {
				sender.sendMessage("§cUsage: /pmr worlds set <world> <option> <value>");
				sender.sendMessage("§cOptions: PvP, ForcePvP");
				sender.sendMessage("§cValues: ON, OFF (and NONE for ForcePvP)");
				return;
			}

			final String worldName = getArgument(args, "world").getAsWorld().getName();
			final String option = getArgument(args, "option").getValue();
			final String value = getArgument(args, "value").getValue();

			final CombatWorld combatWorld = plugin.getWorldManager().getWorld(worldName);
			if (combatWorld == null) {
				sender.sendMessage(Lang.PREFIX + " §cThere is no world with that name: " + worldName);
				return;
			}

			if (option.equalsIgnoreCase("PvP")) {
				final boolean pvp = value.equalsIgnoreCase("ON");
				combatWorld.setCombatAllowed(pvp);
				plugin.getWorldManager().saveWorldData(combatWorld);
				sender.sendMessage(String.format("%s §aPvP in world '%s' set to: §e%s",
						Lang.PREFIX, worldName, pvp ? "ENABLED" : "DISABLED"));

			} else if (option.equalsIgnoreCase("ForcePvP")) {
				try {
					final WorldOptionState state = WorldOptionState.valueOf(value.toUpperCase());
					combatWorld.setForcePVP(state);
					plugin.getWorldManager().saveWorldData(combatWorld);
					sender.sendMessage(String.format("%s §aForce PvP in world '%s' set to: §e%s",
							Lang.PREFIX, worldName, state.name()));
				} catch (final IllegalArgumentException e) {
					sender.sendMessage("§cInvalid ForcePvP value. Use: ON, OFF, or NONE");
				}
			} else {
				sender.sendMessage("§cInvalid option. Use 'PvP' or 'ForcePvP'");
			}
			// Only refresh the worlds menu if --menu flag is provided (indicates it was called from menu)
			if (sender instanceof final Player player && hasArgument(args, "refreshMenu")) {
				createWorldMenu(player);
			}
		}
	}
}
