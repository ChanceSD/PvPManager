package me.chancesd.pvpmanager;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.jetbrains.annotations.NotNull;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class PluginSetup {

	private PvPManager plugin;
	private Server server;
	private World world;
	private Scoreboard scoreboard;
	private String filePath;
	private Player attacker;
	private Player defender;

	public final void setup() throws Exception {
		filePath = URLDecoder.decode(PluginSetup.class.getClassLoader().getResource("").getPath(), "UTF-8");
		final String decoded = filePath + "TestServer/plugins/PvPManager";
		final File pluginDirectory = new File(decoded);
		pluginDirectory.mkdirs();
		server = mock(Server.class, Mockito.RETURNS_MOCKS);
		Mockito.when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
		Mockito.when(server.getUpdateFolderFile()).thenReturn(new File(filePath + "TestServer/plugins/update"));
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT] [%4$-7s] %5$s %n");
		Mockito.when(server.getLogger()).thenReturn(Logger.getLogger("Minecraft"));
		Mockito.when(server.getBukkitVersion()).thenReturn("1.20.4-R0.1-SNAPSHOT");
		this.world = mock(World.class);
		Mockito.when(this.world.getName()).thenReturn("");
		Mockito.when(server.getWorlds()).thenReturn(Arrays.asList(this.world));
		final ScoreboardManager scoreboardManager = mock(ScoreboardManager.class);
		this.scoreboard = mock(Scoreboard.class, Mockito.RETURNS_MOCKS);
		Mockito.when(server.getScoreboardManager()).thenReturn(scoreboardManager);
		Mockito.when(scoreboardManager.getMainScoreboard()).thenReturn(this.scoreboard);
		Mockito.when(server.createBossBar(ArgumentMatchers.anyString(), ArgumentMatchers.any(org.bukkit.boss.BarColor.class), ArgumentMatchers.any(org.bukkit.boss.BarStyle.class))).thenReturn(mock(BossBar.class));
		Bukkit.setServer(server);

		plugin = Mockito.mock(PvPManager.class, Mockito.CALLS_REAL_METHODS);
		// Initialize registeredCommands field that doesn't get initialized in mocks
		final Field registeredCommandsField = plugin.getClass().getSuperclass().getDeclaredField("registeredCommands");
		registeredCommandsField.setAccessible(true);
		registeredCommandsField.set(plugin, new HashMap<>());
		@SuppressWarnings({ "resource" })
		final PluginDescriptionFile pdf = new PluginDescriptionFile(PluginSetup.class.getClassLoader().getResource("plugin.yml").openStream());
		final Method method = JavaPlugin.class.getDeclaredMethod("init", PluginLoader.class, Server.class, PluginDescriptionFile.class, File.class, File.class,
		        ClassLoader.class);
		method.setAccessible(true);
		method.invoke(plugin, (Object) null, server, pdf, pluginDirectory, new File(filePath), PluginSetup.class.getClassLoader());
		Mockito.doReturn(mock(PluginCommand.class)).when(plugin).getCommand(ArgumentMatchers.anyString());

		plugin.onPluginLoad();
		plugin.onEnable();
		setupPlayers();
	}

	public final void setupPlayers() {
		attacker = createPlayer("Attacker");
		defender = createPlayer("Defender");
		Mockito.when(defender.getKiller()).thenReturn(attacker);
	}

	@SuppressWarnings("deprecation")
	@NotNull
	public final Player createPlayer(final String name) {
		final Player player = mock(Player.class);
		Mockito.when(player.hasPlayedBefore()).thenReturn(true);
		doReturn(name).when(player).getName();
		Mockito.when(player.getDisplayName()).thenReturn(name);
		Mockito.when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		Mockito.when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
		Mockito.when(player.getWorld()).thenReturn(world);
		Mockito.when(player.getServer()).thenReturn(this.server);
		Mockito.when(player.getScoreboard()).thenReturn(this.scoreboard);
		Mockito.when(player.isOnline()).thenReturn(true);
		final PlayerInventory inventory = mock(PlayerInventory.class, Mockito.RETURNS_MOCKS);
		Mockito.when(player.getInventory()).thenReturn(inventory);
		final Player.Spigot spigot = mock(Player.Spigot.class);
		Mockito.when(player.spigot()).thenReturn(spigot);
		Mockito.when(player.getItemInHand()).thenReturn(mock(ItemStack.class, Mockito.RETURNS_MOCKS));
		return player;
	}

	public final void tearDown() {
		plugin.onDisable();
		deleteDir(new File(filePath + "TestServer"));
	}

	private boolean deleteDir(final File file) {
		if (file.isDirectory()) {
			final String[] children = file.list();
			if (children != null) {
				for (final String aChildren : children) {
					final boolean success = deleteDir(new File(file, aChildren));
					if (!success)
						return false;
				}
			}
		}
		return file.delete();
	}

	public Server getServer() {
		return server;
	}

	public final PvPManager getPlugin() {
		return plugin;
	}

	public Player getAttacker() {
		return attacker;
	}

	public Player getDefender() {
		return defender;
	}

}
