# Changelog

## 1.3.0

### Breaking changes
- Waterdog PE is no longer supported.
- The `/connection reload` command was removed. Config files are still reloaded automatically after you save them.

### Added
- Players are spread by the player counts of the whole network, not only of the own proxy.
- `registration.sync-interval` in `config.yml` (default `10` seconds)
- `kick.no-fallback-servers` in `messages.yml` can use `<reason>` (the kick reason of the server) and `<server>` (the server the player was kicked from). The new default shows the kick reason. Existing files are not changed.
- The `already-connected` message in `commands.yml` can use `<server>` (the server the player is on).
- On Velocity, players get a message when a command can't send them to the target server.

### Changed
- A SimpleCloud server whose proxy name is already used by an entry in `additional-servers` is no longer registered. A warning is shown in the console and the additional server keeps the name.
- A server that is missing data, such as its ip or port, is reported once in the console instead of on every load.

### Fixed
- Players were sent back and forth between servers that kicked them right after joining. Servers that kicked a player in the last 10 seconds are no longer used as fallback for that player.
- When the join or fallback server failed, the same server could be tried again, especially on BungeeCord. The next server is tried now.
- A lobby command answered "already connected" while the player's server was stopping. It now sends the player to another server.
- The `from` filter of fallback and command targets didn't work for servers that were no longer registered on the proxy.

## 1.2.1

### Changed
- Requests to the SimpleCloud controller now include the plugin name, so the controller logs show which plugin sent them.

## 1.2.0

Compared to 1.0.10, the last version on Modrinth.

### Breaking changes
- Built against SimpleCloud API Plugin `0.1.0-platform.61`. Update the API plugin together with this plugin.
- Automatic migration of the old single-file config from 1.0.0 was removed. If you are still on 1.0.0, update to 1.0.10 first, or delete `config.yml` and set it up again. Configs from 1.0.1 to 1.0.10 work as they are.

### Added
- Config files are reloaded automatically within a few seconds after you save them. A file with YAML errors is reported in the console and the previous version stays active.
- Servers that are stopping are no longer picked as join, fallback or command targets. They stay registered, so players already on them are not affected.
- Messages can use color tags such as `<brand>`, `<info>`, `<success>`, `<warning>`, `<error>` and `<muted>`. New installs get a `palette` section in `messages.yml` to change these colors.
- When the connection to SimpleCloud drops, the plugin loads the server list again once SimpleCloud is back and removes servers that stopped in the meantime.
- Servers whose proxy name changes while running (for example through a property used in `server-name-pattern`) are registered again under the new name.
- `/connection reload` also reloads the server list from SimpleCloud, so servers you remove from `ignore-server-groups-and-persistent-servers` come back without a restart.
- Other SimpleCloud plugins on the proxy can look up the proxy name this plugin gave a server, so they show and use the same names.

### Changed
- New default texts and colors for all messages in `messages.yml` and `commands.yml`, matching the other SimpleCloud plugins. Existing files are not changed. Delete them to get the new defaults.

### Fixed
- Servers that were already in the `INGAME` state when the proxy started were not registered.
- Deleted servers and servers that reached the `STOPPED` state could stay registered on the proxy.
- If SimpleCloud could not be reached while the proxy started, no servers were registered. The plugin now keeps retrying.
- When two servers got the same proxy name, stopping one of them removed the name from the proxy while the other one was still running.
- The `PATTERN` matcher operation used the server name as the pattern. It now uses the configured `value`.
- `GREATER_THAN` never matched in `ENV` rules. It now compares numeric values.
- On BungeeCord, `/connection reload` did not reliably unregister the custom commands from `commands.yml` before registering them again.
