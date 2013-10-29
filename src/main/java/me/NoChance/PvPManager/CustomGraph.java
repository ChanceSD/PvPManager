package me.NoChance.PvPManager;

import java.io.IOException;

import me.NoChance.PvPManager.Config.Variables;

import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;

public class CustomGraph {

	private PvPManager plugin;

	public CustomGraph(PvPManager plugin) {
		this.plugin = plugin;
		initMetrics();
	}

	public void initMetrics() {
		try {
			Metrics metrics = new Metrics(plugin);
			Graph keepItemsExp = metrics.createGraph("Percentage of Keep and Drop");
			Graph pvpTimerUsage = metrics.createGraph("PvPTimer Usage");
			Graph inCombatTime = metrics.createGraph("Time in Combat");
			Graph toggleSigns = metrics.createGraph("Toggle Signs Usage");
			Graph newbieProtection = metrics.createGraph("Newbie Protection Usage");
			Graph updateCheck = metrics.createGraph("Update Check Usage");
			
			updateCheck.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.updateCheck)
						i++;

					return i;
				}
			});

			updateCheck.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.updateCheck)
						i++;

					return i;
				}
			});
			
			newbieProtection.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.newbieProtectionEnabled)
						i++;

					return i;
				}
			});

			newbieProtection.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.newbieProtectionEnabled)
						i++;

					return i;
				}
			});

			toggleSigns.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.toggleSignsEnabled)
						i++;

					return i;
				}
			});

			toggleSigns.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.toggleSignsEnabled)
						i++;

					return i;
				}
			});

			inCombatTime.addPlotter(new Metrics.Plotter(Variables.timeInCombat + " seconds") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.inCombatEnabled)
						i++;

					return i;
				}
			});

			pvpTimerUsage.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.pvpTimerEnabled)
						i++;

					return i;
				}
			});

			pvpTimerUsage.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.pvpTimerEnabled)
						i++;

					return i;
				}
			});

			keepItemsExp.addPlotter(new Metrics.Plotter("Keep Everything") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.keepItems && Variables.keepExp && Variables.punishmentsEnabled)
						i++;

					return i;
				}
			});
			keepItemsExp.addPlotter(new Metrics.Plotter("Drop Everything") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.keepItems && !Variables.keepExp && Variables.punishmentsEnabled)
						i++;

					return i;
				}
			});
			keepItemsExp.addPlotter(new Metrics.Plotter("Keep Items and Drop Exp") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.keepItems && !Variables.keepExp && Variables.punishmentsEnabled)
						i++;

					return i;
				}
			});
			keepItemsExp.addPlotter(new Metrics.Plotter("Keep Exp and Drop Items") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.keepItems && Variables.keepExp && Variables.punishmentsEnabled)
						i++;

					return i;
				}
			});
			metrics.start();
		} catch (IOException e) {
		}
	}
}
