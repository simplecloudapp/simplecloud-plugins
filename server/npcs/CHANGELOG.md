# Changelog

## 1.2.0

Compared to 0.1.6, the last version on Modrinth.

### Breaking changes
- NPCs are now rendered by the plugin itself, as packet NPCs (or native Mannequin entities on 1.21.9 and newer). NPCs that 0.1.6 created in Citizens, FancyNpcs or ZNPCsPlus are converted on the first start: their location, skin and name are copied and the old provider NPC is removed. Keep that provider plugin installed for the first start. If it is missing, the NPC stays hidden until you stand where it should be and run `/scnpcs edit <id> teleport`.
- `/scnpcs create <id> <target> <provider>` was removed. `create` always makes a built-in NPC. Use `/scnpcs link` to attach an NPC of another plugin.
- `/scnpcs edit <id> action send-title` was removed. Use `action title` and `action subtitle` instead.
- The fallback join state is now `public` instead of `default`. NPC files are migrated (a backup is kept in `npcs/backups/`), but commands and scripts that use `default` as the join state need to use `public`.

### Added
- No third-party NPC plugin is needed anymore. Citizens, FancyNpcs, ZNPCsPlus and MythicMobs NPCs can still be attached with `/scnpcs link`.
- In-game editor for every NPC setting, opened with `/scnpcs edit [id]` or right after `/scnpcs create`.
- `/scnpcs edit`, `/scnpcs info` and `/scnpcs delete` work without an id on the NPC you are looking at.
- New commands: `/scnpcs duplicate <id> <newId>`, `/scnpcs tp <id>`, `/scnpcs edit <id> teleport`, `/scnpcs edit <id> skin <player>` and `/scnpcs edit <id> skin data <texture> <signature>`.
- Menus: build server selectors in `inventories/` or with `/scnpcs inventory create|edit|delete|info|list`, with live server lists that update while the menu is open. Open them with `/scnpcs inventory open <id> [player]` or the `open-inventory` NPC action. An `inventories/example.yml` is written on the first start.
- Permissions `simplecloud.npcs.inventory.open` (default: everyone) and `simplecloud.npcs.inventory.open.others` (default: op) for `/scnpcs inventory open`.
- NPC looks: equipment, pose, scale, glowing with a color, custom name, look at nearby players (`look-at-player`, `look-at-player-distance`) and `view-distance`.
- New actions: `action-bar` (also as `/scnpcs edit <id> action action-bar`), `npc-animation`, `speech` (a speech bubble above the NPC), and `close-menu` / `previous-menu` for menu items.
- Per action: `permission` with a `deny-message`, a `cooldown` in milliseconds, and `play-sound-options` (volume and pitch).
- `join-strategy` per NPC: `LEAST_PLAYERS` (default), `MOST_PLAYERS` or `RANDOM`.
- Built-in `offline` join state: when no server of the target has been available for 5 seconds, the NPC shows its `offline` hologram and actions. New NPCs get `offline` and `maintenance` layouts.
- Custom join states per NPC (`join-states`).
- Rotating hologram lines (`frames`, `frame-interval`) and a per-line `refresh` interval.
- Holograms and action texts support `<playername>`, `<playeruuid>` and, with PlaceholderAPI installed, `%placeholders%`. Holograms render them for each player.
- `/scnpcs placeholders [topic]` and the `inventory` help topic.
- Color tags such as `<brand>`, `<success>` and `<error>` in messages, holograms, names and menus. The colors are set in the new `palette` section of `messages.yml`.
- Pushback sound volume and pitch (`pushback.sound-options`).
- Update check on startup that logs to the console when a newer version is on Modrinth.

### Changed
- Supports Paper and Purpur 1.20.6 to 26.3. 1.20 to 1.20.5 are no longer listed.
- Requires the SimpleCloud API plugin (`simplecloud-api`), now declared as a dependency in `plugin.yml`.
- Kotlin is bundled in the jar, so the server no longer downloads libraries on the first start.
- Holograms are packet based. Old hologram entities from 0.1.6 are removed automatically.
- New look for all chat messages, help pages and menus. An existing `messages.yml` is kept and only its old default `prefix` is replaced; delete the file to get the new default texts.
- Each action has a 1 second click cooldown by default (`cooldown: 1000`).
- An action only switches servers once: if it sets more than one of `join-target`, `send-to-server` and `transfer-to-server`, only the first is used and a warning is logged.
- When a join state has no actions of its own, the `public` actions are used.
- `/scnpcs list` shows each NPC's targets and is clickable to open `info`.
- `/scnpcs reload` also reloads menus and `messages.yml`.
