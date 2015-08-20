package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public abstract class EcoPlayer {

	private final Economy economy;

	EcoPlayer(final Economy economy) {
		this.economy = economy;
	}

	protected abstract Player getPlayer();

	public final boolean isOnline() {
		return getPlayer() != null;
	}

	public final void message(final String message) {
		if (isOnline())
			getPlayer().sendMessage(message);
	}

	private void withdrawMoney(final double amount) {
		economy.withdrawPlayer(getPlayer(), amount);
	}

	private void depositMoney(final double amount) {
		economy.depositPlayer(getPlayer(), amount);
	}

	public final void applyFine() {
		withdrawMoney(Variables.getFineAmount());
	}

	public final void applyPenalty() {
		withdrawMoney(Variables.getMoneyPenalty() >= 1 ? Variables.getMoneyPenalty() : Variables.getMoneyPenalty() * economy.getBalance(getPlayer()));
		message(Messages.getMoneyPenalty().replace("%m", Double.toString(Variables.getMoneyPenalty())));
	}

	public final void giveReward(final Player victim) {
		depositMoney(Variables.getMoneyReward() >= 1 ? Variables.getMoneyReward() : Variables.getMoneyReward() * economy.getBalance(getPlayer()));
		message(Messages.getMoneyReward().replace("%m", Double.toString(Variables.getMoneyReward())).replace("%p", victim.getName()));
	}

}
