package me.NoChance.PvPManager;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import me.NoChance.PvPManager.Listeners.EntityListenerTest;
import me.NoChance.PvPManager.Listeners.PlayerListenerTest;
import me.NoChance.PvPManager.Managers.DependencyTest;

@Suite
@SelectClasses({ UpdaterTest.class, DependencyTest.class, EntityListenerTest.class, PlayerListenerTest.class })
public final class InstanceCreator implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

	private static boolean started = false;
	public static PluginTest pt;

	private InstanceCreator() {
	}

	@Override
	public void beforeAll(final ExtensionContext context) throws Exception {
		if (!started) {
			started = true;
			pt = new PluginTest();
			pt.setup();
			context.getRoot().getStore(Namespace.GLOBAL).put("pvpmanager", this);
		}
	}

	@Override
	public void close() {
		pt.tearDown();
	}

	public static PluginTest getPt() {
		return pt;
	}

}
