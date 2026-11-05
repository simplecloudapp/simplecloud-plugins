# Changelog

## 1.2.0

Compared to 0.0.5, the last version on Modrinth.

### Breaking changes
- Built against SimpleCloud API Plugin `0.1.0-platform.61`. Update the API plugin together with this plugin.
- Requires Paper or Purpur 26.1 or newer and Java 25. Servers on 1.20 to 1.21.11 must stay on 0.0.5 or update the server.
- Removed the `<player-name>`, `<player_name>`, `<player-sender>` and `<player_sender>` placeholders from sign layouts. Remove them from your layout files, otherwise they show up as plain text.

### Added
- New `messages.yml` to customize every command message, including a `palette` section with named colors (`<brand>`, `<info>`, `<success>`, `<warning>`, `<error>`, `<muted>` and more) and a `variables` section (for example `<prefix>`). Before, all messages were fixed.
- Layouts can use the palette colors and variables from `messages.yml`, for example `<highlight><server-name>`.
- New layout option `glowing-text: true` to make the sign text glow.
- New `<shine:text:base color:highlight color[:duration]>` tag for layouts that sweeps a highlight across the text, for example `<shine:STARTING:#3B95FF:#CFE4FF>`.
- New `<target>` placeholder for layouts with the group or persistent server name of the sign. It also works on offline signs.
- New `command.exception` section in `messages.yml` for wrong usage, missing permissions, invalid arguments and failed commands.
- `/scsigns help` now shows a short description for each command. Hover a row to see its usage, click it to type the command.

### Changed
- New default look for all messages and new default layouts (glowing text, no colored block behind the sign). Existing `layouts` folders are not changed; delete or rename `plugins/simplecloud-signs/layouts` to get the new defaults.
- Signs update right away when a server starts, stops or changes state, instead of only polling every second.
- Signs are only redrawn when their content changes, which saves work on servers with many signs.
- `/scsigns list` no longer clears your chat with empty lines.
- `add group` and `add persistent` now accept names in any case and save the real name of the group or persistent server.
- Group suggestions for `add group` only list server groups.
- Running `add` or `remove` from the console now says the command can't be used there, and looking at no block now says to look at a sign. Before, nothing happened.
- When the controller can't be reached, `add` now says so instead of claiming the group or persistent server does not exist.
- Frames with more than 4 lines are now cut to 4 lines instead of being rejected.
- Layout and location files with YAML errors are now reported in the console instead of being ignored silently.

### Fixed
- The `EMPTY` rule now matches servers with no players. Before, it matched full servers, so full servers showed the empty layout.
- The `REGEX` matcher now works. Before, it never matched.
- Signs let go of servers that are stopping and move on to the next server or the offline layout.
- Clicking a sign connects to the right server even when the proxy knows it under a different name.
- An error while updating signs, for example a layout with an unknown `rule`, no longer stops all signs from updating until a restart.
- Clicking an entry in `/scsigns list` without a target now teleports you to the sign. Before, it ran a command that does not exist.
- The Previous and Next buttons in `/scsigns list <target>` now work.
- Adding a sign to a group or persistent server whose locations file has a custom name now updates that file instead of creating a second one.
