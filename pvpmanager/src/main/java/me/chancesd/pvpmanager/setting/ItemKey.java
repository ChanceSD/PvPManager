package me.chancesd.pvpmanager.setting;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import me.chancesd.sdutils.utils.MCVersion;

/**
 * Immutable key for items used in cooldown maps.
 * data == -1 represents a wildcard (any data value).
 */
public record ItemKey(Material material, int data) {
    public static final int WILDCARD_DATA = -1;

    public static ItemKey of(final Material material, final int data) {
        return new ItemKey(material, data);
    }

    public static ItemKey wildcard(final Material material) {
        return new ItemKey(material, WILDCARD_DATA);
    }

    /**
     * Parse config-style strings: either "NAME" or "NAME-DATA" (only '-' separator supported).
     * Special-case: if the name is ENCHANTED_GOLDEN_APPLE but that Material does not exist
     * on this runtime, map it to GOLDEN_APPLE with data=1.
     */
    public static ItemKey fromString(final String raw) {
        if (raw == null)
            return null;
        final String s = raw.trim().toUpperCase();
        if ("ENCHANTED_GOLDEN_APPLE".equals(s) && Material.getMaterial("ENCHANTED_GOLDEN_APPLE") == null) {
            final Material legacy = Material.getMaterial("GOLDEN_APPLE");
            return legacy == null ? null : new ItemKey(legacy, 1);
        }
        if (s.contains("-")) {
            final String[] parts = s.split("-", 2);
            final String name = parts[0];
            int data = WILDCARD_DATA;
            try {
                data = Integer.parseInt(parts[1]);
            } catch (final Exception ex) {
                data = WILDCARD_DATA;
            }
            final Material m = Material.getMaterial(name);
            return m == null ? null : new ItemKey(m, data);
        }
        final Material m = Material.getMaterial(s);
        return m == null ? null : new ItemKey(m, WILDCARD_DATA);
    }

    public static ItemKey fromItemStack(final ItemStack item) {
        final Material m = item.getType();
        if (MCVersion.isLowerThan(MCVersion.V1_13)) {
            @SuppressWarnings("deprecation")
            final short dmg = item.getDurability();
            return of(m, dmg);
        }
        return wildcard(m);
    }
}
