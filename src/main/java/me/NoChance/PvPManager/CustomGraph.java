package me.NoChance.PvPManager;

import java.io.IOException;

import me.NoChance.PvPManager.Config.Variables;

import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;

public class CustomGraph {
	
	private PvPManager plugin;
	
	public CustomGraph(PvPManager plugin){
		this.plugin = plugin;
		initMetrics();
	}
	
	public void initMetrics(){
	try {
	Metrics metrics = new Metrics(plugin);
	Graph keepItemsExp = metrics.createGraph("Percentage of Keep and Drop");
	Graph pvpTimerUsage = metrics.createGraph("PvPTimer Usage");
	Graph disableFly = metrics.createGraph("Disable Fly feature");
	Graph inCombatTime = metrics.createGraph("Time in Combat");
	
	inCombatTime.addPlotter(new Metrics.Plotter("15 seconds") {
		@Override
		public int getValue() {
			int i = 0;
			if (Variables.timeInCombat == 15)
				i++;
			
			return i;
		}
	});
	
	inCombatTime.addPlotter(new Metrics.Plotter("Between 10 and 15 seconds") {
		@Override
		public int getValue() {
			int i = 0;
			if (Variables.timeInCombat < 15 && Variables.timeInCombat >= 10)
				i++;
			
			return i;
		}
	});
	
	inCombatTime.addPlotter(new Metrics.Plotter("Less than 10 seconds") {
		@Override
		public int getValue() {
			int i = 0;
			if (Variables.timeInCombat < 10)
				i++;
			
			return i;
		}
	});
	
	inCombatTime.addPlotter(new Metrics.Plotter("Between 15 and 30 seconds") {
		@Override
		public int getValue() {
			int i = 0;
			if (Variables.timeInCombat > 15 && Variables.timeInCombat <= 30)
				i++;
			
			return i;
		}
	});
	
	inCombatTime.addPlotter(new Metrics.Plotter("Between 31 and 45 seconds") {
		@Override
		public int getValue() {
			int i = 0;
			if (Variables.timeInCombat > 30 && Variables.timeInCombat <= 45)
				i++;
			
			return i;
		}
	});
	
	inCombatTime.addPlotter(new Metrics.Plotter("Between 46 and 60 seconds") {
		@Override
		public int getValue() {
			int i = 0;
			if (Variables.timeInCombat > 45 && Variables.timeInCombat <= 60)
				i++;
			
			return i;
		}
	});
	
	inCombatTime.addPlotter(new Metrics.Plotter("More than 60 seconds") {
		@Override
		public int getValue() {
			int i = 0;
			if (Variables.timeInCombat > 60)
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
	
	disableFly.addPlotter(new Metrics.Plotter("Enabled") {
		@Override
		public int getValue() {
			int i = 0;
			if (Variables.disableFly)
				i++;
			
			return i;
		}
	});
	
	disableFly.addPlotter(new Metrics.Plotter("Disabled") {
		@Override
		public int getValue() {
			int i = 0;
			if (!Variables.disableFly)
				i++;
			
			return i;
		}
	});
	
	keepItemsExp.addPlotter(new Metrics.Plotter("Keep Everything") {
		@Override
		public int getValue() {
			int i = 0;
			if (Variables.keepItems && Variables.keepExp)
				i++;
			
			return i;
		}
	});
	keepItemsExp.addPlotter(new Metrics.Plotter("Drop Everything") {
		@Override
		public int getValue() {
			int i = 0;
			if (!Variables.keepItems && !Variables.keepExp)
				i++;
			
			return i;
		}
	});
	keepItemsExp.addPlotter(new Metrics.Plotter(
			"Keep Items and Drop Exp") {
		@Override
		public int getValue() {
			int i = 0;
			if (Variables.keepItems && !Variables.keepExp)
				i++;
			
			return i;
		}
	});
	keepItemsExp.addPlotter(new Metrics.Plotter(
			"Keep Exp and Drop Items") {
		@Override
		public int getValue() {
			int i = 0;
			if (!Variables.keepItems && Variables.keepExp)
				i++;
			
			return i;
		}
	});	
	metrics.start();
} catch (IOException e) {
}
}
}
