---
name: minecraft-design
description: Use when writing, changing, or reviewing anything a Minecraft player or server owner sees, such as chat messages, command feedback, messages.yml or config defaults, MiniMessage or Adventure components, inventory menus or GUIs, item names and lore, scoreboards, the tab list, titles, action bars, boss bars, HUD elements, holograms, NPC nameplates, signs, dialogs, the MOTD or server list, kick screens, resource pack UI, or plugin sounds. Also use when the user asks to make a plugin look better, cleaner, more consistent, more modern, or more professional.
---

# Minecraft Design

Design Minecraft interfaces with the care of a product designer. Players spend hours inside these surfaces. Most plugins shout: gradients, `[Plugin] »` prefixes, bold capitals, five colors per line, glass panes everywhere. The best networks are clear, consistent, and full of small details: color that means something, the same tooltip order on every item, a menu laid out like a page instead of a storage chest.

## Start here

1. **Find the style.** If the project has a `DESIGN.md` at its root, it is the authority: read it before writing a single string. If the user points to a `DESIGN.md` elsewhere, such as a URL, read that. Otherwise, use the defaults below and suggest creating a `DESIGN.md` with the `create-design-md` skill.
2. **Find what already exists.** Many networks already use a plugin or library for holograms, NPCs, the tab list, scoreboards, or menus. Use it; design the strings and the layout it exposes instead of replacing it.
3. **Check for a resource pack.** Look at `DESIGN.md`, `resource-pack=` in `server.properties`, `ResourcePackRequest` or `setResourcePack` in the code, or a pack folder in the repository. If the network ships one, read [RESOURCE-PACK.md](RESOURCE-PACK.md); it changes which surfaces to use.
4. **Open the topic file** for the surface you are working on. Read only what the task needs.
5. **Check the limits** in [LIMITS.md](LIMITS.md) for every string that contains a name, a number, or a translation.

| Surface | File |
| --- | --- |
| Palette, brand spots, accents, backgrounds, world text, legacy fallback | [COLORS.md](COLORS.md) |
| Case, weight, symbols, numbers | [TYPOGRAPHY.md](TYPOGRAPHY.md) |
| Chat messages, prefixes, panels (invites, confirmations, NPC dialogue), command feedback, hover and click, player chat | [CHAT.md](CHAT.md) |
| Inventory menus, layout, items, lore, states | [MENUS.md](MENUS.md) |
| Dialogs (typed input: names, searches, amounts) | [DIALOGS.md](DIALOGS.md) |
| Sidebar scoreboard | [SCOREBOARD.md](SCOREBOARD.md) |
| Tab list header, footer, entries | [TAB-LIST.md](TAB-LIST.md) |
| Title, subtitle, action bar, boss bar, toasts | [TITLES.md](TITLES.md) |
| Text displays, NPC labels, leaderboards, signs | [HOLOGRAMS.md](HOLOGRAMS.md) |
| MOTD, icon, player count, version text, kick screens | [SERVER-LIST.md](SERVER-LIST.md) |
| What changes with a resource pack: icons, badges, HUD, menu textures | [RESOURCE-PACK.md](RESOURCE-PACK.md) |
| Sounds and particles | [SOUNDS.md](SOUNDS.md) |
| Widths, line counts, name lengths, version gates, Bedrock | [LIMITS.md](LIMITS.md) |

## Color roles

The topic files write colors as role tags. The project's `DESIGN.md` assigns a hex value to each role. If the plugin owns its MiniMessage parser, register the roles as tags (`<brand>`, `<muted>`, …) from a `palette` config section, so configs stay readable and the whole plugin can be re-themed in one place. Otherwise write the hex value.

| Role | Meaning | Typically |
| --- | --- | --- |
| `<brand>` | the network's signature, in a few fixed spots: network name, names of menu items without an accent of their own, or prefixes. Clickable things carry it through `<interactive>` | a saturated hue |
| `<interactive>` | tooltip action lines and menu controls; clickable words inside a sentence take the look `DESIGN.md` picks (CHAT.md) | the brand hue, a little lighter |
| `<highlight>` | values inside a sentence; the brightest text | a clear light tint of the brand, never pure white |
| `<muted>` | the body of neutral messages, keys, descriptions | a mid-light neutral gray |
| `<separator>` | structural punctuation: `[ ]`, `:`, `/`, `( )` | a dark gray |
| `<success>` | completed actions, gains | a saturated green |
| `<warning>` | no-ops, empty results, temporary refusals | a saturated orange or amber |
| `<error>` | failures, missing permission, invalid input, losses | a saturated red |

**Accents** are not roles: they tell things apart. Every thing players distinguish (a sidebar label, a game, an NPC, a currency, a tag, a rank, a team, a rarity) gets one saturated accent from a set of six to eight, recorded in `DESIGN.md` and used everywhere that thing appears. Neighbors get different accents. The topic files write a thing's accent as `<accent>`, or by kind: `<rank_color>`, `<team_color>`, `<tag_color>`, `<coins_color>`. Different names in one example mean different accents (`<map_color>`, `<timer_color>`). These are placeholders for tags `DESIGN.md` names after the thing and registers like the roles (`<coins>`, `<pvp>`, `<team_red>`); `<rank_tag>` is a rank's tag text and `<rank_tint>` its light message tint. `<info>` exists only for the line color chat style; `<social>` is used for friend and party notices in every style. Without a `DESIGN.md`, there is no fixed palette: see COLORS.md, Without a DESIGN.md.

Placeholders such as `<player>` are named MiniMessage placeholders. Insert anything a player typed (chat messages, names of items they renamed) unparsed (`Placeholder.unparsed`), so it cannot inject tags.

## Defaults

`DESIGN.md` decides these. When it does not, use the default.

| Decision | Default | Other options (see the topic file) |
| --- | --- | --- |
| Brand spots | **Network name, names of menu items without an accent, clickable things** (through `<interactive>`); accents everywhere else, including categories, tabs, and games in menus | Prefixes instead of item names |
| Chat style | **Highlighted values**: neutral body, the value in highlight; errors in one color for the whole line | **Line color** (each message one category color) |
| Prefix | **None**, in lobbies and in games. With a resource pack, a category icon | The game's name in games, a feature's name (`Party`) on its messages; one form for all (CHAT.md) |
| Clickable words | **Highlight in brackets** | Without brackets, a tint of their own (CHAT.md) |
| Panels | **Source, message, answer buttons** in their meaning colors; blocks set apart by empty lines | Buttons without brackets, in the clickable look (CHAT.md) |
| Ranks in chat | **Tag, name, and colon in the rank color**, message text in a light tint of it | Tag only in color, brackets, small caps, badges, another separator (CHAT.md) |
| Tone | **Friendly, second person**, exclamation marks only for good news | Calm, no exclamation marks; playful host voice |
| Tags | **Small caps**, each in its accent, only for tags, labels, and status words | Plain capitals; badges with a pack |
| Sidebar | **Label and value** lines in games, a short summary or nothing in lobbies | Label above value; HUD with a pack |
| Menus | **Vanilla chest, no filler**, content in the inner seven columns, glass pane navigation | Resource pack textures |
| Resource pack | **None** | Required; optional with text fallbacks |

## Principles

### Say what happened

Write the sentence a person would say, name the thing, and stop. "Teleporting you to Bedwars." beats "You have been successfully teleported to the server Bedwars-3!".

### Brand is a signature, accents tell things apart

The brand sits in a few fixed spots: the network name, one kind of name (menu items without an accent of their own, or prefixes), clickable things. Everything else that needs color gets an accent: each sidebar label, game, NPC, currency, and tag its own, kept everywhere. A screen where every title, label, and NPC is the brand color looks monotone; the same screen with a color per thing looks alive and still belongs together. Colors are saturated, never pastel, and each keeps its job: the same green means success in chat, in lore, and on a hologram.

### A few colors per line, many per screen

A line has one carrier color and one standout: the value, or a player name in its rank color. A third color is fine when a name and a value meet in one line. Dark gray punctuation does not count. Four is the limit. Across a whole screen (sidebar, chat, NPCs, boss bar), six to eight hues are normal.

### No plugin prefix

A prefix repeats on every line and says nothing the player does not know. Never put the network or plugin name in front of a message. Inside a game, the game's name may tell players which game is talking; with a resource pack, a small category icon can.

### The same thing in the same place

The same tooltip order on every item, the same slot for "Next page", the same color for coins, the same sound for "refused". Players learn the system once.

### Save the big surfaces for big moments

Titles, sounds, broadcasts, and particles lose their meaning when they fire for everything. A title for a destroyed bed or a won match, not for joining a lobby.

### Details are the design

A blocked action that explains itself in the tooltip before the click. A countdown that changes color in the last ten seconds. The player's own row marked in a ranking. Details like these separate a network from a plugin.

### Respect the surface

Each surface has a background, a width, a lifetime, and an audience. Chat is shared and scrolls away. Menus are private and stay. The action bar fades in seconds. An inventory title sits on light gray. Design for where the text appears.

### Design for the worst case

A 16-character name, a count of 1,000,000, a long translation, a Bedrock player through Geyser. If it breaks for them, it is broken.

### Make it configurable

Every player-facing string lives in a config file with a good default. Code never concatenates styled fragments; one message is one MiniMessage string with named placeholders.

## Review format (required)

When reviewing player-facing text or interfaces, use a markdown table with Before, After, and Why columns, one row per issue:

| Before | After | Why |
| --- | --- | --- |
| `&a&lSUCCESS! &7You joined &eBedwars&7!` | `<muted>Teleporting you to <highlight>Bedwars<muted>.` | MiniMessage, no bold capitals, one highlight |
| `<gold>[MyPlugin] » <gray>Not enough coins` | `<error>You need 250 more coins to buy this.` | No plugin prefix; say what is missing |
| `<gradient:#FF0000:#0000FF>Shop</gradient>` as an item name | `<!italic><highlight>Shop` | No gradients; custom names are italic unless disabled |
| 3-row menu, 8 games packed edge to edge, glass pane frame | 5-row menu, games centered in the inner seven columns, no filler | Layout over filler |
| Title `Welcome back!` on every lobby join | No title; a short chat line or nothing | Titles are for big moments |
| Sidebar with every label in the brand color | Each label in its own accent from `DESIGN.md` | Brand is a signature; accents tell things apart |

Order rows by impact: broken facts and placeholders first, then state and readability, then style, then polish.

## Before you finish

- Every placeholder from the original string is still present.
- Every string fits its surface with a 16-character name (LIMITS.md).
- No line has more colors than it needs; no pastel colors carrying meaning.
- The brand appears only in its spots. Neighbors in a list (labels, tags, NPCs, boards) have different accents, and each thing has the same accent everywhere.
- World text keeps the vanilla background and an uncolored shadow; no pale tints over the map.
- Every action answers once: a menu, a chat line, or a title, plus at most one sound. Nothing opens a menu and also writes to chat.
- Every clickable thing has a hover or an action line. Clickable words inside a sentence have the one look `DESIGN.md` picks, close to the line's colors.
- No bold, no gradients, no decorative symbols, unless `DESIGN.md` asks for them.
- Nothing is hardcoded that a server owner would want to change.
