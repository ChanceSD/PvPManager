package me.chancesd.pvpmanager.player;


import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public abstract class EcoPlayer extends BasePlayer {

	private final Economy economy;

	protected EcoPlayer(final @NotNull Player player, final Economy economy) {
		super(player);
		this.economy = economy;
	}

	private boolean withdrawMoney(final double amount) {
		final EconomyResponse response = economy.withdrawPlayer(getPlayer(), amount);
		Log.debug(
		        "Withdraw from " + getName() + " - Response: " + response.type + " " + response.amount + " " + response.balance + " " + response.errorMessage);
		return response.transactionSuccess() && response.balance > 0;
	}

	private boolean depositMoney(final double amount) {
		final EconomyResponse response = economy.depositPlayer(getPlayer(), amount);
		Log.debug("Deposit to " + getName() + " - Response: " + response.type + " " + response.amount + " " + response.balance + " "
				+ response.errorMessage);
		return response.transactionSuccess();
	}

	public double getBalance() {
		return economy.getBalance(getPlayer());
	}

	public final void applyFine() {
		final double penalty = getMoneyPercentage(Conf.FINE_AMOUNT.asDouble());
		withdrawMoney(penalty);
	}

	public final void applyPenalty() {
		final double penalty = getMoneyPercentage(Conf.MONEY_PENALTY.asDouble());
		withdrawMoney(penalty);
		message(Lang.MONEY_PENALTY, CombatUtils.formatTo2Digits(penalty));
	}

	public final boolean applyPvPDisabledFee() {
		message(Lang.PVP_DISABLED_FEE, CombatUtils.formatTo2Digits(Conf.PVP_DISABLED_FEE.asInt()));
		return withdrawMoney(Conf.PVP_DISABLED_FEE.asInt());
	}

	public final void giveReward(final EcoPlayer victim) {
		double moneyWon = getMoneyPercentage(Conf.MONEY_REWARD.asDouble());
		if (Conf.MONEY_STEAL.asBool()) {
			moneyWon = victim.getMoneyPercentage(Conf.MONEY_REWARD.asDouble());
			if (!victim.withdrawMoney(moneyWon)) {
				return;
			}
			victim.message(Lang.MONEY_STEAL, getPlayer().getName(), CombatUtils.formatTo2Digits(moneyWon));
		}
		if (depositMoney(moneyWon)) {
			message(Lang.MONEY_REWARD.msg(victim.getPlayer().getName(), CombatUtils.formatTo2Digits(moneyWon)));
		}
	}

	public final int giveExp(final EcoPlayer victim) {
		int expWon = 0;
		final int exp = victim.getPlayer().getTotalExperience();
		if (Conf.EXP_STEAL.asDouble() <= 1) {
			expWon = (int) (Conf.EXP_STEAL.asDouble() * exp);
		} else {
			expWon = exp;
		}
		setExp(getPlayer().getTotalExperience() + expWon);
		message(Lang.EXP_WON, victim.getPlayer().getName(), expWon);
		return expWon;
	}

	public final void setExp(final int exp) {
		getPlayer().setExp(0);
		getPlayer().setLevel(0);
		getPlayer().giveExp(exp);
	}

	public double getMoneyPercentage(final double percentage) {
		if (percentage > 1)
			return percentage;
		final double balance = getBalance();
		final double amount = Utils.roundTo2Decimal(percentage * balance);
		return Math.min(amount, balance);
	}

	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
