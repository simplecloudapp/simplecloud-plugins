# Holograms, NPCs, and signs

Text placed in the world: floating labels, NPC nameplates, leaderboards, reward reveals. The player reads it while moving, from a distance, against whatever is behind it. Design for a glance, not a read.

## Use the project's API first

Most networks already have a way to spawn holograms and NPCs: an in-house library, or a plugin such as DecentHolograms, FancyHolograms, FancyNpcs, Citizens, or ZNPCs. Search the project for one before writing anything, and use it if it exists or if the user names one. Design the text and the placement; let that API own the entities, their persistence, and their click handling. Spawning raw entities next to an existing system leaves two sources of truth and holograms that survive restarts twice.

Only reach for the primitives below when the project has nothing, or when you are building that system.

## Primitives

| Need | Use |
| --- | --- |
| Floating text, 1.19.4+ clients | Text display entity |
| Floating text on older clients | Invisible marker armor stands, one per line |
| Label above an NPC | A Mannequin's own name and description lines (1.21.9+), or a hidden nametag plus a text display |
| Item or block above text | Item display or block display stacked above the text display |
| Clickable hologram | Interaction entity sized to the text, never the text entity itself |

Text displays are one entity for all lines, wrap automatically, and support background, scale, and billboard. Armor stands are one entity per line with fixed spacing and no background. Do not mix the two in one hologram.

## Labels

A label names the thing. Everything else is optional, and most labels need one or two lines:

- **Name first**, in the thing's accent: a game in its game color, a character in theirs, an item in its rarity color. Not the brand: a lobby where every NPC is the brand color is one wall of the same hue, and players stop telling them apart.
- **One line of context** if the name alone is not enough: live state (`42 playing`), or how to interact (`Right-click`). Quieter than the name: the value in highlight, the rest in muted.
- **A category line above the name** only when the lobby has many NPCs of different kinds and players would otherwise not tell a shop from a game. Short small caps, in the same accent or a deeper shade of it (COLORS.md, World text), with the same wording on every NPC of that kind.

```
<accent>Bedwars
<highlight>42 <muted>playing
```

Neighboring NPCs and holograms get different accents, and each keeps its accent wherever it appears: the Bedwars NPC, its hologram, and the Bedwars tag in menus share one color. Characters get a color that fits them (a wizard purple, a fisher cyan).

Whatever pattern the network picks, every NPC and hologram in a lobby follows it, so players read them all the same way: the same line order, the same kind of color in the same place. More than four lines is a leaderboard or belongs in a menu.

Never repeat what the object already shows. An NPC holding a bed does not need a "Bedwars" category, and a podium does not need the word "Podium".

## Text display defaults

Change the vanilla defaults deliberately:

| Property | Vanilla default | Use |
| --- | --- | --- |
| `billboard` | `fixed` | `center` for labels that should face the player; `vertical` for tall boards that must stay upright; `fixed` only when mounted on a wall |
| `background` | 25% black | keep the vanilla default |
| `shadow` | off | keep off; the background already carries the contrast |
| `see_through` | off | keep off; text through walls leaks information and clutters |
| `line_width` | 200px | lower it to force a narrow block |
| `alignment` | center | center for labels; left for leaderboards |
| `view_range` | 1.0 (64 blocks) | lower it for small labels, so distant crowds of text do not stack up |
| `scale` (transformation) | 1.0 | 1.0 to 1.5 for names and titles; below 1.0 only for close-up text, tested at the distance players read it |

A scaled title line makes a hologram feel designed. Use it the same way across the lobby. Never color the text shadow (`<shadow:#RRGGBBAA>`): a tinted shadow looks smeared, not designed.

World text washes out where chat text does not: over sand, snow, quartz, and noon sky, pale tints and light grays lose their fill (COLORS.md, World text). Use full-strength accents for names, muted (never a lighter gray) for quiet lines, and keep the vanilla background. Check every label at noon from the spot where players first see it.

## Placement

- Put the bottom line at about 2 blocks above the floor, so the player reads it without looking up. Above an NPC, start just above the head.
- Leave room between independent holograms. Two labels that overlap at an angle become noise.
- Keep holograms out of the walking path. Text that clips through a player's face is the first thing they notice.

## NPCs

- **Mannequins** (1.21.9+) show the custom name plus a built-in description line under it (default "NPC"). Set the description to your context line or remove it, so it does not duplicate the label.
- **Player NPCs** (packet-based, older versions): hide the vanilla nametag and render a text display. The skin profile name must be 16 characters or fewer; never put display text into it.
- The name is the destination, unless the network has deliberate characters. Characters get a real name and a voice (CHAT.md, Panels).
- Interaction hints use the same words as menu action lines, in muted or the NPC's accent, never a pale tint.
- A click gets one answer: the menu opens (with a click sound), or the NPC speaks (CHAT.md, Panels). Never a greeting in chat plus a menu.

## Leaderboards

```
<accent>Top Kills <muted>(Weekly)
<muted>1. <white><player>  <accent>1,204
<muted>2. <white><player>  <accent>1,177
...
<muted>Updates in 4m
```

- Ten rows at most. Rank, name, then the score last, so only one column is ragged. Dot leaders or a right-aligned score column need measured padding (LIMITS.md) or a second text display for the scores; never pad with plain spaces.
- One color per row besides the neutrals: the score, or the player name in its rank color, not both.
- Each board has its own accent, for the title and the scores; boards next to each other differ, so three boards in three accents read as three boards, not one.
- State the period and when it updates. Stale numbers without a timestamp look broken.
- To switch periods or categories, put an Interaction entity on a `Click to switch` line. Do not spawn one board per period.

## Signs

Signs are four lines of 90px (hanging signs: 60px; detect them and use a shorter layout).

```
<accent>Bedwars
<highlight>42 <muted>playing
<success>Open
<muted>Right-click to join
```

- Turn on glowing text (`SignSide#setGlowingText(true)`). It makes the light palette readable on every wood; without it, use dark text.
- Wax the sign (`Sign#setWaxed(true)`) so players cannot edit it.

## Animation

Animate only to show change: a counter that ticks up, a status that flips, a reward that rises out of a crate. No scrolling marquees, rainbow cycling, or typewriter text. Update at most once per second and keep the line count stable, so the hologram does not jump.
