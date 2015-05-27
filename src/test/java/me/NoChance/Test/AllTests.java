package me.NoChance.Test;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Utils.CombatUtils;

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
@PrepareForTest({ PvPManager.class, CombatUtils.class, PluginCommand.class })
@PowerMockRunnerDelegate(Suite.class)
@SuiteClasses({ DamageListenerTest.class, OnDeathTest.class })
public class AllTests {

	public static PluginTest pt;

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
