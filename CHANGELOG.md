# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Error handling when adding a broken .lsdsng.

### Changed
- Use new layout system and native file dialogs, might work better on different screens.
- Switched to Maven build system.

## [v1.0] - 2013-09-13
### Added
- import + export multiple songs in one go
- remember directories from last run

### Changed
- native look & feel
- Tidy up song list

## [v0.9] - 2012-01-09
### Fixed
- Only enable "clear slot" or "export .lsdsng" buttons when a song is selected.

## [v0.8] - 2011-04-03
### Fixed
- LSD-Manager will now try to open v3 .sav files even if they are smaller than 128kB. This may be necessary since some emulators/backup devices seem to truncate the .sav files if there is unused space at the end...

## [v0.7] - 2007-02-03
### Added
- Japanese white Nintendo Power cartridge support. Thanx Xinon!

## [v0.6] - 2007-02-01
### Fixed
- .lsdsng add. Thanx Rabato!

## [v0.5] - 2006-12-19
### Fixed
- .lsdsng export

## [0.4a]
### Fixed
- bug when adding .lsdsng's...

## [0.3a]
### Fixed
- "Add .lsdsng..." button

### Changed
- made file mem usage indicator more understandable
- replaced showOpenDialog with showSaveDialog where appropriate

## [0.2a]
### Added
- Export work memory to 32kByte v2 .sav

## [0.1a]
### Added
- Initial release.
