package me.NoChance.PvPManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URLDecoder;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

public class PluginTest {

	private PvPManager plugin;
	private Server server;
	private String filePath;
	private static PluginTest instance;
	private Player attacker;
	private Player defender;

	public final void setup() throws Exception {
		filePath = URLDecoder.decode(PluginTest.class.getClassLoader().getResource("").getPath(), "UTF-8");
		final String decoded = filePath + "TestServer/plugins/PvPManager";
		final File pluginDirectory = new File(decoded);
		pluginDirectory.mkdirs();
		server = mock(Server.class, Mockito.RETURNS_MOCKS);
		Mockito.when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
		Mockito.when(server.getUpdateFolderFile()).thenReturn(new File(filePath + "TestServer/plugins/update"));
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT] [%4$-7s] %5$s %n");
		Mockito.when(server.getLogger()).thenReturn(Logger.getLogger("Minecraft"));
		Bukkit.setServer(server);
		plugin = PowerMockito.mock(PvPManager.class, Mockito.CALLS_REAL_METHODS);
		final PluginDescriptionFile pdf = new PluginDescriptionFile(PluginTest.class.getClassLoader().getResource("plugin.yml").openStream());
		Whitebox.invokeMethod(plugin, "init", (Object) null, server, pdf, pluginDirectory, new File(filePath), PluginTest.class.getClassLoader());
		Mockito.doReturn(mock(PluginCommand.class)).when(plugin).getCommand(Matchers.anyString());
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

	public final void tearDown() {
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

	public static PluginTest getInstance() {
		return instance;
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
