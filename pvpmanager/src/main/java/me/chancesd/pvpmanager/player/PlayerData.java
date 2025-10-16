package me.chancesd.pvpmanager.player;

import java.util.Map;
import java.util.UUID;

import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.storage.fields.UserDataFields;
import me.chancesd.pvpmanager.utils.CombatUtils;

/**
 * Data Transfer Object for player persistence data.
 * Contains all the data that needs to be saved/loaded for a player.
 */
public class PlayerData {

	private final boolean defaultData;
    private final UUID uuid;
    private final String name;
    private final String displayName;
    private final boolean pvpEnabled;
    private final long toggleTime;
    private final boolean newbie;
    private final long newbieTimeLeft;
    private final long lastSeen;

    // Private constructor - use Builder instead
	private PlayerData(final Builder builder, final boolean defaultData) {
		this.defaultData = defaultData;
        this.uuid = builder.uuid;
        this.name = builder.name;
        this.displayName = builder.displayName;
        this.pvpEnabled = builder.pvpEnabled;
        this.toggleTime = builder.toggleTime;
        this.newbie = builder.newbie;
        this.newbieTimeLeft = builder.newbieTimeLeft;
        this.lastSeen = builder.lastSeen;
    }

    /**
     * Builder class for PlayerData
     */
	@SuppressWarnings("hiding")
    public static class Builder {
        private UUID uuid;
        private String name;
        private String displayName;
        private boolean pvpEnabled = false;
        private long toggleTime = 0;
        private boolean newbie = false;
        private long newbieTimeLeft = 0;
        private long lastSeen = System.currentTimeMillis();

		public Builder uuid(final UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder displayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder pvpEnabled(final boolean pvpEnabled) {
            this.pvpEnabled = pvpEnabled;
            return this;
        }

        public Builder toggleTime(final long toggleTime) {
            this.toggleTime = toggleTime;
            return this;
        }

        public Builder newbie(final boolean newbie) {
            this.newbie = newbie;
            return this;
        }

        public Builder newbieTimeLeft(final long newbieTimeLeft) {
            this.newbieTimeLeft = newbieTimeLeft;
            return this;
        }

        public Builder lastSeen(final long lastSeen) {
            this.lastSeen = lastSeen;
            return this;
        }

		public PlayerData build(final boolean defaultData) {
			return new PlayerData(this, defaultData);
        }
    }

    /**
     * Creates a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates PlayerData from raw database map
     */
    public static PlayerData fromMap(final Map<String, Object> userData) {
        if (userData == null || userData.isEmpty()) {
            return createDefault();
        }

        // Extract UUID
        UUID uuid = null;
        final Object uuidObj = userData.get(UserDataFields.UUID);
		if (uuidObj instanceof final String string) {
            try {
				uuid = UUID.fromString(string);
            } catch (final IllegalArgumentException e) {
                // Invalid UUID format, will use null
            }
        }

        // Extract name
        final String name = (String) userData.get(UserDataFields.NAME);

        // Extract display name
        final String displayName = (String) userData.get(UserDataFields.DISPLAYNAME);

        // Extract PvP status
        boolean pvpEnabled = false;
        final Object pvpState = userData.get(UserDataFields.PVPSTATUS);
        if (pvpState instanceof Integer) {
            pvpEnabled = (int) pvpState != 0;
        } else if (pvpState instanceof Boolean) {
            pvpEnabled = (boolean) pvpState;
        }

        // Extract toggle time
        long toggleTime = 0;
        final Object toggleTimeObj = userData.get(UserDataFields.TOGGLETIME);
		if (toggleTimeObj instanceof final Number number) {
			toggleTime = number.longValue();
        }

        // Extract newbie status
        boolean newbie = false;
        final Object newbieState = userData.get(UserDataFields.NEWBIE);
        if (newbieState instanceof Integer) {
            newbie = (int) newbieState != 0;
        } else if (newbieState instanceof Boolean) {
            newbie = (boolean) newbieState;
        }

        // Extract newbie time left
        long newbieTimeLeft = 0;
        final Object newbieTime = userData.get(UserDataFields.NEWBIETIMELEFT);
		if (newbieTime instanceof final Number number) {
			newbieTimeLeft = number.longValue();
        }

        // Extract last seen
        long lastSeen = System.currentTimeMillis();
        final Object lastSeenObj = userData.get(UserDataFields.LASTSEEN);
		if (lastSeenObj instanceof final Number number) {
			lastSeen = number.longValue();
        }

        return builder()
                .uuid(uuid)
                .name(name)
                .displayName(displayName)
                .pvpEnabled(pvpEnabled)
                .toggleTime(toggleTime)
                .newbie(newbie)
                .newbieTimeLeft(newbieTimeLeft)
                .lastSeen(lastSeen)
				.build(false);
    }

    /**
     * Creates default PlayerData for new players
     */
    public static PlayerData createDefault() {
		return builder().pvpEnabled(Conf.DEFAULT_PVP.asBool()).build(true);
    }

    /**
     * Converts PlayerData to database map format
     */
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new java.util.HashMap<>();
        if (uuid != null) {
            map.put(UserDataFields.UUID, uuid.toString());
        }
        if (name != null) {
            map.put(UserDataFields.NAME, name);
        }
        if (displayName != null) {
            map.put(UserDataFields.DISPLAYNAME, CombatUtils.truncateString(displayName, 255));
        }
        map.put(UserDataFields.PVPSTATUS, pvpEnabled);
        map.put(UserDataFields.TOGGLETIME, toggleTime);
        map.put(UserDataFields.NEWBIE, newbie);
        map.put(UserDataFields.NEWBIETIMELEFT, newbieTimeLeft);
        map.put(UserDataFields.LASTSEEN, lastSeen);
        return map;
    }

    // Getters
    public boolean isDefault() { return defaultData; }
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public boolean isPvpEnabled() { return pvpEnabled; }
    public long getToggleTime() { return toggleTime; }
    public boolean isNewbie() { return newbie; }
    public long getNewbieTimeLeft() { return newbieTimeLeft; }
    public long getLastSeen() { return lastSeen; }
}
