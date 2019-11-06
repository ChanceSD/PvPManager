package me.NoChance.PvPManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import me.NoChance.PvPManager.Libraries.Updater.Updater;
import me.NoChance.PvPManager.Libraries.Updater.Updater.UpdateType;

public class UpdaterTest {

	private UpdaterMock updater;
	private final PvPManager plugin = AllTests.getPt().getPlugin();

	@Before
	public final void setup() {
		updater = new UpdaterMock(plugin, UpdateType.VERSION_CHECK);
	}

	@Test
	public final void testUpdater() {
		assertTrue(updater.versionCheck("1.2.3", "1.2.4"));
		assertFalse(updater.versionCheck("1.2.3", "1.2.3"));
		assertFalse(updater.versionCheck("1.2.3", "1.2.2"));

		assertTrue(updater.versionCheck("1.2.3-SNAPSHOT", "1.2.4"));
		assertTrue(updater.versionCheck("1.2.3-SNAPSHOT", "1.2.3"));
		assertFalse(updater.versionCheck("1.2.3-SNAPSHOT", "1.2.2"));

		assertTrue(updater.versionCheck("1.2.3", "1.2.4-SNAPSHOT"));
		assertFalse(updater.versionCheck("1.2.3", "1.2.3-SNAPSHOT"));
		assertFalse(updater.versionCheck("1.2.3", "1.2.2-SNAPSHOT"));

		assertTrue(updater.versionCheck("1.2.3-SNAPSHOT", "1.2.4-SNAPSHOT"));
		assertFalse(updater.versionCheck("1.2.3-SNAPSHOT", "1.2.3-SNAPSHOT"));
		assertFalse(updater.versionCheck("1.2.3-SNAPSHOT", "1.2.2-SNAPSHOT"));
	}

	public class UpdaterMock extends Updater {

		protected UpdaterMock(final Plugin plugin, final UpdateType type) {
			super(plugin, 0, type);
		}

		public boolean versionCheck(final String localVersion, final String remoteVersion) {
			Mockito.doReturn(new PluginDescriptionFile("PvPManager", localVersion, "")).when(this.getPlugin()).getDescription();
			return versionCheck(remoteVersion);
		}

		@Override
		protected boolean read() {
			return false;
		}

		@Override
		public boolean downloadFile() {
			return false;
		}

	}

}
