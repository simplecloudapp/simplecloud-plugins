# Colors

`DESIGN.md` assigns the hex values. This file explains how to pick them, where each kind of color goes, and how to use them on each surface, because Minecraft puts text on very different backgrounds.

## Four kinds of color

Every color on screen does one of four jobs:

| Kind | Job | Where |
| --- | --- | --- |
| **Brand** | says which network this is | a few fixed spots: the network name, the names of menu items without an accent of their own or prefixes, and clickable controls (`<interactive>`, a lighter brand) |
| **Meaning** | says what happened | success, warning, error, and the categories of the chosen chat style |
| **Accents** | tell things apart | each sidebar label, game, NPC, currency, tag, rank, team, rarity, and menu category gets its own |
| **Neutrals** | carry the reading | values, quiet text, punctuation |

A palette looks boring when the brand does the accents' job. If every title, label, header, NPC name, and leaderboard is the brand color, every screen is one hue plus gray, and nothing stands apart. Well-designed networks show six to eight saturated hues on a busy screen, while a single line still keeps to two or three colors.

## Brand is a signature

Put the brand in a few fixed spots, and keep it out of everything else, so it stays recognizable instead of becoming background:

- **The network name**: the sidebar title, the tab list header, the MOTD, a logo.
- **One kind of name**: menu items that have no accent of their own (a profile, a mailbox), or the prefix in chat. Not both, unless `DESIGN.md` says so. Categories, tabs, skills, tiers, and games are things: they keep their accent in menus too (MENUS.md).
- **Clickable controls**: tooltip action lines and menu controls in `<interactive>`, the brand hue a little lighter.

`DESIGN.md` lists the spots. Everywhere else, color comes from an accent or a meaning:

| Surface | Not the brand, but |
| --- | --- |
| Sidebar labels | one accent per label, as `DESIGN.md` assigns them |
| Menu titles | the vanilla title color (Know the background) |
| Tags and status words | the tag's accent or meaning color |
| NPC and hologram names | the NPC's or the game's accent |
| Leaderboards | the board's accent for the title and the scores, neutrals for places and names |
| Titles and boss bars | the meaning (Victory! in success) or the game's accent |

## Accents: a color per thing

Give every thing players tell apart its own accent, and keep that accent wherever the thing appears:

- **Neighbors differ.** The labels in one sidebar, the tags on one item, NPCs standing side by side, and the boards in one room each get a different accent. Neighbors in the same color read as one group.
- **Assign once.** `DESIGN.md` records which thing has which accent, and that accent is used on the sidebar, in chat, in the shop, and on the hologram. A new thing takes the next free accent, not a new hex.
- **The network decides, not the convention.** A natural color (rubies red, a water game cyan) is one reason to pick an accent, not a rule. Coins do not have to be yellow: on a network whose palette is built around teal and violet, violet coins look more like that network than yellow ones. Never assume a color for a thing that `DESIGN.md` has not assigned; take a free accent from its set.
- **Reuse across kinds is fine.** Cyan can be a rarity and a game at once; the context says which. Within one kind (all ranks, all currencies), keep accents unique while the set lasts. When a kind has more things than accents, reuse for things that never appear side by side, or let related things share one (all hardcore modes red).
- **Baselines may be neutral.** The default rank, the COMMON rarity, or an ordinary tag can be gray, so the colored ones stand out.
- **The set is a budget, not a quota.** Use as many accents as there are things to tell apart; a sidebar with two labels needs two.
- **Accents are not states.** A green label does not mean success. Success, warning, and error keep their roles; never use those role tags for things. An accent may share a hue family with a state, but not its exact shade: the red accent is a crimson, not the error red, so a red label never reads as a failure.

The accent set is six to eight saturated hues with the same strength as the state colors (One family).

## Saturated, not pastel

Text sits on dark, busy backgrounds behind a drop shadow. Pastel colors (light pink, pale mint, baby blue) look washed out there and read as "disabled". Brand, states, and accents are strong, bright hues; only the neutrals are quiet.

Typical values from well-designed networks:

| Role | Typical hex | Not |
| --- | --- | --- |
| Value, message text | a clear light tint of the brand, such as `#E8D4FF` for a purple brand or cream `#FCF3CA` for a gold one | pure white, which looks unfinished next to a tinted palette; light gray for values, which must be the brightest text |
| Quiet text, labels in tooltips | `#A09DA3` to `#B4B4B4` | colored grays that look like a faded hue |
| Punctuation, brackets, dimmed maxima | `#5C5C5C` to `#6A686E` | anything lighter: it starts competing with the text |
| Success | `#4EFD3F`, `#28D410`, `#88CD2B`: a yellow-leaning green | `#7BF1A8` (pastel mint) |
| Error | `#FB2C36`, `#E01030`, `#D8303C` | `#FFA2A2` (pastel pink) |
| Warning | `#FC7800`, `#FF8904`: a true orange | pale peach, or an amber too close to a gold brand |
| Brand and accents (from different palettes) | gold `#F5D84A`, crimson `#E10B4E`, cyan `#23B4E5`, mint `#1EDB8C`, violet `#9065F5`, pink `#DA48A0` | the same hue at 30% saturation |

A rough test: a color that carries meaning has an OKLCH chroma of about 0.14 or more; under 0.12 it starts to read as pastel. HSV saturation is misleading here: a clearly vivid purple can score under 0.5. The real test is the surface: look at it over daylight chat and in a tooltip. A pale tint is fine for text that is meant to be quiet, such as item descriptions.

## One family

Vivid is not enough: the colors also have to belong together. A palette of unrelated, maximally saturated hues (a light blue, a neon green, a signal yellow) makes every line shout in its own way, and chat looks random.

- **Same weight, different hue.** Pick every category color (brand, info, success, warning, error, social) and every accent so they look equally strong; only the hue changes, so no category is louder than another. Yellows, oranges, and greens look vivid at OKLCH lightness 0.75 to 0.83 (yellow highest) with chroma 0.17 to 0.22. Purples, blues, pinks, and reds only reach their full color darker: lightness about 0.65 to 0.70, chroma 0.21 to 0.23. Below a chroma of about 0.18, every hue starts to look pale next to a strong brand; keep all category colors at the brand's strength (cyans are the exception: sRGB caps them near 0.14). At 0.75, a purple turns lavender and a red turns salmon.
- **The clickable color comes from the brand.** Use the brand hue a little lighter (lightness about 0.72, chroma about 0.17) instead of a new hue. A yellow clickable color next to a purple brand looks like a mistake.
- **No pure white.** Highlight, the brightest text, is a light tint of the brand (lightness about 0.90, chroma about 0.065), clearly tinted rather than almost white: in game, a tint much paler than that still reads as white. Values, clickable words in brackets, and sidebar values all use it.
- **Tint the grays toward the brand, barely.** A trace of the brand hue (chroma about 0.01) in muted and separator ties them to the colors. More than that, and Bedrock and old clients round the gray to a color (the palette script checks it).
- **Keep highlight and the clickable color apart.** The clickable color stays clearly deeper than highlight, so a clickable line never looks like a plain value. A paler clickable tint also washes out over the world.
- **Ranks, teams, rarities, and the rest of the accents follow the same rule.**

Tailwind's 400 and 500 steps are a starting point for the hue, not the final value: their lightness differs a lot between hues (yellow 400 is far brighter than red 500). Adjust each color to the shared lightness and chroma. The palette script in `create-design-md` reports both and warns when the family falls apart.

## Without a DESIGN.md

There is no fixed starting palette: every network gets its own colors. When the project has no `DESIGN.md`, suggest creating one with the `create-design-md` skill, which picks the base colors for the network. If the user does not want one now, build the roles and the accent set from the project's existing colors with the rules in this file, use the same values for the whole task, and list them in your reply so they can go into `DESIGN.md` later.

## Build palettes from Tailwind

Tailwind CSS's hue scales are a good source of hues: each scale has even steps. The same step of two hues does not have the same lightness, though, so treat the **400 and 500 steps** as a starting point and adjust them into one family (above); 300 is the lightest step that still reads as a color on a dark background. 50 to 200 are tints, useful only as a near-white tint for message text. 600 and darker vanish over the world, except for reds, which hold up to 600.

For the neutrals, use one gray family and nothing else: `#94A3B8` next to `#A3A3A3` looks like a mistake. A plain neutral gray works; so does a gray with a slight warm or cool cast that matches the brand. Using one family consistently matters more than which one.

Avoid the 16 vanilla colors as the palette. `§a`, `§c` and `§6` side by side look like a plugin from 2014; picking your own hex values is free on modern clients.

## Ranks, teams, and rarities

These are accents with stricter rules, because players compare them. Each gets **one** saturated color, used everywhere that thing appears: chat, the name tag, the tab list, tooltips, holograms. Which parts of a rank carry it (the tag, the name, the separator) is a `DESIGN.md` decision (CHAT.md, Player chat). Never give one rank two different colors.

Keep ranks apart from each other, and do not use the exact error red for a rank: a red rank's chat line looks like a failure. Team colors are free: teams are named after their color (Red Team), so the name always says which it is.

## Know the background

| Surface | Background | Works | Fails |
| --- | --- | --- | --- |
| Chat | 50% black over the world | white, saturated 400 to 500 hues | dark grays and dark hues in daylight |
| Item tooltip | near-black purple `#100010` with a purple border | everything in the palette | dark purple text |
| Inventory title (no pack) | light gray `#C6C6C6` | vanilla dark gray `#404040`, very dark hues | the entire light palette |
| Sign (oak, birch) | light wood | dark text, glowing text | light palette without glow |
| Sign (dark oak, spruce, crimson) | dark wood | light palette | dark text |
| Text display | 25% black over the world by default | light palette | dark text, removing the background over sky |
| Boss bar, title, action bar | the world, no background | light palette, with shadow | anything thin or dark |
| Server list, kick screen | dark menu background | light palette | dark grays |

Every glyph is drawn with a drop shadow at 25% of its color, 1px down and right. The shadow is why light text holds up over a bright sky.

## World text

Holograms, NPC labels, and boards sit in front of whatever the map has behind them: sand, snow, quartz, bright sky. Pale tints and light grays that read well in chat wash out there, because the fill blends into the background and only the shadow is left.

- **Keep the vanilla background.** Leave the text display background at its default (25% black). It keeps text readable over sand and sky and looks like part of the game.
- **Names in full color.** The name line of a label is the thing's accent at full strength. World text is the one place where plain white beats the highlight tint, for a name without an accent. Never a pale tint (highlight, a light clickable color) as the main color of a label.
- **Quiet lines one step darker.** Context lines and click hints use muted, or the label's accent; not a lighter gray than chat uses.
- **Darker and more saturated than the menu version is fine.** World labels can sit a step deeper than the same thing's color in a tooltip.
- **Test at noon on the brightest spot of the map**, and at night.

## Rules

- **The inventory title is the exception.** Without a resource pack, leave it in the vanilla title color. Light colors disappear on the light gray background.
- **One state color per message.** A message that mixes success and error colors reads as neither.
- **Values are the brightest text.** The eye goes to the name or number first.
- **Punctuation is dark on purpose.** Brackets, colons, and slashes structure the line without being read.
- **One thing, one color.** If gold means coins, it means coins in chat, on the sidebar, and in menus (Accents).
- **Color is not the only signal.** About 1 in 12 men cannot reliably tell green from orange or red. The words must say "was not found", "is full", "is already enabled".
- **No gradients or rainbow text** in messages, names, or menus. They cost a component per glyph, break on legacy clients, and read as decoration. A logo in the MOTD may earn one.

## Legacy fallback

Modern Java clients always get the exact hex color. Only these see one of the 16 vanilla colors instead:

- Bedrock players joining through Geyser (Geyser maps to the vanilla colors plus a few Bedrock-only shades).
- Java clients below 1.16 joining through ViaVersion.
- Every player, in the server list hover (the player sample is plain legacy text).

Do not let the fallback drive the palette. If the network supports Bedrock or old clients, check only that success, warning, and error still map to three distinct colors, that the brand does not round to red, and that muted and separator stay gray. Write server list hover text with the 16 vanilla colors directly.
