# Changelog
## [1.2.1] - 2026-05-27
### Added
- New mixins to surpress ToughAsNails and SimpleDifficulty's temperature calculations when compat is set to surpress mode to true. This is 90% more better.
### Changed
- ToughAsNails and SimpleDifficulty's thrist is now done every 39 ticks and billed, this was never something that needed to be ran every tick.

## [1.2.0] - 2026-05-20
### Added
- Added SimpleDifficulty compatibility, uses same config options as ToughAsNails.
### Fixed
- Removed empty creative tabs from starmap when starmap is disabled.
### Changed
- Android mob weight of 0 will disable spawns instead of inserting a 0 weight entry.

## [1.1.0] - 2026-05-09
### Added
- New Dependancy - MixinBooter for Tough As Nails compatibility.
- Added Tough As Nails compatibility and configuration entries.
- Tough As Nails - thirst management on ZeroCalories ability with power costs (configurable).
- Tough As Nails - temperature management on new unlockable ability, either surpress or regulate (configurable).
### Fixed
- Improved performance with managePotionEffects, early exit to avoid per tick arraylists.
- Improved performance with android stats iteration, cache unlocked stats seperately to avoid checking locked stats, only check stats which are unlocked.
- Improved performance with minimap ability. O(chunk) using entities within AABB instead of o(world), scanning the whole world. Staggered the scan operations to reduce spikes. Guard scan operation to only when ability is in use, not sure why it never was.
- Cache max energy lookups and deloop respawn energy fill to a single operation. 
- Damage source type to a static final instead of 700 reallocations.
- Gate android potion effect spam behind potion check, avoid 4x700 new potion effect applications and network packets.

## [1.0.6] - 2026-04-30
### Fixed
- Tritanium crates not dropping items when destroyed. Now saves on wrench pickup, otherwise drops them.

### Changed
- Tritanium crates now accept dye through oreDict entries, modded dye should now work.

### Added
- Tritanium crates preserve dye color when picked up with a wrench.
- Added missing shift tooltip to tritanium crate.

## [1.0.5] - 2026-04-30
## Fixed
- Fixed FML version check (For real this time I swears).
## Changed
- Updated CE URL's to Refitted URL.

## [1.0.4] - 2026-04-30
### Fixed
- Fixed entity registration for mod integration (DeepMobEvolution). Moved from preInit to postInit.

## [1.0.3] - 2026-04-28
### Fixed
- Fixed natural and player spawned androids never attacking.
- Fixed some anomaly render compatability with shaders.

### Changed
- Switched from github based update URL to a curseforge related API, get latest upload.

## [1.0.2] - 2026-04-27
### Fixed
- Corrected the project and update URLs.
- An attempt of semi-automated FML changelog / update json generation.

### Changed
- Made baubles a soft dependancy. SpaceTimeEqualizer default chestpiece with optional bauble.

## [1.0.1] - 2026-03-31
- First build under Refitbench.

### Added
- This is a default template changelog that follows the [KeepAChangelog Convention](https://keepachangelog.com/en/1.1.0/)