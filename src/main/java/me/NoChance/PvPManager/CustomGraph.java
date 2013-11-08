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

			updateCheck.addPlotter(new Metrics.Plotter("Auto Update") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.updateCheck && Variables.autoUpdate)
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
			updateCheck.addPlotter(new Metrics.Plotter("Update Check") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.updateCheck && !Variables.autoUpdate)
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
			
			if (Variables.punishmentsEnabled) {
				keepItemsExp.addPlotter(new Metrics.Plotter("Keep Everything") {
					@Override
					public int getValue() {
						int i = 0;
						if (!Variables.dropInventory && !Variables.dropExp && !Variables.dropArmor)
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Drop Everything") {
					@Override
					public int getValue() {
						int i = 0;
						if (Variables.dropInventory && Variables.dropExp && Variables.dropArmor)
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Keep Inventory/Armor and Drop Exp") {
					@Override
					public int getValue() {
						int i = 0;
						if (!Variables.dropInventory && !Variables.dropArmor && Variables.dropExp)
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Keep Inventory/Exp and Drop Armor") {
					@Override
					public int getValue() {
						int i = 0;
						if (!Variables.dropInventory && !Variables.dropExp && Variables.dropArmor)
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Keep Exp/Armor and Drop Inventory") {
					@Override
					public int getValue() {
						int i = 0;
						if (Variables.dropInventory && !Variables.dropExp && !Variables.dropArmor)
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Drop Exp/Armor and Keep Inventory") {
					@Override
					public int getValue() {
						int i = 0;
						if (!Variables.dropInventory && Variables.dropExp && Variables.dropArmor)
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Drop Exp/Inventory and Keep Armor") {
					@Override
					public int getValue() {
						int i = 0;
						if (Variables.dropInventory && Variables.dropExp && !Variables.dropArmor)
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Drop Inventory/Armor and Keep Exp") {
					@Override
					public int getValue() {
						int i = 0;
						if (Variables.dropInventory && !Variables.dropExp && Variables.dropArmor)
							i++;

						return i;
					}
				});
			}
			metrics.start();
		} catch (IOException e) {
		}
	}
}
