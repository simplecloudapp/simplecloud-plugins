# <Network> design

<One paragraph: what <Network> is, who plays, and the character in two or three words. Every string in every plugin must look like it came from the same product.>

## Priority order

1. Preserve facts: names, counts, states, permissions, placeholders.
2. Preserve the server owner's configuration.
3. Make the state and the next action clear at a glance.
4. Follow the decisions and tokens in this file.
5. Fit the surface's limits.
6. Refine hover text, click actions, sounds, and alignment.

## Decisions

| Decision | Choice |
| --- | --- |
| Brand color | <hex, and where it comes from> |
| Brand spots | <network name, menu items without an accent or prefixes, clickable things through interactive> |
| Chat style | <highlighted values / line color> |
| Prefix | <none / category icon / game name / feature name> |
| Prefix form | <frame ([Bedwars] / Bedwars » / name only), color (brand / accent), frame color, case, icon> |
| Clickable words | <highlight in brackets / no brackets / a tint of their own> |
| Panels | <source form, button form and colors, block spacing> |
| Tone | <friendly, ! for good news / calm / playful> |
| Tags | <plain uppercase / small caps / badges> |
| Rank format | <tag form (plain / small caps / brackets / badge), which parts carry the rank color, separator, message text color, players without a rank> |
| Sidebar | <label and value / label above value / none in lobbies / HUD> |
| Menu actions | <Left-click to join / one color per button / Left-click » Join> |
| Navigation panes | <stained glass color> |
| Resource pack | <none / optional / required> |
| Language and numbers | <language, `2,500`, `2h 41m`> |

<If any decision was made without the user, say so here and why.>

## Project facts

<What step 1 found: client versions, Bedrock (Geyser), resource pack, languages, and the plugins already used for holograms, NPCs, the tab list, scoreboards, and menus.>

## Tokens

### Color roles

| Role | Hex | Use |
| --- | --- | --- |
| brand | | the brand spots only |
| interactive | | tooltip action lines and menu controls |
| highlight | | values inside a sentence, the brightest text |
| muted | | neutral message bodies, keys, descriptions |
| separator | | `[ ]`, `:`, `/`, `( )` |
| success | | completed actions, gains |
| warning | | no-ops, empty results, temporary refusals |
| error | | failures, permission, invalid input, losses |
| info | | info lines (only for the line color chat style) |
| social | | friends, party, private messages (only if used) |

<Rules: carrier color plus one highlight per line, separators always in separator, which states are whole-line colors.>

### Accents

| Accent | Hex |
| --- | --- |
| | |

<Six to eight hues chosen for this network, at the strength of the state colors, leaving out the brand's hue.>

### Accent assignments

| Thing | Kind | Accent | Used on |
| --- | --- | --- | --- |
| | rank / team / rarity / currency / game / NPC / sidebar label / tag | | |

<Every thing that has a color is listed here once. Neighbors on one surface differ; within one kind, accents are unique while the set lasts; baselines may be gray. Ranks also list their light message tint, if the rank format uses one.>

### Syntax

<Role tags registered from a `palette` config section, or hex in configs. One tag per accent assignment, named after the thing (`<coins>`, `<pvp>`, `<bedwars>`), registered the same way. Placeholder naming. Player input through `Placeholder.unparsed`.>

### Symbols

| Symbol | Use |
| --- | --- |
| | |

<Icons (resource pack glyphs) with their text fallback, if any. Which symbols are banned.>

## Components

### Messages

<The chat style's pattern and examples: neutral, success, error, warning, list, help, rewards.>

### Panels

<Invites, requests, confirmations, NPC dialogue: one short and one long example, the hover with the limit, the answer to a late click.>

### Player chat, friend and party notices, private messages

### Menus

<Title, layout, tooltip order with an example, action line form, states, control row slots and items.>

### Sidebar or HUD

### Tab list

### Titles, action bar, boss bar, toasts

### Holograms, NPCs, signs

### Server list and kick screens

### Dialogs

<Which inputs use one: names, searches, amounts.>

<Delete any component the network does not use. Add any it has that is not listed.>

## Resource pack

<Only if the network ships one. Delete the section otherwise.>

| Decision | Value |
| --- | --- |
| Required | <yes / optional, with the fallback for players without it> |
| Namespace | <short network name, e.g. hearth> |
| Theme | <vanilla-plus / the network's own theme.toml, described: panel, slots, buttons, corners>; title <left / centered> |
| HUD carrier | <the boss bar color whose sprites are transparent, e.g. yellow> |

Button colors by meaning. Every button with a meaning uses its color, in every menu:

| Meaning | Fill | Used for |
| --- | --- | --- |
| active | <#hex> | the selected tab, the main action, confirm |
| <travel> | <#hex> | <teleport, join> |
| <store> | <#hex> | <purchases> |
| <help> | <#hex> | <the ? button> |
| <danger> | <#hex> | <delete, leave> |

Icons. One per meaning, used everywhere that meaning appears:

| Icon | Meaning | Tint |
| --- | --- | --- |
| <check> | <success, done> | <success> |
| <coin> | <the coins currency, after amounts> | <untinted, multi-color art> |

Badges: <which tags become badges (ranks, rarities, statuses), each in the accent assigned above>.

HUD: <the boss bar rows top to bottom and what each shows; the action bar elements>.

## Writing

<Case, punctuation, banned words, failure wording, how to address the player, product names, number formats.>

## Reject

<The network-specific list of things never to ship, starting with the habits found in the existing code.>

## Review in this order

1. **Facts**
2. **State**
3. **Decisions and tokens**
4. **Voice**
5. **Limits**
6. **Restraint**
