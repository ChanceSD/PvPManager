package me.chancesd.pvpmanager.integration.hook.worldguard;

import org.bukkit.ChatColor;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.sdutils.utils.Log;

public class WorldGuardFlagHandler extends FlagValueChangeHandler<State> {
	public static final Factory FACTORY = new Factory();
	public static class Factory extends Handler.Factory<WorldGuardFlagHandler> {
		@Override
		public WorldGuardFlagHandler create(final Session session) {
			return new WorldGuardFlagHandler(session);
		}
	}

	private static StateFlag noProtectionFlag;
	private final PvPManager plugin = PvPManager.getInstance();

	public WorldGuardFlagHandler(final Session session) {
		super(session, noProtectionFlag);
	}

	public static void initializeFlags() {
		Log.infoColor(ChatColor.DARK_PURPLE + "Registering WorldGuard flags...");
		noProtectionFlag = registerFlag("pvp-protection", false);
	}

	public static void shutdown() {
		WorldGuard.getInstance().getPlatform().getSessionManager().unregisterHandler(FACTORY);
	}

	@Override
	protected void onInitialValue(final LocalPlayer player, final ApplicableRegionSet set, final State value) {
		// Do nothing
		Log.debug("Initial value: " + value);
	}

	@Override
	protected boolean onSetValue(final LocalPlayer player, final Location from, final Location to, final ApplicableRegionSet toSet,
			final State currentValue, final State lastValue, final MoveType moveType) {
		Log.debug("Changed value: " + currentValue + " from " + lastValue);
		if (currentValue != State.DENY)
			return true;
		final CombatPlayer cplayer = plugin.getPlayerManager().get(BukkitAdapter.adapt(player));
		if (!cplayer.hasPvPEnabled()) {
			cplayer.setPvP(true);
			cplayer.message(Lang.PVP_FORCE_ENABLED_WG);
		}
		return true;
	}

	@Override
	protected boolean onAbsentValue(final LocalPlayer player, final Location from, final Location to, final ApplicableRegionSet toSet,
			final State lastValue, final MoveType moveType) {
		Log.debug("Absent value, last was: " + lastValue);
		return true;
	}

	private static StateFlag registerFlag(final String name, final boolean def) {
		final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
		final StateFlag flag = new StateFlag(name, def);
		try {
			registry.register(flag);
		} catch (final FlagConflictException | IllegalStateException e) {
			final Flag<?> existing = registry.get(name);
			if (existing instanceof final StateFlag stateFlag) {
				return stateFlag;
			}
		}
		return flag;
	}

	public static StateFlag getNoProtectionFlag() {
		return noProtectionFlag;
	}

}
