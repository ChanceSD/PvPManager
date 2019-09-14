package me.NoChance.PvPManager.Libraries.Metrics;

import java.util.concurrent.Callable;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Settings.Settings;

public class CustomMetrics {

	private final PvPManager plugin;

	public CustomMetrics(final PvPManager plugin) {
		this.plugin = plugin;
		initMetrics();
	}

	private void initMetrics() {

		final Metrics metrics = new Metrics(plugin);
//		final Graph inCombatTime = metrics.createGraph("Time in Combat");
//		final Graph newbieProtection = metrics.createGraph("Newbie Protection Usage");
//		final Graph updateCheck = metrics.createGraph("Update Check Usage");
//		final Graph killAbuse = metrics.createGraph("Kill Abuse Usage");
//		final Graph blood = metrics.createGraph("Blood Usage");

		metrics.addCustomChart(new Metrics.SimplePie("number_of_servers", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return Settings.isInCombatEnabled() ? Settings.getTimeInCombat() + " seconds" : "Disabled";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("number_of_servers", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return Settings.isNewbieProtectionEnabled() ? "Enabled" : "Disabled";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("number_of_servers", new Callable<String>() {
			@Override
			public String call() throws Exception {
				if (!Settings.isUpdateCheck())
					return "Disabled";
				return !Settings.isAutoUpdate() ? "Update Check" : "Auto Update";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("number_of_servers", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return Settings.isKillAbuseEnabled() ? "Enabled" : "Disabled";
			}
		}));

		metrics.addCustomChart(new Metrics.SimplePie("number_of_servers", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return Settings.isPvpBlood() ? "Enabled" : "Disabled";
			}
		}));

	}
}
