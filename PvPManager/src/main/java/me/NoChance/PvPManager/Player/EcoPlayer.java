package me.NoChance.PvPManager.Player;

import java.text.DecimalFormat;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import net.milkbowl.vault.economy.Economy;

public abstract class EcoPlayer {

	private final Economy economy;

	protected EcoPlayer(final Economy economy) {
		this.economy = economy;
	}

	protected abstract Player getPlayer();

	public final boolean isOnline() {
		return getPlayer() != null;
	}

	public final void message(final String message) {
		if (isOnline() && !message.isEmpty()) {
			getPlayer().sendMessage(message);
		}
	}

	private void withdrawMoney(final double amount) {
		economy.withdrawPlayer(getPlayer(), amount);
	}

	private void depositMoney(final double amount) {
		economy.depositPlayer(getPlayer(), amount);
	}

	public final void applyFine() {
		withdrawMoney(Settings.getFineAmount());
	}

	public final void applyPenalty() {
		withdrawMoney(Settings.getMoneyPenalty() >= 1 ? Settings.getMoneyPenalty() : Settings.getMoneyPenalty() * economy.getBalance(getPlayer()));
		message(Messages.getMoneyPenalty().replace("%m", Double.toString(Settings.getMoneyPenalty())));
	}

	public final void giveReward(final Player victim) {
		double moneyWon = Settings.getMoneyReward();
		if (Settings.getMoneyReward() >= 1) {
			depositMoney(Settings.getMoneyReward());
		} else {
			moneyWon = Settings.getMoneyReward() * economy.getBalance(victim);
			depositMoney(moneyWon);
			economy.withdrawPlayer(victim, moneyWon);
			victim.sendMessage("§cPlayer " + getPlayer().getName() + " stole " + moneyWon + " coins from you!");
		}
		final DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		message(Messages.getMoneyReward().replace("%m", df.format(moneyWon)).replace("%p", victim.getName()));
	}

}
