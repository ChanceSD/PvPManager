package me.chancesd.pvpmanager.command;

import com.google.common.base.Strings;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.chancesd.pvpmanager.setting.Messages;
import me.chancesd.pvpmanager.world.CombatWorld;
import me.chancesd.pvpmanager.world.CombatWorld.WorldOptionState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class Worlds {

	private final PvPManager plugin;

	public Worlds(final PvPManager plugin) {
		this.plugin = plugin;
	}

	public void createWorldMenu(final Player player) {
		final TextComponent menuHeader = new TextComponent(
				"" + ChatColor.GREEN + ChatColor.BOLD + "World Combat Manager" + "\n");
		player.spigot().sendMessage(menuHeader);

		for (final CombatWorld combatWorld : plugin.getWorldManager().getWorlds()) {
			final TextComponent worldItem = new TextComponent(
					ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "World: " + ChatColor.GOLD + combatWorld.getName() + ChatColor.AQUA + " \n  ");

			final World world = Bukkit.getWorld(combatWorld.getName());
			final List<PvPlayer> pvPlayerList = world.getPlayers().stream().map(p -> plugin.getPlayerHandler().get(p)).collect(Collectors.toList());
			worldItem.addExtra(createInfoText("Players", pvPlayerList.size()));
			worldItem.addExtra(createSeparator());
			worldItem.addExtra(createInfoText("In Combat", pvPlayerList.stream().filter(PvPlayer::isInCombat).count()));
			worldItem.addExtra(createSeparator());
			worldItem.addExtra(createInfoText("PvP On", pvPlayerList.stream().filter(PvPlayer::hasPvPEnabled).count()));
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

		final BaseComponent[] onButton = createToggleButton(command, "On", ChatColor.GREEN, state,WorldOptionState.ON);
		final BaseComponent[] offButton = createToggleButton(command, "Off", ChatColor.RED, state,WorldOptionState.OFF);

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

	private BaseComponent[] createToggleButton(final String command, final String text, final ChatColor color, final WorldOptionState state, final WorldOptionState selectedState) {
		BaseComponent[] stateComp;
		String hoverText = "";
		String finalCommand = "";
		if (state == selectedState) {
			stateComp = new ComponentBuilder(text).color(color).create();
			hoverText = ChatColor.DARK_RED + "Click to unselect this option";
			finalCommand = command  + " " + WorldOptionState.getOpposite(selectedState);
		} else {
			stateComp = new ComponentBuilder(text).color(ChatColor.DARK_GRAY).create();
			hoverText = ChatColor.DARK_GREEN + "Click to select this option";
			finalCommand = command  + " " + selectedState;
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

	public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 5) {
			final String subcommand = args[1];
			if (subcommand.equalsIgnoreCase("set")) {
				final String world = args[2];
				final CombatWorld combatWorld = plugin.getWorldManager().getWorld(world);
				if (combatWorld == null) {
					sender.sendMessage(Messages.PREFIXMSG + ChatColor.RED + "There is no world with that name");
					return true;
				}
				final String option = args[3];
				if (option.equalsIgnoreCase("PvP")) {
					final boolean pvp = args[4].equalsIgnoreCase("ON");
					combatWorld.setCombatAllowed(pvp);
					plugin.getWorldManager().saveWorldData(combatWorld);
				} else if (option.equalsIgnoreCase("ForcePvP")) {
					final WorldOptionState state = WorldOptionState.valueOf(args[4]);
					combatWorld.setForcePVP(state);
					plugin.getWorldManager().saveWorldData(combatWorld);
				}
			}
		}
		if (sender instanceof Player) {
			createWorldMenu((Player) sender);
		}

		return true;
	}
}
