# Scoreboard

The sidebar is permanent screen space on the right edge. Players see it for hours, mostly out of the corner of their eye. It should answer "where am I and how am I doing?" in one glance, and nothing else.

With a resource pack, much of this can move into a HUD instead (RESOURCE-PACK.md). Outside of games, no sidebar at all is a good option when the HUD or the tab list already shows the essentials.

## What goes on it

- **In games:** the live state the player checks during play. The map, the round, what is left (players, teams, time), the standings, and the player's own score.
- **In lobbies:** a short personal summary: rank, currencies, playtime, friends online. Or almost nothing: the network name and the address.
- **Never:** news, ads, social links, or the same value twice.

## Anatomy

```
<brand>ʙᴇᴅᴡᴀʀꜱ                              title

<map_color>ᴍᴀᴘ<separator>: <highlight>Lighthouse       context, each label its own accent
<timer_color>ᴛɪᴍᴇ ʟᴇꜰᴛ<separator>: <highlight>12:41

<muted>ᴛᴇᴀᴍꜱ                                section
<team_red>Red Team                            4    rows, value right-aligned
<highlight>▶ <team_blue>Blue Team                         3    the player's own team
<team_green>Green Team                          0

<separator>example.net                          footer
```

- **Title**: the mode or place in small caps, centered by the client, in the brand color. The sidebar title is one of the brand's few spots, even when the mode has its own accent elsewhere. A network may prefix it with its short name (`NETWORK: BEDWARS`) for screenshots; then the prefix is brand and the mode name highlight.
- **Labels carry the color, values are highlight.** A label is a short small caps word (`ᴍᴀᴘ`, `ᴄᴏɪɴꜱ`, `ᴋɪʟʟꜱ`; TYPOGRAPHY.md); the value next to or under it is highlight. This keeps the sidebar colorful but readable, because the eye reads the bright values.
- **Each label has its own accent, never the brand.** Neighboring labels differ, and a label that names a thing uses that thing's accent everywhere: the `COINS` label is in the coins accent, and so are coins in chat and in menus. Labels without a thing of their own (`MAP`, `TIME LEFT`) take free accents from the set. A sidebar with every label in the brand color looks like one long stripe.
- **Section headers** (`TEAMS`) are quieter than labels: muted small caps, so the rows below carry the color.
- **Rows** in a list (teams, players, standings): name in its team or rank color, value in highlight, right-aligned through the score's number format (`NumberFormat.fixed(component)`, 1.20.3+). No padding with spaces.
- **The player's own row** (or team) stands out with a `▶` marker before it, or their name in highlight. In a ranking, show it even when the player is outside the top rows.
- **Blank lines** between groups. A drawn divider costs a line; with a resource pack, a small divider graphic under the title is the one exception.
- **Footer**: the address, once, last. Optional: the date and time in muted beneath it, which makes screenshots and bug reports more useful.
- Hide the red score numbers on every line that does not show a right-aligned value (`NumberFormat.blank()`).

## Two layouts

`DESIGN.md` picks one. Use it on every server of the network.

**Label and value on one line** fits games, where many facts compete for space:

```
<map_color>ᴍᴀᴘ<separator>: <highlight>Lighthouse
<players_color>ᴘʟᴀʏᴇʀꜱ ʟᴇꜰᴛ<separator>: <highlight>8
```

**Label above value** fits lobbies and short sidebars. It reads like a card:

```
<rank_color>ᴠɪᴘ
<highlight>Permanent

<coins_color>ᴄᴏɪɴꜱ
<highlight>2,500

<playtime_color>ᴘʟᴀʏᴛɪᴍᴇ
<highlight>2h 41m
```

Keep it to five pairs at most, with a blank line between them.

## Limits

- Under 24 characters per line. Test with a 16-character name.
- 15 lines is the hard limit. In games, 10 to 12; in lobbies, 5 to 10, or up to 14 for a label-above-value card with five pairs. Every line needs a unique entry, so blank lines need distinct invisible entries.
- Labels are one or two words. Values, names, and map names stay in their normal case.

## Updates

- Update only lines whose value changed.
- Never animate the title or cycle colors. Movement in peripheral vision pulls attention away from the game.
- Timers update once per second, counters when they change, nothing faster.
- Keep the line count stable. A sidebar that grows and shrinks makes every line jump. Show `-` or `0` instead of removing a line.

Let players turn the sidebar off.
