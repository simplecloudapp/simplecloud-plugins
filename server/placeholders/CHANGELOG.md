# Changelog

## 1.2.0

Compared to 0.0.9, the last version on Modrinth.

### Breaking changes
- Built against SimpleCloud API Plugin `0.1.0-platform.61`. Update the API plugin together with this plugin.
- Requires Minecraft 1.21.11 or newer. Servers on 1.20 to 1.21.10 must stay on 0.0.9 or update the server.

### Added
- MiniPlaceholders support. Every placeholder is available as `<simplecloud_...>`, for example
  `<simplecloud_server_state>`. Pass the target as arguments: `<simplecloud_group_player_count:Lobby>`,
  `<simplecloud_server_state:Lobby:1>` or `<simplecloud_server_state:id:<server-id>>`.
- Network placeholders: `%simplecloud_network_online_players%` and `%simplecloud_network_online_servers%`
  (servers that are available or in game).
- Player placeholders for the viewing player, for example `%simplecloud_player_server%`, or for any player by name or
  UUID with `%simplecloud_player_<name-or-uuid>_<property>%`. Properties: `uuid`, `name`, `display_name`, `server`,
  `proxy`, `online`, `online_time` (shown as `1h 23m`), `online_time_seconds`, `first_seen`, `last_seen`.
- `%simplecloud_server_name%` (for example `Lobby-1`, or the persistent server name) and `%simplecloud_server_group%`.
- A console warning on startup when neither PlaceholderAPI nor MiniPlaceholders is installed.

### Changed
- PlaceholderAPI is no longer required. Install PlaceholderAPI, MiniPlaceholders or both.
- Placeholder values are loaded in the background and cached, so they no longer block the server thread. A value
  shows `...` until it has loaded for the first time and is refreshed every 5 seconds.
- A placeholder for a server, group, persistent server or player that cannot be found now shows empty text instead
  of the raw placeholder.
- Failed cloud lookups are logged as warnings in the console instead of being ignored.

### Fixed
- `group_player_count` no longer counts players on stopped servers.
- PlaceholderAPI placeholders no longer fail when they are requested without a player.
