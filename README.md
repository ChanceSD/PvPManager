PvPManager
===========
[![Discord](https://discordapp.com/api/guilds/622559860705198108/widget.png)](https://discord.gg/QFTjs3g)
[![Spiget Downloads](https://img.shields.io/spiget/downloads/845?label=spigot%20downloads)](https://www.spigotmc.org/resources/pvpmanager-lite.845/)
[![Build Status](https://github.com/ChanceSD/PvPManager/actions/workflows/maven.yml/badge.svg)](https://ci.codemc.io/job/ChanceSD/job/PvPManager/)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ChanceSD_PvPManager&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ChanceSD_PvPManager)
[![Crowdin](https://badges.crowdin.net/pvpmanager/localized.svg)](https://crowdin.com/project/pvpmanager)
[![GitHub commits since latest release](https://img.shields.io/github/commits-since/chancesd/pvpmanager/latest)](https://github.com/ChanceSD/PvPManager/commits/master)
![GitHub Releases](https://img.shields.io/github/downloads/chancesd/pvpmanager/latest/total)
***

To install and use the plugin, check the [Wiki](https://github.com/ChanceSD/PvPManager/wiki)

Useful Links
------------
**Spigot:** https://www.spigotmc.org/resources/pvpmanager-lite.845/  
**Bukkit:** https://dev.bukkit.org/bukkit-plugins/pvpmanager/  
**Modrinth:** https://modrinth.com/plugin/pvpmanager

**Dev Builds:** https://ci.codemc.io/job/ChanceSD/job/PvPManager/  
**Crowdin Translations:** https://crowdin.com/project/pvpmanager  

Developers ([API](https://github.com/ChanceSD/PvPManager/wiki/Developer-API))
------
Maven Repo:
```xml
<repository>
    <id>CodeMC</id>
    <url>https://repo.codemc.org/repository/maven-public/</url>
</repository>
```
Artifact Information:
```xml
<dependency>
    <groupId>me.chancesd.pvpmanager</groupId>
    <artifactId>pvpmanager</artifactId>
    <version>4.0.6</version>
    <scope>provided</scope>
</dependency>
 ```

Description
------------
PvPManager is a comprehensive PvP management plugin that handles combat mechanics, player protection, and server administration. It covers most PvP-related features while maintaining excellent performance.

**Combat Management:**
- Toggle PvP for individual players, worlds, or the entire server
- Combat tagging with customizable timers and visual indicators
- Action bar and boss bar displays for combat status
- Disable actions while in combat (fly, gamemode, commands, etc.)
- Prevent combat logging with flexible punishment system

**Player Protection:**
- Newbie protection for new players
- Respawn and teleport protection
- Stop "border hopping" when players flee to safezones during combat
- Prevent KDR abuse with configurable kick thresholds

**Additional Features:**
- Money rewards, penalties, and custom commands on kills
- Flexible inventory handling (keep/drop/transfer to killer)
- Item cooldown system and combat action restrictions
- PlaceholderAPI support and plugin integrations (WorldGuard, Citizens, etc.)
- [Premium version](https://www.spigotmc.org/resources/pvpmanager.10610/) available with additional features

For detailed feature descriptions and configuration options, check the [Wiki](https://github.com/ChanceSD/PvPManager/wiki).  

bStats
-----------

[![bStats](https://bstats.org/signatures/bukkit/PvPManager.svg "bStats")](https://bstats.org/plugin/bukkit/PvPManager/ "bStats")

![YourKit](https://www.yourkit.com/images/yklogo.png)

PvPManager uses YourKit to make sure everything runs smoothly in your server.  
YourKit supports open source projects with innovative and intelligent tools
for monitoring and profiling Java and .NET applications.
YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.
