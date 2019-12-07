package me.NoChance.PvPManager;

import org.bukkit.command.PluginCommand;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PvPManager.class, PluginCommand.class })
@PowerMockRunnerDelegate(Suite.class)
@SuiteClasses({ UpdaterTest.class, DependencyTest.class, DamageListenerTest.class, OnDeathTest.class })
public final class AllTests {

	private static PluginTest pt;

	private AllTests() {

	}

	public static PluginTest getPt() {
		return pt;
	}

	@ClassRule
	public static ExternalResource resource = new ExternalResource() {

		@Override
		protected void before() throws Throwable {
			pt = new PluginTest();
			pt.setup();
		}

		@Override
		protected void after() {
			pt.tearDown();
		}
	};

}
