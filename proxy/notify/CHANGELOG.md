# Changelog

## 1.2.0

Compared to 1.0.0, the last version on Modrinth.

### Breaking changes
- Built against SimpleCloud API Plugin `0.1.0-platform.61`. Update the API plugin together with this plugin.

### Added
- Config files are reloaded automatically within a few seconds after you save them. A file with YAML errors is reported in the console and the previous version stays active.
- New `palette` section in `messages.yml` with named colors (`<brand>`, `<info>`, `<success>`, `<warning>`, `<error>`, `<muted>` and more) that you can use as tags in every message.
- New `command.exception` section in `messages.yml` to customize the messages for wrong usage, missing permissions, invalid arguments and failed commands. Before, these were fixed English messages.
- `/scnotify help` now shows a short description for each command. Hover a row to see its usage, click it to type the command. Edit the texts under `command.help.descriptions` and the layout under `command.help.hover`.
- `/scnotify set <player>` now suggests online player names.

### Changed
- New default look for all messages, matching the other SimpleCloud plugins. Existing `messages.yml` files are not changed; delete or rename yours to get the new defaults.
- The `<server>` placeholder in server notifications now includes the numerical id, e.g. `lobby-1` instead of `lobby`. If you customized your messages to `<server><id>`, use just `<server>`.
- The `<state>` placeholder now reads `Starting`, `Available`, `Stopping` and `Stopped` instead of upper case names.
- The default notification hovers now show the server or group name as their title.
- The unused `command.usage` and `command.permission` sections are no longer part of `messages.yml`. You can remove them from your file.
