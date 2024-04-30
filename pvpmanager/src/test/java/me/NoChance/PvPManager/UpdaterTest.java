package me.NoChance.PvPManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.sdutils.updater.Updater;
import me.chancesd.sdutils.updater.Updater.UpdateType;

@ExtendWith(InstanceCreator.class)
class UpdaterTest {

	private UpdaterMock updater;
	private final PvPManager plugin = InstanceCreator.getPt().getPlugin();

	@BeforeEach
	public final void setup() {
		updater = new UpdaterMock(plugin, UpdateType.VERSION_CHECK);
	}

	@Test
	final void testUpdater() {
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

	public static class UpdaterMock extends Updater {

		protected UpdaterMock(final Plugin plugin, final UpdateType type) {
			super(plugin, 0, type, plugin.getName());
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

		@Override
		public String getUpdateLink() {
			return "Test";
		}

	}

}
