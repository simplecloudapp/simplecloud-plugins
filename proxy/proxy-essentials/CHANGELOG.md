# Changelog

## 1.2.0

Compared to 0.0.14, the last version on Modrinth.

### Breaking changes
- Built against SimpleCloud API Plugin `0.1.0-platform.61`. Update the API plugin together with this plugin.
- Java 25 is required (was Java 21). Update the Java version of your proxy groups.
- The plugin is now called `simplecloud-proxy-essentials` on both Velocity (plugin ID) and BungeeCord (plugin name). Remove the old `proxy-velocity.jar` / `proxy-bungeecord.jar` so the plugin isn't loaded twice.
- The data folder moved to `plugins/simplecloud-proxy-essentials`. On the first start, the old `proxy-essentials-velocity` or `proxy-essentials-bungeecord` folder is copied over if the new folder is empty. Rename the folder in your templates so your edits land in the folder that is used.
- Join state commands now take a target type (`group` or `ps`): `/scproxy joinstate info <group|ps> <target> [id]` and `/scproxy joinstate set <group|ps> <target> <joinstate> [id]`. The old forms `joinstate set <group> [id] <joinstate>` and `joinstate info <group> [id]` no longer work.
- `/scproxy reload` now needs `simplecloud.proxy-essentials.command.reload` instead of `simplecloud.proxy-essentials.command.help`.
- Config files are no longer reloaded automatically when you save them. Run `/scproxy reload` after editing.
- `placeholder.yml` is no longer read. Move your ping colors to `format.ping-colors` in `messages.yml`. The date and time formats are now `format.date` and `format.time`.
- The `whitelist` in `config.yml` is now enforced. Listed players skip the join state permission and full network checks. Older default configs had it enabled with `Notch` on the list, so review or disable it.
- In an existing `messages.yml`, `<error>` in `command.reload.failure` is now a color tag. Use `<reason>` for the error text. In `command.join-state.list.states.entry`, rename `<joinPermission>` to `<join_permission>`.
- For developers: the `ConfigureTagResolversEvent` on Velocity and BungeeCord was removed, so other plugins can no longer add placeholders to proxy-essentials texts.

### Added
- `external-server-joinstates` in `config.yml` sets join states for servers that SimpleCloud doesn't manage. The keys are server names and are not case-sensitive.
- `player-count.network` shows the online players of the whole network instead of the sum of the configured groups and persistent servers.
- Tab list groups take an optional `server-name-matcher` (`STARTS_WITH`, `ENDS_WITH`, `CONTAINS`, `EQUALS`, `REGEX`, `PATTERN`, with `negate`) for picking servers.
- `/scproxy joinstate list` lists the configured join states with their join permission.
- `/scproxy joinstate info` without arguments shows the join state of every group and persistent server.
- Help pages are clickable and show a description on hover. They list only the commands you have permission for. Descriptions are set in `command.*.help.descriptions`.
- `messages.yml` has a `palette` of named colors (for example `<brand>`, `<success>`, `<error>`). Every entry in `variables` becomes a tag. Both work in all texts: messages, kick messages, MOTD, tab list and server list.
- New `<time>` placeholder. `<date>` and `<time>` use `format.date` and `format.time`, and `<date:'pattern'>` sets a different format for one text.
- You can now change the messages for wrong command usage, missing permissions and invalid arguments (`command.exception`), as well as `command.failure`, `command.target-not-found`, `command.invalid-target-type` and `command.persistent-server-id`.
- MiniMessage now works in the server list hover lines (`player-list`) and in `version.name.text`.

### Changed
- New default look for messages, kick messages, MOTD layouts and the tab list. Your existing files are kept. To get the new defaults, delete `messages.yml` or the layout files, or copy the parts you want from a fresh install.
- On new installs, the whitelist is disabled and empty by default.
- When `version.name.enabled` is on, the version text is now always shown in the server list. Before, only players on a different Minecraft version saw it.
- If a server's join state doesn't exist in `config.yml`, players are now denied with `kick.no-join-state` instead of being handled with `initial-state`.
- `update-time: 0` on a tab list group now always shows its first entry. Before, it turned off tab list updates.
- `player-count.update-time: 0` now counts players once on startup. Before, it turned off counting other servers.

### Fixed
- Each tab list group now rotates at its own `update-time`. Before, every group used the fastest interval.
- On Velocity, the tab list now appears as soon as a player joins or switches servers, not on the next update.
- The server icon, player count and version text are now applied even when `motd.enabled` is `false` in a layout.
- `motd.update-time` in layouts is now used. Without it, the MOTD entry changes on every ping.
