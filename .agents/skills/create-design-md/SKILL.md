---
name: create-design-md
description: Use when the user wants to create, write, set up, or update a DESIGN.md, design system, style guide, brand guide, house style, or color palette for a Minecraft server, network, or plugin, or asks how their messages, prefix, colors, menus, or scoreboard should look across plugins. Also use when a Minecraft project has no DESIGN.md and the user asks to make its plugins look consistent or more modern.
---

# Create a DESIGN.md

A `DESIGN.md` is the house style of a Minecraft network or plugin suite, written for agents. It fixes the palette, the style decisions, and the components, so every plugin and every agent produces text that looks like the same product. The `minecraft-design` skill reads it as its authority and falls back to its own defaults for anything the file leaves open.

Write it in the voice of a design authority: direct, prescriptive, specific. Every rule is something an agent can follow or check.

## How it works

The design comes from what the project already does and from the networks the user likes. The user is not interviewed about design decisions: you make them, show the result, and change what they do not like.

### 1. Read the project

Before saying anything, look at:

- Message configs (`messages.yml`, `config.yml`, `lang/*.yml`), MiniMessage strings in code, and the defaults for MOTD, tab list, scoreboard, holograms, and menus.
- Colors in use, prefixes, symbols, sentence patterns, and inconsistencies between plugins.
- Ranks and their colors (the plugin's config or LuckPerms).
- A resource pack (`resource-pack=` in `server.properties`, `ResourcePackRequest` in code, a pack folder).
- Geyser or Floodgate (Bedrock players), ViaVersion (older clients), and the plugins already used for holograms, NPCs, the tab list, scoreboards, and menus.

Never ask for anything you can find here.

### 2. Ask for references

Send one message with one question, and make it clear it is optional:

> Which networks or servers do you like the look of, and what do you like about them? Screenshots help. Is there anything that has to stay, such as a brand color or rank names?

If the user skips it, or nobody can answer, continue with what the project has and the defaults.

### 3. Write the file

Make every decision in [TEMPLATE.md](TEMPLATE.md)'s decision table yourself:

1. What the user said must stay.
2. What the references show.
3. What the project already does consistently.
4. Otherwise, the default below.

| Decision | Default |
| --- | --- |
| Brand color | the existing color, or a saturated hue that fits the network |
| Brand spots | network name (sidebar title, tab header, MOTD), names of menu items without an accent of their own, clickable things (through `interactive`) |
| Chat style | highlighted values (CHAT.md) |
| Prefix | none; a category icon with a resource pack |
| Prefix form | only if games or features use one: the accent in dark gray brackets, `[Bedwars]` |
| Clickable words | highlight in brackets |
| Panels | the source in the prefix form or the speaker's accent; buttons in brackets, accept in success, decline in error; empty lines around blocks |
| Tone | friendly, `!` only for good news |
| Tags | small caps; badges with a resource pack |
| Rank format | tag, name, and `:` in the rank color; message text in a light tint of the rank color; players without a rank show the name in gray |
| Sidebar | label and value |
| Menu actions | `Left-click to join` |
| Navigation panes | one pane color for Previous and Next, a different one for Back; Previous or Back in the first slot and Next in the last slot of the bottom row |
| Language and numbers | English, `2,500`, `2h 41m` |
| Resource pack (only if it ships one) | required; the vanilla-plus theme, or the network's own theme (colors, corners, button style, title alignment) when it wants its own look; namespace = the network's short name; button colors by meaning (active, travel, store, help, danger) from the palette's states and accents; yellow boss bar carries the HUD |

Build the colors. There is no stock palette to copy: choose the base colors for this network, so two networks built with this skill do not look alike.

- Base colors first: the brand, success, warning, error, and the six to eight accent hues. Start from the colors the project already uses and the references; fill the rest with hues that suit the network's character (a fantasy server warmer and deeper, a competitive one cooler and sharper).
- Text from the brand hue, never pure white: a light tint of the brand for highlight (OKLCH lightness about 0.90, chroma about 0.065; paler tints still look white in game), and grays with only a trace of the brand (chroma about 0.01) for muted and separator. Muted sits around lightness 0.70, so quiet text and NPC hints do not wash out over the world.
- Category colors (brand, info, success, warning, error, social) and accents as one family: equally strong, with the hue-dependent lightness and chroma in COLORS.md ("One family"). Pick hues from [TAILWIND.md](TAILWIND.md), then adjust each to its target. Keep an established brand hue.
- The clickable color is the brand hue a little lighter (lightness about 0.72, chroma about 0.17), not a new color and not a pale tint.
- An accent set of six to eight hues at the same strength. Leave out the brand's own hue, so the brand stays a signature, and keep the red and orange accents a step away from error and warning (a crimson, an amber).
- Assign accents to things: every rank, team, rarity, currency, game, NPC kind, sidebar label, and tag gets one. A natural color (rubies red, a water game cyan) is a hint, not a rule: pick what fits this palette, so coins are not yellow just because they are yellow elsewhere. Neighbors on the same surface differ; within one kind (all ranks), accents are unique while the set lasts, and baselines (the default rank, COMMON) may be gray. When the rank format colors message text by rank, each rank also gets a light tint (OKLCH lightness about 0.91, chroma about 0.05).
- Accents may share a hue family with a state but never its exact shade; info and social can simply be two of the accents.
- Run `scripts/check_palette.py` from this skill's directory with the roles and fix every warning that applies to the network:

```bash
python3 scripts/check_palette.py brand=#RRGGBB interactive=#RRGGBB highlight=#RRGGBB muted=#RRGGBB separator=#RRGGBB success=#RRGGBB warning=#RRGGBB error=#RRGGBB info=#RRGGBB social=#RRGGBB accent.<name>=#RRGGBB ...
```

Then fill in [TEMPLATE.md](TEMPLATE.md):

- Exact MiniMessage strings with role tags (`<brand>`, `<muted>`) and real placeholders from the project's configs.
- At least one example for each message state.
- Delete components the network does not have; add ones it has that the template misses.
- Put the file at the project root. For a plugin suite, the shared repository or the plugin API module is the root.

### 4. Show it and adjust

Show the user the color table, the accent assignments, and five strings from their own project, before and after: a neutral message, an error, player chat, a menu tooltip, and the sidebar.

Colors have to be judged in game, not in a chat window. When the project is a plugin you can change, add an admin command such as `/<plugin> preview` that prints one sample of every kind of text from a `preview` list in the messages config: an info, success, warning, error, and social line, a line with a clickable word, a help list, and every accent as a sidebar label with a value. Together with the reload command, the user can change a palette value, reload, and look again without rebuilding. Point the user to it.

When they say what they do not like ("too colorful", "the sidebar is too busy"), change `DESIGN.md` and show the affected strings again. Every later correction goes into `DESIGN.md` too, so the file always matches what the user approved.

If the user cannot answer, say in the first line of your reply that you chose everything yourself, and list those choices under "Decisions" in the file.

## Rules for the content

- Eight color roles, the brand spots, the accent set with its assignments, and the category colors the chat style needs (for line color, usually info and social).
- Every rule is checkable: "End every sentence with a period", not "Keep a consistent tone".
- Include a "Reject" list written for this network, starting from the habits found in step 1.
- Do not include generic Minecraft knowledge (widths, limits, version gates). That lives in the `minecraft-design` skill.
- Do not mention Tailwind in the file. It is how the palette was built, not a rule for using it.
- With a resource pack, record the pack decisions (TEMPLATE.md, Resource pack), not how the pack is built.

## Hand off

List the existing strings that now break `DESIGN.md`, grouped by plugin, and suggest applying it one surface at a time, starting with chat and menus.
