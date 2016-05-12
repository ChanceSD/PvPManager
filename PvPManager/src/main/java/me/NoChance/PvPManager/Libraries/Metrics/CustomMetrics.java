package me.NoChance.PvPManager.Libraries.Metrics;

import java.io.IOException;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Libraries.Metrics.Metrics.Graph;
import me.NoChance.PvPManager.Settings.Settings;

public class CustomMetrics {

	private final PvPManager plugin;

	public CustomMetrics(final PvPManager plugin) {
		this.plugin = plugin;
		initMetrics();
	}

	private void initMetrics() {
		try {
			final Metrics metrics = new Metrics(plugin);
			final Graph inCombatTime = metrics.createGraph("Time in Combat");
			final Graph newbieProtection = metrics.createGraph("Newbie Protection Usage");
			final Graph updateCheck = metrics.createGraph("Update Check Usage");
			final Graph killAbuse = metrics.createGraph("Kill Abuse Usage");
			final Graph blood = metrics.createGraph("Blood Usage");

			blood.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					return Settings.isPvpBlood() ? 1 : 0;
				}

			});
			blood.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					return !Settings.isPvpBlood() ? 1 : 0;
				}
			});

			killAbuse.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (Settings.isKillAbuseEnabled())
						i++;

					return i;
				}
			});
			killAbuse.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Settings.isKillAbuseEnabled())
						i++;

					return i;
				}
			});

			updateCheck.addPlotter(new Metrics.Plotter("Auto Update") {
				@Override
				public int getValue() {
					int i = 0;
					if (Settings.isUpdateCheck() && Settings.isAutoUpdate())
						i++;

					return i;
				}
			});
			updateCheck.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Settings.isUpdateCheck())
						i++;

					return i;
				}
			});
			updateCheck.addPlotter(new Metrics.Plotter("Update Check") {
				@Override
				public int getValue() {
					int i = 0;
					if (Settings.isUpdateCheck() && !Settings.isAutoUpdate())
						i++;

					return i;
				}
			});

			newbieProtection.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (Settings.isNewbieProtectionEnabled())
						i++;

					return i;
				}
			});
			newbieProtection.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Settings.isNewbieProtectionEnabled())
						i++;

					return i;
				}
			});

			inCombatTime.addPlotter(new Metrics.Plotter(Settings.getTimeInCombat() + " seconds") {
				@Override
				public int getValue() {
					int i = 0;
					if (Settings.isInCombatEnabled())
						i++;

					return i;
				}
			});

			metrics.start();
		} catch (final IOException ignored) {
		}
	}
}
