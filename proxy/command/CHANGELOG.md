# Changelog

## 1.2.0

Compared to 0.0.7, the last version on Modrinth.

### Breaking changes
- Built against SimpleCloud API Plugin `0.1.0-platform.61`. Update the API plugin together with this plugin.
- An existing `messages.yml` is not migrated and still loads, but several keys and placeholders changed. Back it up, delete it and restart to get the new default file, then re-apply your own changes. The changes:
  - `command.group.list.entry` no longer gets `<value>`. Use `<online>` and `<total>` instead.
  - `command.server.start.success` and `command.server.start.queued` were replaced by `command.server.start.requested`.
  - `command.server.error.already-stopped` was renamed to `command.server.error.already-stopping`.
  - `command.group.start.queued` was removed. `command.group.start.partial` and `command.group.stop.partial` were added.
  - `command.format`, `command.permission`, `command.error`, `command.usage.invalid`, `command.usage.entry`, `command.usage.missing-argument` and `command.usage.invalid-number` were removed. Their messages are now under `command.exception`.
  - Servers are now shown as `<group>-<id>` (for example `lobby-1`) instead of `<group> <id>`.

### Added
- Config files are reloaded automatically within a few seconds after you save them. A file with YAML errors is reported in the console and the previous version stays active.
- `/cloud player list` without arguments lists all online players. The `<group|ps> <target>` arguments are now optional.
- `/cloud player info` shows online time, first seen and last seen.
- `/cloud player sendfrom` accepts a group name as source and moves the players from all servers of that group. Persistent servers can be used as source by name.
- Tab completion for the `<target>` argument (group or persistent server names, depending on the target type) and for the `sendfrom` source (running servers).
- Configurable messages for wrong usage, missing permission, wrong sender and invalid arguments under `command.exception`. Before, these used the command framework's built-in English messages.
- Error messages show the reason for a failure through the `<reason>` placeholder, for example "the controller is not reachable".
- A `palette` section in `messages.yml` that defines the colors used as tags such as `<brand>`, `<info>` or `<error>`.
- `command.help.hover` and `command.help.descriptions`: each `/cloud` help line has a description on hover and types the command into chat when clicked.
- A new `messages.yml` comes with comments for each section.

### Changed
- New default look for all messages, shared with the other SimpleCloud plugins, with a new prefix and color palette.
- Group and persistent server names are matched case-insensitively in all commands.
- `/cloud group start` starts the requested servers at the same time, allows a count from 1 to 50 and reports how many servers started when only some of them could be started.
- `/cloud group stop` skips servers that are already stopping, stops the rest at the same time and reports how many stopped when only some of them could be stopped.
- `/cloud group list` shows `<online>/<total> online` per group, where online counts servers that are available or in game.
- Stopped servers no longer show up in `/cloud server list`, the `/cloud group list` and `/cloud group info` counts, or server ID suggestions. Server ID suggestions are sorted.
- `/cloud server stop` also treats servers in cleanup or already stopped as stopping.
- `/cloud player list` is sorted by player name.
- `/cloud player sendall` reports when no players are online (`command.player.error.no-online-players`) and when all players are already on the target (`command.player.error.all-on-target`).
- Server state and group type are shown in readable form (for example `Available` instead of `AVAILABLE`).
- `/cloud reload` logs the error to the console when `messages.yml` can't be loaded.

### Fixed
- `/cloud server list <group>` with an unknown group now says the group was not found instead of showing an empty list.
- `/cloud server info` and `/cloud server stop` with an unknown group now say the group was not found.
- `/cloud group stop <group> <id>` no longer sends a stop request for a server that is already stopping.
- Player names typed into a command are no longer parsed as MiniMessage formatting in the reply.
