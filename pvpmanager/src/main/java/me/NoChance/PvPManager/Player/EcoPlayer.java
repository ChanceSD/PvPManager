package me.NoChance.PvPManager.Player;

import org.bukkit.entity.Player;

import me.NoChance.PvPManager.Settings.Messages;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.chancesd.sdutils.utils.Log;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public abstract class EcoPlayer extends BasePlayer {

	private final Economy economy;

	protected EcoPlayer(final Player player, final Economy economy) {
		super(player);
		this.economy = economy;
	}

	private boolean withdrawMoney(final double amount) {
		final EconomyResponse response = economy.withdrawPlayer(getPlayer(), amount);
		Log.debug(
		        "Withdraw money from " + getName() + " - Response: " + response.type + " " + response.amount + " " + response.balance + " " + response.errorMessage);
		return response.transactionSuccess() && response.balance > 0;
	}

	private void depositMoney(final double amount) {
		final EconomyResponse response = economy.depositPlayer(getPlayer(), amount);
		Log.debug("Deposit money to " + getName() + " - Response: " + response.type + " " + response.amount + " " + response.balance + " "
				+ response.errorMessage);
	}

	public final void applyFine() {
		final double penalty = getMoneyPercentage(Settings.getFineAmount());
		withdrawMoney(penalty);
	}

	public final void applyPenalty() {
		final double penalty = getMoneyPercentage(Settings.getMoneyPenalty());
		withdrawMoney(penalty);
		message(Messages.getMoneyPenalty().replace("%m", CombatUtils.formatTo2Digits(penalty)));
	}

	public final boolean applyPvPDisabledFee() {
		message(Messages.getPvPDisabledFee().replace("%money", CombatUtils.formatTo2Digits(Settings.getPvPDisabledFee())));
		return withdrawMoney(Settings.getPvPDisabledFee());
	}

	public final void giveReward(final EcoPlayer victim) {
		double moneyWon = getMoneyPercentage(Settings.getMoneyReward());
		if (Settings.isMoneySteal()) {
			final double vbalance = economy.getBalance(victim.getPlayer());
			if (Settings.getMoneyReward() <= 1) {
				moneyWon = roundMoney(Settings.getMoneyReward() * vbalance);
			} else if (Settings.getMoneyReward() > vbalance) {
				moneyWon = vbalance;
			}
			victim.withdrawMoney(moneyWon);
			victim.message(Messages.getMoneySteal(getPlayer().getName(), CombatUtils.formatTo2Digits(moneyWon)));
		}
		depositMoney(moneyWon);
		message(Messages.getMoneyReward(victim.getPlayer().getName(), CombatUtils.formatTo2Digits(moneyWon)));
	}

	public final int giveExp(final EcoPlayer victim) {
		int expWon = 0;
		final int exp = victim.getPlayer().getTotalExperience();
		if (Settings.getExpSteal() <= 1) {
			expWon = (int) (Settings.getExpSteal() * exp);
		} else {
			expWon = exp;
		}
		setExp(getPlayer().getTotalExperience() + expWon);
		message(Messages.getExpWon(victim.getPlayer().getName(), String.valueOf(expWon)));
		return expWon;
	}

	public final void setExp(final int exp) {
		getPlayer().setExp(0);
		getPlayer().setLevel(0);
		getPlayer().giveExp(exp);
	}

	private double getMoneyPercentage(final double percentage) {
		if (percentage > 1)
			return percentage;
		return roundMoney(percentage * economy.getBalance(getPlayer()));
	}

	private double roundMoney(final double money) {
		return Math.round(money * 100.0) / 100.0;
	}

}
