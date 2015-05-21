package me.NoChance.Test;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Managers.PlayerHandler;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class PluginTest {

	@Mock
	private PlayerHandler playerHandler;
	@InjectMocks
	private PvPManager plugin;
	private static PluginTest instance;

	public PluginTest() {
		setup();
	}

	@BeforeClass
	public final void setup() {
		MockitoAnnotations.initMocks(this);
	}

	public static PluginTest getInstance() {
		if (instance == null)
			instance = new PluginTest();
		return instance;
	}

	public final PvPManager getPlugin() {
		return plugin;
	}

	public final PlayerHandler getPh() {
		return playerHandler;
	}

}
