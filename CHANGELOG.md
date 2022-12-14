# Changelog

## 0.22.0 (2022-12-30)

### Added
- `state.rollback()` function

## 0.21.0 (2022-11-12)

### Changed
- `includeHelp` is now builder argument

## 0.20.0 (2022-11-10)

### Added
- `onDocumentMediaGroup` trigger

## 0.19.4 (2022-11-05)

### Changed
- Added `onException` argument to `rolelessStateMachine`

## 0.19.3 (2022-11-01)

### Changed
- Added deprecation notice on `setState`

## 0.19.2 (2022-10-30)

### Added
- `onPhoto`

## 0.19.1 (2022-10-25)

### Fixed
- Remove unused K type argument from `rolelessStateMachine`

## 0.19.0 (2022-10-25)

### Added
- Nested state machines
- `rolelessStateMachine` builder

### Changed
- Updated `dev.inmo:tgbotapi:inmo-tgbotapi` to 3.3.0

## 0.18.0 (2022-10-21)

### Added
- `setState` and `setStateQuiet` with `transform` lambda argument.

### Changed
- Deprecate `onTransition`, replace with `onEnter`.
- Make it impossible to nest triggers.
- Artifact id is now just `fsm`
