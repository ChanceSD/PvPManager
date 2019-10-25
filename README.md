PvPManager
===========

[![Build Status](https://travis-ci.org/NoChanceSD/PvPManager.svg)](https://travis-ci.org/NoChanceSD/PvPManager)
[![Join the chat at https://gitter.im/NoChanceSD/PvPManager](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/NoChanceSD/PvPManager?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Discord:
https://discord.gg/QFTjs3g

Development Builds:
http://ci.md-5.net/job/PvPManager/

There are multiple PvP plugins, but PvPManager aims to be All in One. Meaning that instead of using multiple plugins that change/customize PvP in your server you can use just PvPManager. 
The features include allowing players to toggle PvP easily, which is a nice feature for donors, block commands for players in combat, detecting and applying defined punishments on PvP logging and a timer feature that toggles PvP for a world automatically! 
All this features have Multi-World support and don't conflict with plugins like WorldGuard!

Spigot Page: https://www.spigotmc.org/resources/pvpmanager-lite.845/

Bukkit Page: http://dev.bukkit.org/bukkit-plugins/pvpmanager/

**bStats**

[![bStats](https://bstats.org/signatures/bukkit/PvPManager.svg "bStats")](https://bstats.org/plugin/bukkit/PvPManager/ "bStats")

EVERYTHING BELOW IS OUTDATED
-----------

Commands and Permissions
-----------

* /pvp - Toggles PvP -> pvpmanager.pvpstatus.change
* /pvp status	- Check your PvP status	-> pvpmanager.pvpstatus.self
* /pvp status <player>	- Check other player PvP status	-> pvpmanager.pvpstatus.others
* /pvp disable protection	- Disables Newbie protection -> No permission
* /pm	- Shows PvPManager help page -> No permission
* /pm update - Update PvPManager to latest version -> pvpmanager.admin
* /pm reload - Reloads PvPManager -> pvpmanager.reload
* /pm pvpstart <time> [world]	- Changes PvP start time on a world -> pvpmanager.pvptimer
* /pm pvpend <time> [world] -	Changes PvP end time on a world -> pvpmanager.pvptimer

Special Permissions:
-----------

pvpmanager.nodrop - Players/ranks don't drop items if killed in PvP

pvpmanager.nocombat - Players/ranks are not placed in combat

pvpmanager.nopvp - Players/ranks have PvP disabled

pvpmanager.nodisable - Players/ranks don't get fly and gamemode disabled on PvP 

pvpmanager.pvpstatus.nocooldown - Bypass PvP toggle cooldown period

License
-----------
Copyright (c) 2019 , NoChanceSD

All rights reserved
