package me.NoChance.PvPManager.Lib;

import java.io.IOException;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Lib.Metrics.Graph;

public class CustomMetrics {

	private final PvPManager plugin;

	public CustomMetrics(final PvPManager plugin) {
		this.plugin = plugin;
		initMetrics();
	}

	public final void initMetrics() {
		try {
			final Metrics metrics = new Metrics(plugin);
			final Graph keepItemsExp = metrics.createGraph("Percentage of Keep and Drop");
			final Graph inCombatTime = metrics.createGraph("Time in Combat");
			final Graph toggleSigns = metrics.createGraph("Toggle Signs Usage");
			final Graph newbieProtection = metrics.createGraph("Newbie Protection Usage");
			final Graph updateCheck = metrics.createGraph("Update Check Usage");
			final Graph killAbuse = metrics.createGraph("Kill Abuse Usage");
			final Graph autoSoup = metrics.createGraph("Auto Soup Usage");

			autoSoup.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.isAutoSoupEnabled())
						i++;

					return i;
				}
			});
			autoSoup.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.isAutoSoupEnabled())
						i++;

					return i;
				}
			});

			killAbuse.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.isKillAbuseEnabled())
						i++;

					return i;
				}
			});
			killAbuse.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.isKillAbuseEnabled())
						i++;

					return i;
				}
			});

			updateCheck.addPlotter(new Metrics.Plotter("Auto Update") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.isUpdateCheck() && Variables.isAutoUpdate())
						i++;

					return i;
				}
			});
			updateCheck.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.isUpdateCheck())
						i++;

					return i;
				}
			});
			updateCheck.addPlotter(new Metrics.Plotter("Update Check") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.isUpdateCheck() && !Variables.isAutoUpdate())
						i++;

					return i;
				}
			});

			newbieProtection.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.isNewbieProtectionEnabled())
						i++;

					return i;
				}
			});
			newbieProtection.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.isNewbieProtectionEnabled())
						i++;

					return i;
				}
			});

			toggleSigns.addPlotter(new Metrics.Plotter("Enabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.isToggleSignsEnabled())
						i++;

					return i;
				}
			});
			toggleSigns.addPlotter(new Metrics.Plotter("Disabled") {
				@Override
				public int getValue() {
					int i = 0;
					if (!Variables.isToggleSignsEnabled())
						i++;

					return i;
				}
			});

			inCombatTime.addPlotter(new Metrics.Plotter(Variables.getTimeInCombat() + " seconds") {
				@Override
				public int getValue() {
					int i = 0;
					if (Variables.isInCombatEnabled())
						i++;

					return i;
				}
			});

			if (Variables.isPunishmentsEnabled() && Variables.isInCombatEnabled()) {
				keepItemsExp.addPlotter(new Metrics.Plotter("Keep Everything") {
					@Override
					public int getValue() {
						int i = 0;
						if (!Variables.isDropInventory() && !Variables.isDropExp() && !Variables.isDropArmor())
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Drop Everything") {
					@Override
					public int getValue() {
						int i = 0;
						if (Variables.isDropInventory() && Variables.isDropExp() && Variables.isDropArmor())
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Keep Inventory/Armor and Drop Exp") {
					@Override
					public int getValue() {
						int i = 0;
						if (!Variables.isDropInventory() && !Variables.isDropArmor() && Variables.isDropExp())
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Keep Inventory/Exp and Drop Armor") {
					@Override
					public int getValue() {
						int i = 0;
						if (!Variables.isDropInventory() && !Variables.isDropExp() && Variables.isDropArmor())
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Keep Exp/Armor and Drop Inventory") {
					@Override
					public int getValue() {
						int i = 0;
						if (Variables.isDropInventory() && !Variables.isDropExp() && !Variables.isDropArmor())
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Drop Exp/Armor and Keep Inventory") {
					@Override
					public int getValue() {
						int i = 0;
						if (!Variables.isDropInventory() && Variables.isDropExp() && Variables.isDropArmor())
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Drop Exp/Inventory and Keep Armor") {
					@Override
					public int getValue() {
						int i = 0;
						if (Variables.isDropInventory() && Variables.isDropExp() && !Variables.isDropArmor())
							i++;

						return i;
					}
				});
				keepItemsExp.addPlotter(new Metrics.Plotter("Drop Inventory/Armor and Keep Exp") {
					@Override
					public int getValue() {
						int i = 0;
						if (Variables.isDropInventory() && !Variables.isDropExp() && Variables.isDropArmor())
							i++;

						return i;
					}
				});
			}
			metrics.start();
		} catch (final IOException e) {
		}
	}
}
