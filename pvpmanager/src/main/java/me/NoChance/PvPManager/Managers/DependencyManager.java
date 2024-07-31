package me.NoChance.PvPManager.Managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Dependencies.AFKDependency;
import me.NoChance.PvPManager.Dependencies.BaseDependency;
import me.NoChance.PvPManager.Dependencies.Dependency;
import me.NoChance.PvPManager.Dependencies.DependencyException;
import me.NoChance.PvPManager.Dependencies.DisguiseDependency;
import me.NoChance.PvPManager.Dependencies.GodDependency;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.GroupDependency;
import me.NoChance.PvPManager.Dependencies.RegionDependency;
import me.NoChance.PvPManager.Dependencies.ForceToggleDependency;
import me.NoChance.PvPManager.Dependencies.WorldGuardHook;
import me.NoChance.PvPManager.Dependencies.Hooks.EssentialsHook;
import me.NoChance.PvPManager.Dependencies.Hooks.KingdomsXHook;
import me.NoChance.PvPManager.Dependencies.Hooks.LibsDisguisesHook;
import me.NoChance.PvPManager.Dependencies.Hooks.PlaceHolderAPIHook;
import me.NoChance.PvPManager.Dependencies.Hooks.SimpleClansHook;
import me.NoChance.PvPManager.Dependencies.Hooks.TownyHook;
import me.NoChance.PvPManager.Dependencies.Hooks.VaultHook;
import me.NoChance.PvPManager.Dependencies.Hooks.WorldGuardLegacyHook;
import me.NoChance.PvPManager.Dependencies.Hooks.WorldGuardModernHook;
import me.NoChance.PvPManager.Listeners.MoveListener;
import me.NoChance.PvPManager.Listeners.MoveListener1_9;
import me.NoChance.PvPManager.Player.CancelResult;
import me.NoChance.PvPManager.Settings.Settings;
import me.NoChance.PvPManager.Utils.CombatUtils;
import me.chancesd.pvpmanager.utils.ScheduleUtils;
import me.chancesd.sdutils.utils.Log;
import net.milkbowl.vault.economy.Economy;

public class DependencyManager {

	private final HashMap<Hook, Dependency> dependencies = new HashMap<>();
	private final ArrayList<GroupDependency> attackChecks = new ArrayList<>();
	private final ArrayList<RegionDependency> regionChecks = new ArrayList<>();
	private final ArrayList<GodDependency> godChecks = new ArrayList<>();
	private final ArrayList<DisguiseDependency> disguiseChecks = new ArrayList<>();
	private final ArrayList<ForceToggleDependency> togglePvPChecks = new ArrayList<>();
	private final ArrayList<AFKDependency> afkChecks = new ArrayList<>();

	public DependencyManager() {
		if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null) {
			Log.warning("GriefPrevention has been detected. GriefPrevention has some combat features without showing any feedback messages. "
					+ "Make sure to disable Punish Logout and set tag time to 0 seconds in GP config. "
					+ "Issues with those features often get wrongly blamed on PvPManager and cause conflicts due to the lack of GP feedback messages.");
		}
		if (Bukkit.getPluginManager().getPlugin("TAB") != null && Settings.useNameTag()) {
			Log.info("TAB detected. If you want nametags to change while in combat, set 'anti-override' to false in TAB's config."
					+ " (Doing that will prevent TAB from changing nametags)");
			Log.info("Or use the premium version of PvPManager which hooks into TAB for nametag/tablist changes.");
		}
		if (Bukkit.getPluginManager().getPlugin("TAB-Bridge") != null && Settings.useNameTag()) {
			Log.info("TAB-Bridge detected. Nametags in combat disabled due to incompatibility. Use the prefix/suffix placeholders instead");
			Settings.setUseNameTag(false);
		}
		final List<Hook> failedHooks = setupHooks(Hook.values());
		// Delayed check for hooks that do not use softdepend
		ScheduleUtils.runPlatformTask(() -> {
			final Hook[] hooks = Arrays.stream(Hook.values())
					.filter(hook -> hook.isEnabled() && !isDependencyEnabled(hook) && !failedHooks.contains(hook))
					.toArray(Hook[]::new);
			if (hooks.length == 0)
				return;
			Log.infoColor(ChatColor.LIGHT_PURPLE + "Delayed checking for any missing hooks...");
			setupHooks(hooks);
		});
	}

	private List<Hook> setupHooks(final Hook... hooks) {
		final List<Hook> failedHooks = new ArrayList<>();
		for (final Hook hook : hooks) {
			try {
				if (!hook.isEnabled()) {
					if (hook.getDisabledWarning() != null) {
						Log.warning(hook.getDisabledWarning());
					}
					hook.getDisabledAction().run();
					continue;
				}
				attemptHookingInto(hook);
			} catch (final NoClassDefFoundError | NoSuchMethodError | ClassCastException | NoSuchFieldError e) {
				Log.warning("Your " + hook + " version is currently unsupported: " + hook.getDescription().getFullName());
				Log.warning(hook + " support disabled");
				failedHooks.add(hook);
			} catch (final DependencyException e) {
				Log.warning(e.getMessage());
				hook.getDisabledAction().run();
				failedHooks.add(hook);
			} catch (final ExceptionInInitializerError e) {
				// Ignore, only here for unit tests
				Log.warning("Exception initializing " + hook);
			} catch (final Exception | LinkageError e) {
				Log.warning("Failed to enable support for " + hook.getDescription().getFullName() + ". Is " + hook + " up to date?");
				e.printStackTrace();
				failedHooks.add(hook);
			}
		}
		return failedHooks;
	}

	private void attemptHookingInto(final Hook hook) {
		switch (hook) {
		case SIMPLECLANS:
			registerDependency(new SimpleClansHook(hook));
			break;
		case VAULT:
			registerDependency(new VaultHook(hook));
			break;
		case WORLDGUARD:
			if (CombatUtils.isVersionAtLeast(CombatUtils.stripTags(hook.getVersion()), "7.0")) {
				registerDependency(new WorldGuardModernHook(hook));
			} else {
				registerDependency(new WorldGuardLegacyHook(hook));
			}
			break;
		case ESSENTIALS:
			registerDependency(new EssentialsHook(hook));
			break;
		case PLACEHOLDERAPI:
			registerDependency(new PlaceHolderAPIHook(hook));
			break;
		case LIBSDISGUISES:
			registerDependency(new LibsDisguisesHook(hook));
			break;
		case TOWNY:
			registerDependency(new TownyHook(hook));
			break;
		case KINGDOMSX:
			registerDependency(new KingdomsXHook(hook));
			break;
		default:
			registerDependency(new BaseDependency(hook));
			break;
		}
	}

	public final boolean canAttack(final Player attacker, final Player defender) {
		for (final GroupDependency pvPlugin : attackChecks)
			if (!pvPlugin.canAttack(attacker, defender))
				return false;
		return true;
	}

	public final boolean canAttackAt(final Player p, final Location l) {
		for (final RegionDependency regionPlugin : regionChecks) {
			if (!regionPlugin.canAttackAt(p, l))
				return false;
		}
		return true;
	}

	public final void disableGodMode(final Player p) {
		for (final GodDependency godPlugin : godChecks) {
			if (godPlugin.hasGodMode(p)) {
				godPlugin.disableGodMode(p);
			}
		}
	}

	public final void disableDisguise(final Player p) {
		for (final DisguiseDependency disguisePlugin : disguiseChecks) {
			if (disguisePlugin.isDisguised(p)) {
				disguisePlugin.unDisguise(p);
			}
		}
	}

	public final boolean shouldDisableProtection(final Player attacker, final Player defender, final CancelResult reason) {
		for (final ForceToggleDependency togglePvPPlugin : togglePvPChecks) {
			if (togglePvPPlugin.shouldDisable(attacker, defender, reason))
				return true;
		}
		return false;
	}

	public final boolean shouldProtectAFK(final Player player) {
		for (final AFKDependency afkPlugin : afkChecks) {
			if (afkPlugin.isAFK(player))
				return true;
		}
		return false;
	}

	public void startListeners(final PvPManager plugin) {
		if (Settings.borderHoppingPushback() && !regionChecks.isEmpty()) {
			if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.9")) {
				Bukkit.getPluginManager().registerEvents(new MoveListener1_9(plugin.getPlayerHandler(), this), plugin);
			} else if (CombatUtils.isVersionAtLeast(Settings.getMinecraftVersion(), "1.8")) {
				Bukkit.getPluginManager().registerEvents(new MoveListener(plugin.getPlayerHandler()), plugin);
			} else {
				Log.warning("Pushback on border hopping not available for 1.7.10 or below! Feature disabled!");
				Settings.setBorderHoppingPushback(false);
			}
		}
		if (isDependencyEnabled(Hook.WORLDGUARD)) {
			((WorldGuardHook) getDependency(Hook.WORLDGUARD)).startListener(plugin.getPlayerHandler());
		}
	}

	public boolean isDependencyEnabled(final Hook d) {
		return dependencies.containsKey(d);
	}

	public Dependency getDependency(final Hook h) {
		return dependencies.get(h);
	}

	public void registerDependency(final Dependency dep) {
		dependencies.put(dep.getHook(), dep);
		if (dep instanceof GroupDependency) {
			attackChecks.add((GroupDependency) dep);
		}
		if (dep instanceof RegionDependency) {
			regionChecks.add((RegionDependency) dep);
		}
		if (dep instanceof GodDependency) {
			godChecks.add((GodDependency) dep);
		}
		if (dep instanceof DisguiseDependency) {
			disguiseChecks.add((DisguiseDependency) dep);
		}
		if (dep instanceof ForceToggleDependency) {
			final ForceToggleDependency togglePvPHook = (ForceToggleDependency) dep;
			if (togglePvPHook.shouldDisableProtection()) {
				togglePvPChecks.add(togglePvPHook);
			}
		}
		if (dep instanceof AFKDependency) {
			final AFKDependency afkHook = (AFKDependency) dep;
			if (afkHook.shouldProtectAFK()) {
				afkChecks.add(afkHook);
			}
		}
	}

	public void unregisterDependency(final Dependency dep) {
		dependencies.remove(dep.getHook());
		attackChecks.remove(dep);
		regionChecks.remove(dep);
		godChecks.remove(dep);
		disguiseChecks.remove(dep);
		togglePvPChecks.remove(dep);
	}

	public JavaPlugin getDependencyMainClass(final Hook h) {
		if (isDependencyEnabled(h))
			return dependencies.get(h).getPlugin();
		return null;
	}

	public final Economy getEconomy() {
		if (isDependencyEnabled(Hook.VAULT))
			return ((VaultHook) dependencies.get(Hook.VAULT)).getEconomy();
		return null;
	}

	@SuppressWarnings("unchecked")
	public Map<Hook, Dependency> getDependencies() {
		return (Map<Hook, Dependency>) dependencies.clone();
	}

}
