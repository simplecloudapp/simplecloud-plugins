# Changelog

## 1.2.0

Compared to 1.1.1, the last version on Modrinth.

### Breaking changes
- Built against SimpleCloud API Plugin `0.1.0-platform.61`. Update the API plugin together with this plugin.
- API: all packages moved from `app.simplecloud.prefixes` to `app.simplecloud.plugin.prefixes` (for example `app.simplecloud.plugin.prefixes.api.PrefixesApi`). Update your imports and rebuild plugins that use the API.
- API: the `id: UUID` parameters of `PrefixesApi`, `GroupProvider` and `PrefixesGroup` are now called `player`. Kotlin callers that use named arguments (`id = ...`) need to switch to `player = ...`.
- API: custom `GroupProvider` and `PrefixesGroup` implementations must be recompiled against the new package names.
- The `command.permission.denied` message in `messages.yml` is no longer used. If you changed it, move your text to `command.exception.permission`.

### Added
- Config files are reloaded automatically within a few seconds after you save them. A file with YAML errors is reported in the console and the previous version stays active.
- Vanished players are no longer published to the synced tab list on other servers. On Paper, players marked with the `vanished` metadata (set by common vanish plugins) are detected automatically.
- On Paper, vanished players no longer show up in name tag teams for players who can't see them.
- API: `PrefixesApi.setVanished(player, vanished)` and `PrefixesApi.isVanished(player)` to mark a player as vanished from your own plugin (also works on Minestom).
- `/scprefix` help entries now show a description and usage on hover, and clicking one types the command for you. Descriptions can be changed under `command.help.descriptions` in `messages.yml`.
- Clear chat messages for wrong command usage, missing permissions, commands that can't be used from the console and failed commands, configurable under `command.exception` in `messages.yml`.
- A `palette` section in `messages.yml` with named colors (`<brand>`, `<success>`, `<error>`, ...) that can be used as tags in every message.

### Changed
- New default look for messages and the default groups, shared with all SimpleCloud plugins. Only fresh installs get the new `config.yml` and `messages.yml`; existing files are kept. Delete `messages.yml` to get the new message defaults.
- The bundled `config.yml` now ships with an `admin` and a `default` group (the `owner` group was removed, and the `default` group has no prefix anymore). The built-in fallback chat format is now `<prefix><color><playername><suffix><#5F5F63>: <#E1E1E4><message>`.
- Reload messages were reworded ("Reloaded the prefixes!" / "Couldn't reload the prefixes. Check the console.").
- The error thrown when the API is used before Prefixes is loaded now lists the common causes.
- Maven: `prefixes-api` no longer pulls in Adventure transitively. Paper and Minestom already provide it; add it yourself on other setups. Sources jars are now published for all modules.
