# Cupboard
### Description
This plugin is made for PvP style server, let player can protect thire building / items, 
still passable destory by TNT, but TNT is hard to craft. (configurable).
This plugin goal is made a PvP server as like as Rust fun, 
but still keep Minecraft original play style.

### Features
* Protect area like Rust Cupboard (Core function, use Block of Gold)
* Anti TNT destory protect area (default is disable)
* Anti Creeper destory protect area (default is enable)
* Anti Soild Block block Nether portal
* Anti Lava cover Nether portal
* Anti Nether portal entity(non-player) teleport
* Change Nether portal sreach radius
* Let PigZombie drop nether wart
* World border (configurable)
* World border deduct amount per period.
* World border nether / the end scale.
* TNT CAN destory obsidian, water and lava.
* Language files support.

### How Cupboard work

Block of Gold can protect 19x19x19 area, Block of Gold is at center。

It can grant /revoke access by right click Block of Gold, It Protect following:

1. No access player can't place/remove block
2. No access player can't use stone plate/stone button
3. Mobs can't trigger stone plate
4. Creeper can't destory block in protect area

It CAN'T Protect following:

1. No access player place TNT (it will ignite immediately) (configurable)
2. No access player use Wooden plate / Wooden button / Chest / Armor Stand
3. TNT Destory Protect area (configurable)

### Commands and Permissions
* No permissions, when you are op and in creative mode, can bypass protect area.
* /kill - kill your self, this is good for someone is blocked in portect area.

### Install
1. Download jar files
2. Put it in plugins folder
3. Restart server
