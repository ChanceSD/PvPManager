package me.NoChance.PvPManager;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.net.URLDecoder;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

public class PluginTest {

	private PvPManager plugin;
	private final Logger logger = Logger.getLogger("PvPManager-Test");
	private String filePath;
	private static PluginTest instance;

	public PluginTest() {
	}

	public final void setup() throws Exception {
		filePath = URLDecoder.decode(PluginTest.class.getClassLoader().getResource("").getPath(), "UTF-8");
		String decoded = filePath + "TestServer/plugins/PvPManager";
		File pluginDirectory = new File(decoded);
		pluginDirectory.mkdirs();
		Server server = mock(Server.class, Mockito.RETURNS_MOCKS);
		Mockito.when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
		Bukkit.setServer(server);
		plugin = PowerMockito.mock(PvPManager.class, Mockito.CALLS_REAL_METHODS);
		PluginDescriptionFile pdf = new PluginDescriptionFile(PluginTest.class.getClassLoader().getResource("plugin.yml").openStream());
		Whitebox.invokeMethod(plugin, "init", (Object) null, server, pdf, pluginDirectory, new File(filePath), PluginTest.class.getClassLoader());
		Mockito.doReturn(logger).when(plugin).getLogger();
		Mockito.doReturn(mock(PluginCommand.class)).when(plugin).getCommand(Matchers.anyString());
		plugin.onEnable();
		System.out.println(pdf.getVersion());
	}

	public final void tearDown() {
		deleteDir(new File(filePath + "TestServer"));
	}

	private boolean deleteDir(final File file) {
		if (file.isDirectory()) {
			String[] children = file.list();
			if (children != null)
				for (String aChildren : children) {
					boolean success = deleteDir(new File(file, aChildren));
					if (!success) {
						return false;
					}
				}
		}
		return file.delete();
	}

	public static PluginTest getInstance() {
		return instance;
	}

	public final PvPManager getPlugin() {
		return plugin;
	}

}
