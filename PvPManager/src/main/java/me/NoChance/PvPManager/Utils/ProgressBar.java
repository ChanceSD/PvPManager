package me.NoChance.PvPManager.Utils;

import com.google.common.base.Strings;

public class ProgressBar {

	private final int totalBars;
	private final int goal;
	private final String symbol;
	private double progress;
	private String message;

	public ProgressBar(final String message, final int totalBars, final int goal, final String symbol) {
		this.message = message;
		this.totalBars = totalBars;
		this.goal = goal;
		this.symbol = symbol;
	}

	public ProgressBar(final String message, final int totalBars, final int goal, final String symbol, final int secondsPassed) {
		this(message, totalBars, goal, symbol);
		setProgress(secondsPassed);
	}

	public void setProgress(final double progress) {
		final double percent = progress / goal;
		final int progressBars = (int) (totalBars * percent);
		message = message.replace("<barsLeft>", Strings.repeat(symbol, totalBars - progressBars)).replace("<barsPassed>", Strings.repeat(symbol, progressBars))
		        .replace("<time>", Integer.toString((int) (goal - progress)));
		this.progress = progress;
	}

	public double getProgress() {
		return progress;
	}

	public String getMessage() {
		return message;
	}

	public int getTotalBars() {
		return totalBars;
	}

	public String getSymbol() {
		return symbol;
	}

	public int getGoal() {
		return goal;
	}

	@Override
	public String toString() {
		return message;
	}

}
