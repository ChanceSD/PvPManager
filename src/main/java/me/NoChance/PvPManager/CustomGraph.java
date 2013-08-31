package me.NoChance.PvPManager;

import java.io.IOException;

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
