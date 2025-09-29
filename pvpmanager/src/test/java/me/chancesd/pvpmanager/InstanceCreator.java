package me.chancesd.pvpmanager;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({ "me.chancesd.pvpmanager" })
public final class InstanceCreator implements BeforeAllCallback, AutoCloseable {

	private static boolean started = false;
	private static PluginSetup pt;

	private InstanceCreator() {
	}

	@Override
	public void beforeAll(final ExtensionContext context) throws Exception {
		if (!started) {
			started = true;
			pt = new PluginSetup();
			pt.setup();
			context.getRoot().getStore(Namespace.GLOBAL).put("pvpmanager", this);
		}
	}

	@Override
	public void close() {
		pt.tearDown();
	}

	public static PluginSetup getPt() {
		return pt;
	}

}
