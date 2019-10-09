package me.NoChance.PvPManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;

import me.NoChance.PvPManager.Managers.DependencyManager;

public class DependencyTest {

	private static Server server;

	@BeforeClass
	public static void setupClass() {
		final PluginTest pt = AllTests.getPt();
		server = pt.getServer();
		when(server.getServicesManager()).thenReturn(mock(ServicesManager.class));
	}

	@Before
	public void setup() {
		when(server.getPluginManager().getPlugin(Matchers.anyString())).thenReturn(null);
	}

	@Test
	public void allEnabled() {
		final JavaPlugin plugin = PowerMockito.mock(JavaPlugin.class);
		when(plugin.getDescription()).thenReturn(new PluginDescriptionFile("Plugin", "1.0", "plugin"));
		when(server.getPluginManager().getPlugin(Matchers.anyString())).thenReturn(plugin);
		new DependencyManager();
	}

	@AfterClass
	public static void cleanup() {
		when(server.getPluginManager().getPlugin(Matchers.anyString())).thenReturn(null);
	}

}
