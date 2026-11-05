# Limits

Hard numbers for every surface. Design inside them, then test against them. When a value depends on client version, design for the oldest version the network supports and enhance upward.

## The default font

Minecraft's default font is a pixel font with variable widths. Width is measured in pixels at GUI scale 1, including the 1px gap after each glyph.

| Glyphs | Width |
| --- | --- |
| Most letters, digits, `#`, `$`, `%`, `&`, `-`, `_`, `=`, `+`, `/`, `?` | 6px |
| `i`, `!`, `.`, `,`, `:`, `;`, `\|`, `'` | 2px |
| `l`, `` ` `` | 3px |
| `I`, `t`, space, `[`, `]`, `(`, `)`, `{`, `}`, `*`, `"` | 4px |
| `f`, `k`, `<`, `>` | 5px |
| `@`, `~` | 7px |
| Any **bold** glyph | +1px |

Rules that follow:

- Estimate with 6px per character, then check strings that are mostly wide glyphs (`W`, `M`, `@`) or bold.
- Bold costs width on every glyph. A bold 20-character title is 20px wider than its regular version.
- Never center by hand-counting spaces. Measure the rendered component and pad it in code with a mix of regular spaces (4px) and bold spaces (5px), which reaches any width from 12px up.
- `»`, `«`, `•`, `↳`, `▶` and small caps letters are not in the 6px table above. Measure them before using them in centered or aligned text.
- Unicode outside Latin-1 (small caps, box drawing, CJK, emoji fallbacks) falls back to other bitmap or unifont glyphs with different widths and a different look. Measure them, never assume 6px.

## Surface sizes

| Surface | Limit | Practical budget |
| --- | --- | --- |
| Chat line | 320px at default chat width | about 50 characters before wrap; keep messages to one line |
| Chat history | 100 lines kept, about 10 visible when closed | a burst of more than 3 lines pushes everything else away |
| Item name | no hard limit, never wraps | under 30 characters |
| Item lore line | no hard limit, never wraps; the tooltip widens instead | under 40 characters per line, under 12 lines |
| Inventory title | container width: 176px for chests | under 25 characters |
| Chest inventory | 9 columns, 1 to 6 rows | see MENUS.md |
| Sidebar scoreboard | 15 lines plus title | 10 to 12 lines, under 24 characters each |
| Tab list | 80 entries shown, 20 per column | header and footer under 4 lines each |
| Title | rendered at 4x scale | under 16 characters |
| Subtitle | rendered at 2x scale | under 35 characters |
| Action bar | one line above the hotbar, fades after 3 seconds | under 40 characters |
| Boss bar | title above the bar, 182px bar | under 40 characters; 1 to 2 bars at once |
| Sign line | 4 lines, 90px each (hanging signs 60px) | about 14 characters, 9 on hanging signs; dark text on light woods, glowing text on dark woods |
| Text display | `line_width` default 200px, then wraps | 3 to 5 lines |
| MOTD | 2 lines, 270px each (row is 305px, text starts after the 32px icon and a 3px gap) | under 45 characters per line |
| Server list hover | player sample list, one entry per line | under 10 lines |
| Player name (Java) | 3 to 16 characters, `A-Z a-z 0-9 _` | always design for 16 |

## Names

Every string that contains a player name must still fit when the name is 16 characters wide. Test with `WWWWWWWWWWWWWWWW` (96px), not with `Steve`.

Bedrock players joining through Geyser and Floodgate get a configurable prefix (default `.`), and long gamertags are truncated to fit 16 characters. Never parse a name to find out whether it is a Bedrock player; ask Floodgate.

Nicknames, prefixes, and suffixes stack. `[Admin] ` + 16-character name + ` ✦` is already 25 characters before the message starts.

## Color support

| Client | Colors |
| --- | --- |
| Java 1.16+ | full RGB via hex |
| Java below 1.16 (through ViaVersion) | 16 legacy colors; hex is rounded to the nearest one |
| Bedrock (through Geyser) | 16 legacy colors plus a few Bedrock-only shades; hex is rounded |
| Java 1.21.4+ | configurable text shadow color |

So every palette needs a legacy fallback that still reads correctly. See COLORS.md.

## Version-gated features

Only use these when the network's minimum client version supports them, or provide a fallback.

| Feature | Since |
| --- | --- |
| Hex colors, custom fonts in components | 1.16 |
| Text display entities | 1.19.4 |
| Two-sided signs, sign editing lock | 1.20 |
| Hidden scoreboard numbers, per-score display names | 1.20.3 |
| `item_name` component (no forced italics) | 1.20.5 |
| Custom tooltip styles, item models via components | 1.21.2 |
| Tab list order without team-name sorting | 1.21.2 |
| Text shadow color | 1.21.4 |
| Dialogs (server-defined forms) | 1.21.6 (Paper API 1.21.7) |
| Mannequin entities (player-shaped NPCs with a description line) | 1.21.9 |
| Player heads and atlas sprites inside text (object components) | 1.21.9 |

Since 2026, Minecraft uses year-based versions (`26.1`, `26.2`, `26.3`, …). Everything in this table is available on every 26.x client. Networks that only support 26.x can use all of it without fallbacks; check this table only when older clients join through ViaVersion.
