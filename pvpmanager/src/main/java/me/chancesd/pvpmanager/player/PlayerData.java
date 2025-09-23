package me.chancesd.pvpmanager.player;

import java.util.Map;
import java.util.UUID;

import me.chancesd.pvpmanager.storage.fields.UserDataFields;
import me.chancesd.pvpmanager.utils.CombatUtils;

/**
 * Data Transfer Object for player persistence data.
 * Contains all the data that needs to be saved/loaded for a player.
 */
public class PlayerData {
    
    private final UUID uuid;
    private final String name;
    private final String displayName;
    private final boolean pvpEnabled;
    private final long toggleTime;
    private final boolean newbie;
    private final long newbieTimeLeft;
    private final long lastSeen;
    
    public PlayerData(UUID uuid, String name, String displayName, boolean pvpEnabled, 
                     long toggleTime, boolean newbie, long newbieTimeLeft, long lastSeen) {
        this.uuid = uuid;
        this.name = name;
        this.displayName = displayName;
        this.pvpEnabled = pvpEnabled;
        this.toggleTime = toggleTime;
        this.newbie = newbie;
        this.newbieTimeLeft = newbieTimeLeft;
        this.lastSeen = lastSeen;
    }
    
    /**
     * Creates PlayerData from raw database map
     */
    public static PlayerData fromMap(Map<String, Object> userData) {
        if (userData == null || userData.isEmpty()) {
            return createDefault();
        }
        
        // Extract UUID
        UUID uuid = null;
        Object uuidObj = userData.get(UserDataFields.UUID);
        if (uuidObj instanceof String) {
            try {
                uuid = UUID.fromString((String) uuidObj);
            } catch (IllegalArgumentException e) {
                // Invalid UUID format, will use null
            }
        }
        
        // Extract name
        String name = (String) userData.get(UserDataFields.NAME);
        
        // Extract display name
        String displayName = (String) userData.get(UserDataFields.DISPLAYNAME);
        
        // Extract PvP status
        boolean pvpEnabled = false;
        Object pvpState = userData.get(UserDataFields.PVPSTATUS);
        if (pvpState instanceof Integer) {
            pvpEnabled = (int) pvpState != 0;
        } else if (pvpState instanceof Boolean) {
            pvpEnabled = (boolean) pvpState;
        }
        
        // Extract toggle time
        long toggleTime = 0;
        Object toggleTimeObj = userData.get(UserDataFields.TOGGLETIME);
        if (toggleTimeObj instanceof Number) {
            toggleTime = ((Number) toggleTimeObj).longValue();
        }
        
        // Extract newbie status
        boolean newbie = false;
        Object newbieState = userData.get(UserDataFields.NEWBIE);
        if (newbieState instanceof Integer) {
            newbie = (int) newbieState != 0;
        } else if (newbieState instanceof Boolean) {
            newbie = (boolean) newbieState;
        }
        
        // Extract newbie time left
        long newbieTimeLeft = 0;
        Object newbieTime = userData.get(UserDataFields.NEWBIETIMELEFT);
        if (newbieTime instanceof Number) {
            newbieTimeLeft = ((Number) newbieTime).longValue();
        }
        
        // Extract last seen
        long lastSeen = System.currentTimeMillis();
        Object lastSeenObj = userData.get(UserDataFields.LASTSEEN);
        if (lastSeenObj instanceof Number) {
            lastSeen = ((Number) lastSeenObj).longValue();
        }
        
        return new PlayerData(uuid, name, displayName, pvpEnabled, toggleTime, newbie, newbieTimeLeft, lastSeen);
    }
    
    /**
     * Creates default PlayerData for new players
     */
    public static PlayerData createDefault() {
        return new PlayerData(null, null, null, false, 0, false, 0, System.currentTimeMillis());
    }
    
    /**
     * Converts PlayerData to database map format
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new java.util.HashMap<>();
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
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public boolean isPvpEnabled() { return pvpEnabled; }
    public long getToggleTime() { return toggleTime; }
    public boolean isNewbie() { return newbie; }
    public long getNewbieTimeLeft() { return newbieTimeLeft; }
    public long getLastSeen() { return lastSeen; }
    
    // Builder methods for creating modified copies
    public PlayerData withPvpEnabled(boolean pvpEnabled) {
        return new PlayerData(uuid, name, displayName, pvpEnabled, toggleTime, newbie, newbieTimeLeft, lastSeen);
    }
    
    public PlayerData withToggleTime(long toggleTime) {
        return new PlayerData(uuid, name, displayName, pvpEnabled, toggleTime, newbie, newbieTimeLeft, lastSeen);
    }
    
    public PlayerData withNewbie(boolean newbie) {
        return new PlayerData(uuid, name, displayName, pvpEnabled, toggleTime, newbie, newbieTimeLeft, lastSeen);
    }
    
    public PlayerData withNewbieTimeLeft(long newbieTimeLeft) {
        return new PlayerData(uuid, name, displayName, pvpEnabled, toggleTime, newbie, newbieTimeLeft, lastSeen);
    }
    
    public PlayerData withCurrentTimestamp() {
        return new PlayerData(uuid, name, displayName, pvpEnabled, toggleTime, newbie, newbieTimeLeft, System.currentTimeMillis());
    }
}
