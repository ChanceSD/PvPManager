package me.chancesd.pvpmanager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class PluginSetup {

	private PvPManager plugin;
	private Server server;
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
		final World world = mock(World.class);
		Mockito.when(world.getName()).thenReturn("");
		Mockito.when(server.getWorlds()).thenReturn(Arrays.asList(world));
		Bukkit.setServer(server);
		plugin = Mockito.mock(PvPManager.class, Mockito.CALLS_REAL_METHODS);
		@SuppressWarnings({ "resource", "null" })
		final PluginDescriptionFile pdf = new PluginDescriptionFile(PluginSetup.class.getClassLoader().getResource("plugin.yml").openStream());
		final Method method = JavaPlugin.class.getDeclaredMethod("init", PluginLoader.class, Server.class, PluginDescriptionFile.class, File.class, File.class,
		        ClassLoader.class);
		method.setAccessible(true);
		method.invoke(plugin, (Object) null, server, pdf, pluginDirectory, new File(filePath), PluginSetup.class.getClassLoader());
		Mockito.doReturn(mock(PluginCommand.class)).when(plugin).getCommand(ArgumentMatchers.anyString());
		plugin.onLoad();
		plugin.onEnable();
		setupPlayers();
	}

	public final void setupPlayers() {
		attacker = mock(Player.class, Mockito.RETURNS_MOCKS);
		defender = mock(Player.class, Mockito.RETURNS_MOCKS);
		when(attacker.hasPlayedBefore()).thenReturn(true);
		when(defender.hasPlayedBefore()).thenReturn(true);
		when(attacker.getName()).thenReturn("Attacker");
		when(defender.getName()).thenReturn("Defender");
		when(attacker.getUniqueId()).thenReturn(UUID.randomUUID());
		when(defender.getUniqueId()).thenReturn(UUID.randomUUID());
		when(attacker.getGameMode()).thenReturn(GameMode.SURVIVAL);
		when(defender.getKiller()).thenReturn(attacker);
	}

	@NotNull
	public final Player createPlayer(final String name) {
		final Player player = mock(Player.class, Mockito.RETURNS_MOCKS);
		when(player.hasPlayedBefore()).thenReturn(true);
		when(player.getName()).thenReturn(name);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
		return player;
	}

	@NotNull
	public final Player createPlayer(final String name, final Player killer) {
		final Player player = createPlayer(name);
		when(player.getKiller()).thenReturn(killer);
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
