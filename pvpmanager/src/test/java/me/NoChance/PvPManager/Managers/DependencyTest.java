package me.NoChance.PvPManager.Managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import me.NoChance.PvPManager.InstanceCreator;
import me.NoChance.PvPManager.PluginTest;
import me.NoChance.PvPManager.Utils.CombatUtils;

@ExtendWith(InstanceCreator.class)
public class DependencyTest {

	private static Server server;

	@BeforeAll
	public static void setupClass() {
		final PluginTest pt = InstanceCreator.getPt();
		server = pt.getServer();
		when(server.getServicesManager()).thenReturn(mock(ServicesManager.class));
	}

	@Test
	void allEnabled() {
		final JavaPlugin plugin = Mockito.mock(JavaPlugin.class);
		when(plugin.getDescription()).thenReturn(new PluginDescriptionFile("Plugin", "1.0", "plugin"));
		when(server.getPluginManager().getPlugin(ArgumentMatchers.anyString())).thenReturn(plugin);
		assertEquals(server.getPluginManager().getPlugin("PvPManager"), plugin);
		new DependencyManager();
	}

	@Test
	void versionTags() {
		final String v1 = CombatUtils.stripTags("1.0.2-SNAPSHOT");
		final String v2 = CombatUtils.stripTags("1.0.2;1994-9adac4f");
		final String v3 = CombatUtils.stripTags("1.0.2+9adac4f");
		assertEquals("1.0.2", v1);
		assertEquals("1.0.2", v2);
		assertEquals("1.0.2", v3);
	}

	@AfterAll
	public static void cleanup() {
		when(server.getPluginManager().getPlugin(ArgumentMatchers.anyString())).thenReturn(null);
	}

}
