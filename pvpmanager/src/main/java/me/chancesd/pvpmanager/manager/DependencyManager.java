package me.chancesd.pvpmanager.manager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.DependencyException;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.hook.EssentialsHook;
import me.chancesd.pvpmanager.integration.hook.GriefPreventionHook;
import me.chancesd.pvpmanager.integration.hook.LibsDisguisesHook;
import me.chancesd.pvpmanager.integration.hook.PlaceHolderAPIHook;
import me.chancesd.pvpmanager.integration.hook.SimpleClansHook;
import me.chancesd.pvpmanager.integration.hook.TABHook;
import me.chancesd.pvpmanager.integration.hook.TownyHook;
import me.chancesd.pvpmanager.integration.hook.VaultHook;
import me.chancesd.pvpmanager.integration.hook.worldguard.WorldGuardModernHook;
import me.chancesd.pvpmanager.integration.type.AFKDependency;
import me.chancesd.pvpmanager.integration.type.Dependency;
import me.chancesd.pvpmanager.integration.type.DisguiseDependency;
import me.chancesd.pvpmanager.integration.type.ForceToggleDependency;
import me.chancesd.pvpmanager.integration.type.GodDependency;
import me.chancesd.pvpmanager.integration.type.GroupDependency;
import me.chancesd.pvpmanager.integration.type.RegionDependency;
import me.chancesd.pvpmanager.integration.type.WorldGuardDependency;
import me.chancesd.pvpmanager.listener.MoveListener;
import me.chancesd.pvpmanager.listener.MoveListener1_9;
import me.chancesd.pvpmanager.player.ProtectionType;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.sdutils.scheduler.ScheduleUtils;
import me.chancesd.sdutils.utils.Log;
import me.chancesd.sdutils.utils.MCVersion;
import me.chancesd.sdutils.utils.Utils;
import net.milkbowl.vault.economy.Economy;

public class DependencyManager {

	private final Map<Hook, Dependency> dependencies = new EnumMap<>(Hook.class);
	private final ArrayList<GroupDependency> attackChecks = new ArrayList<>();
	private final ArrayList<RegionDependency> regionChecks = new ArrayList<>();
	private final ArrayList<GodDependency> godChecks = new ArrayList<>();
	private final ArrayList<DisguiseDependency> disguiseChecks = new ArrayList<>();
	private final ArrayList<ForceToggleDependency> togglePvPChecks = new ArrayList<>();
	private final ArrayList<AFKDependency> afkChecks = new ArrayList<>();

	public DependencyManager() {
		if (Bukkit.getPluginManager().getPlugin("TAB-Bridge") != null && (Conf.NAMETAG_COMBAT_ENABLED.asBool() || Conf.TOGGLE_NAMETAG_ENABLED.asBool())) {
			Log.info("TAB-Bridge detected. Nametags in combat disabled due to incompatibility. Use the prefix/suffix placeholders instead");
			Conf.NAMETAG_COMBAT_ENABLED.disable();
			Conf.TOGGLE_NAMETAG_ENABLED.disable();
		}
		final List<Hook> failedHooks = setupHooks(Hook.values());
		// Delayed check for hooks that do not use softdepend
		ScheduleUtils.runPlatformTask(() -> {
			final Hook[] hooks = Arrays.stream(Hook.values())
					.filter(hook -> hook.isEnabled() && !isDependencyEnabled(hook) && !failedHooks.contains(hook))
					.toArray(Hook[]::new);
			if (hooks.length == 0)
				return;
			final List<String> softDepend = PvPManager.getInstance().getDescription().getSoftDepend();
			if (Arrays.stream(hooks).anyMatch(h -> softDepend.contains(h.toString()))) {
				Log.severe("Found plugins that PvPManager declared as soft dependencies but were still loaded out of order.");
				Log.severe("If you're not using any server flags that cause this, please report it to your server software developers.");
			}
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
				Log.warning("Failed to enable support for " + hook.getDescription().getFullName() + ". Is " + hook + " up to date?", e);
				failedHooks.add(hook);
			}
		}
		return failedHooks;
	}

	private boolean attemptHookingInto(final Hook hook) {
		final Dependency dependency = switch (hook) {
		case SIMPLECLANS -> new SimpleClansHook(hook);
		case VAULT -> new VaultHook(hook);
		case WORLDGUARD -> {
			if (Utils.isVersionAtLeast(Utils.stripTags(hook.getVersion()), "7.0")) {
				yield new WorldGuardModernHook(hook);
			} else {
				// Use reflection to instantiate WorldGuardLegacyHook from the pvpmanager-worldguard-legacy module
				try {
					final Class<?> clazz = Class.forName("me.NoChance.PvPManager.Dependencies.Hooks.WorldGuardLegacyHook");
					final Constructor<?> constructor = clazz.getConstructor(Hook.class);
					final WorldGuardDependency legacyHook = (WorldGuardDependency) constructor.newInstance(hook);
					yield legacyHook;
				} catch (final Exception e) {
					throw new DependencyException("Failed to load WorldGuardLegacyHook", e, hook);
				}
			}
		}
		case ESSENTIALS -> new EssentialsHook(hook);
		case PLACEHOLDERAPI -> new PlaceHolderAPIHook(hook);
		case LIBSDISGUISES -> new LibsDisguisesHook(hook);
		case TOWNY -> new TownyHook(hook);
		case GRIEFPREVENTION -> new GriefPreventionHook(hook);
		case TAB -> new TABHook(hook);
		default -> new BaseDependency(hook);
		};
		return registerDependency(dependency);
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

	public final boolean shouldDisableProtection(@NotNull final Player attacker, @NotNull final Player defender, final ProtectionType reason) {
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
		if (Conf.PUSHBACK_ENABLED.asBool() && !regionChecks.isEmpty()) {
			if (MCVersion.isAtLeast(MCVersion.V1_9)) {
				Bukkit.getPluginManager().registerEvents(new MoveListener1_9(plugin.getPlayerManager(), this), plugin);
			} else if (MCVersion.isAtLeast(MCVersion.V1_8)) {
				Bukkit.getPluginManager().registerEvents(new MoveListener(plugin.getPlayerManager()), plugin);
			} else {
				Log.warning("Pushback on border hopping not available for 1.7.10 or below! Feature disabled!");
				Conf.PUSHBACK_ENABLED.disable();
			}
		}
		if (isDependencyEnabled(Hook.WORLDGUARD)) {
			((WorldGuardDependency) getDependency(Hook.WORLDGUARD)).startListener(plugin.getPlayerManager());
		}
	}

	public boolean isDependencyEnabled(final Hook d) {
		return dependencies.containsKey(d);
	}

	public Dependency getDependency(final Hook h) {
		return dependencies.get(h);
	}

	public boolean registerDependency(final Dependency dep) {
		dependencies.put(dep.getHook(), dep);
		if (dep instanceof final GroupDependency pvpHook) {
			attackChecks.add(pvpHook);
		}
		if (dep instanceof final RegionDependency regionHook) {
			regionChecks.add(regionHook);
		}
		if (dep instanceof final GodDependency godHook) {
			godChecks.add(godHook);
		}
		if (dep instanceof final DisguiseDependency disguiseHook) {
			disguiseChecks.add(disguiseHook);
		}
		if (dep instanceof final ForceToggleDependency togglePvPHook && togglePvPHook.shouldDisableProtection()) {
			togglePvPChecks.add(togglePvPHook);
		}
		if (dep instanceof final AFKDependency afkHook && afkHook.shouldProtectAFK()) {
			afkChecks.add(afkHook);
		}
		return true;
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

	public Collection<Dependency> getDependencies() {
		return dependencies.values();
	}

}
