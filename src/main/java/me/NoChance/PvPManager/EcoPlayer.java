package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.entity.Player;

public abstract class EcoPlayer {

	private final Economy economy;

	protected EcoPlayer(final Economy economy) {
		this.economy = economy;
	}

	public abstract Player getPlayer();

	public final boolean isOnline() {
		return getPlayer() != null;
	}

	public final void message(final String message) {
		if (isOnline())
			getPlayer().sendMessage(message);
	}

	public final void withdrawMoney(final double amount) {
		economy.withdrawPlayer(getPlayer(), amount);
	}

	public final void depositMoney(final double amount) {
		economy.depositPlayer(getPlayer(), amount);
	}

	public final void applyFine() {
		withdrawMoney(Variables.getFineAmount());
	}

	public final void applyPenalty() {
		withdrawMoney(Variables.getMoneyPenalty());
		message("§cYou lost " + Variables.getMoneyPenalty() + " for getting killed");
	}

	public final void giveReward(final Player victim) {
		depositMoney(Variables.getMoneyReward());
		message(Messages.getMoneyReward().replace("%m", Double.toString(Variables.getMoneyReward())).replace("%p", victim.getName()));
	}

}
