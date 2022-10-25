# Changelog

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
