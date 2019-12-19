package me.NoChance.PvPManager.Player;

import java.text.DecimalFormat;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import net.milkbowl.vault.economy.Economy;

public abstract class EcoPlayer extends BasePlayer {

	private final Economy economy;
	private final DecimalFormat df = new DecimalFormat();

	protected EcoPlayer(final Player player, final Economy economy) {
		super(player);
		this.economy = economy;
		df.setMaximumFractionDigits(2);
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
		final double penalty = Settings.getMoneyPenalty() >= 1 ? Settings.getMoneyPenalty() : Settings.getMoneyPenalty() * economy.getBalance(getPlayer());
		withdrawMoney(penalty);
		message(Messages.getMoneyPenalty().replace("%m", df.format(penalty)));
	}

	public final void giveReward(final EcoPlayer victim) {
		double moneyWon = Settings.getMoneyReward() >= 1 ? Settings.getMoneyReward() : Settings.getMoneyReward() * economy.getBalance(getPlayer());
		if (Settings.isMoneySteal()) {
			if (Settings.getMoneyReward() < 1) {
				moneyWon = Settings.getMoneyReward() * economy.getBalance(victim.getPlayer());
			}
			economy.withdrawPlayer(victim.getPlayer(), moneyWon);
			victim.message(Messages.getMoneySteal().replace("%p", getPlayer().getName()).replace("%m", df.format(moneyWon)));
		}
		depositMoney(moneyWon);
		message(Messages.getMoneyReward().replace("%m", df.format(moneyWon)).replace("%p", victim.getPlayer().getName()));
	}

}
