# Changelog

## 1.2.0

Compared to 1.0.10, the last version on Modrinth.

### Breaking changes
- Built against SimpleCloud API Plugin `0.1.0-platform.61`. Update the API plugin together with this plugin.
- Automatic migration of the old single-file config from 1.0.0 was removed. If you are still on 1.0.0, update to 1.0.10 first, or delete `config.yml` and set it up again. Configs from 1.0.1 to 1.0.10 work as they are.

### Added
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
