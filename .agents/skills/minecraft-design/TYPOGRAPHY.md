# Typography

Minecraft has one font, one size, and two weights. Hierarchy comes from color, case, spacing, and position, not from bold or decoration. Use them the same way on every surface.

## Case

| Text | Case | Example |
| --- | --- | --- |
| Sentences: chat, descriptions, kick screens | Sentence case | `Teleporting you to Bedwars.` |
| Names: items, menus, games, NPCs, sections | Title case, as the thing is called | `Amber Crown`, `Your Profile` |
| Tags: rank, rarity, category, and status words | Small caps | `ᴠɪᴘ`, `ʀᴀʀᴇ`, `ᴘᴏᴘᴜʟᴀʀ`, `ʟᴏᴄᴋᴇᴅ` |
| Sidebar title, labels, and section headers | Small caps | `ʙᴇᴅᴡᴀʀꜱ`, `ᴍᴀᴘ`, `ᴛᴇᴀᴍꜱ` |
| Keys in key–value lines | Sentence case | `Time left: 22h 31m` |

### Uppercase is for tags

Uppercase works because it is rare. Use it for short tags that name a category or a state: rank tags, rarity and category tags in lore, a status line, sidebar labels. One or two words each.

Never for sentences, descriptions, item names, menu titles, section headers in menus, values, or anything a player types or searches for. A tooltip with three uppercase lines shouts.

**Small caps, not plain capitals.** Without a resource pack, write tags and labels in Unicode small caps (`ʀᴀʀᴇ`, `ᴍᴀᴘ`). They are smaller and quieter than plain capitals and come close to the label fonts large networks draw with a pack, so a tag reads as a label instead of shouting.

- Keep the config text plain (`RARE`, `MAP`) and convert in code: a `<smallcaps>` MiniMessage tag for config strings, and a helper for tags the code builds (rank tags, game tags, status words). Translators write normal text.
- Map `a`–`z` (either case) to `ᴀ ʙ ᴄ ᴅ ᴇ ꜰ ɢ ʜ ɪ ᴊ ᴋ ʟ ᴍ ɴ ᴏ ᴘ ǫ ʀ ꜱ ᴛ ᴜ ᴠ ᴡ x ʏ ᴢ`. `x` has no small capital; digits and punctuation stay as they are.
- Umlauts and accents have no small capitals. Keep such a word in plain capitals, or pick a label without them.
- Small caps come from different font ranges and look different on Bedrock and very old clients. If the network supports those, `DESIGN.md` may choose plain capitals instead.

With a resource pack, tags become badges: white uppercase text on a solid colored shape (RESOURCE-PACK.md).

## Weight

Do not use bold. Large networks almost never do: hierarchy comes from color and position, and a pack's custom fonts carry the weight where it is needed. Bold glyphs are also 1px wider, which breaks centering and widths (LIMITS.md).

The exceptions are rare and deliberate: an NPC's name above their dialogue, or a single word that must be readable across a lobby. Never a sentence, a rank tag, or an item name.

Never use italics, except where vanilla already does (lore and custom names default to italic and must be turned off with `<!italic>`).

## Symbols

Minecraft's default font has a limited set of symbols, and each one looks a little different. Use few, and use each for one job.

| Symbol | Common use |
| --- | --- |
| `[ ]` | brackets around a prefix, a clickable word, or a panel button in chat: `[Bedwars]`, `[Accept]` |
| `•` | a bullet in a list of items or rewards |
| `/` | current and maximum: `12/50` |
| `( )` | a count or a change after a value: `Friends (3)`, `29 (+1)` |
| `:` | between a key and its value, and between a name and a chat message |
| `→` | "goes to": trade rows (`5 emeralds → 1 diamond`), next steps (`Next →`) |
| `▶` | marks the player's own row in a ranking |
| `↳` | a detail line under an entry in a tooltip or a results block |
| `×` | a blocked action in a tooltip: `× Not enough coins` |

`»` and `>` are not required anywhere. If `DESIGN.md` uses one between a name and a message or between a key and an action, use it the same way everywhere and nowhere else.

Do not use symbols as decoration: no `✦ ✦ ✦`, no `═══` or `▬▬▬` rules, no stars around titles. Anything beyond the table (`✔`, `⚠`, `★`, emoji) is an icon. Icons belong in a resource pack (RESOURCE-PACK.md). Without one, use one only if `DESIGN.md` defines it, and check that it renders on every supported client.

## Numbers

- Group thousands: `2,500`, or the locale's separator (`2.500` in German).
- Durations in words or the two largest units: `2h 41m`, `22 hours and 31 minutes`. Countdowns and race times as clocks: `0:42`, `1:22.650`. Never `9842 seconds`.
- The unit follows the value and keeps its color: `250 coins`, `12 players`. With a pack, a currency icon replaces the word.
- Progress as `current/max`, with the maximum quieter (`430<separator>/450`).
- Changes after the value in brackets: `29 (+1)` with `+1` in success, `-3` in error, unchanged values in a neutral color.

## Lines and blocks

- One message is one line. A block of related lines starts with a headline; list items under it start with `•`.
- Separate blocks with a blank line. A full-width rule is only for a rare framed block (a game intro, final results), and only if `DESIGN.md` defines it.
- Columns in chat, holograms, and the tab list need measuring: the default font is not monospaced. Put the variable-width part last, so only one column is ragged (LIMITS.md).
