# Tab list

The player list opens when a player holds Tab. They look for one thing: who is here, or where they are. Design for that lookup.

Many networks manage the tab list with a plugin (TAB, a proxy plugin, or an in-house one) and a config file. In that case, the work is writing that config well, not code: the rules below apply to the strings and options it exposes.

## Header and footer

- Header: where the player is. The network name or logo, optionally the current mode or server below it.
- Footer: one useful line, such as which server the player is on, or how many players are online. The address last.
- Pad each with one empty line at the outer edge, so the text does not touch the list or the screen border.
- 2 to 4 lines each. A header taller than the list pushes player names down the screen.
- The client centers each line. Do not add spaces to center.

```
header: <br><brand>Example Network<br>
footer: <br><muted>You are on <highlight><server><br><separator>example.net<br>
```

With a resource pack, the header can be a logo glyph instead of text.

## Player entries

- Rank tag, then the name, in the same rank colors as chat and name tags, so a rank looks identical everywhere. The default rank shows no tag, or a gray `PLAYER` tag if `DESIGN.md` wants every name to have one.
- Keep entries short. The list has columns of 20; one long prefix widens every column.
- Do not put coins, levels, or ping text into the entry. Ping has its own bars.
- Sort by rank, then by name. On 1.21.2+ use the list order field; on older versions sort through team names (`000Owner`, `010Admin`, …), which the client sorts alphabetically.

## In games

In a match, team identity matters more than rank. Color names by team instead of rank, and drop rank tags if they make entries too wide.

- Group players by team: a team header row in the team color, members below it.
- The footer carries the live match state: `<highlight>32<separator>/<muted>32 alive`, teams left, time.
- Show eliminated players dimmed or in spectator style, not removed, so the roster stays readable.

## Updates

Update the header and footer when their values change, at most once per second. Never animate them; the list is open only briefly and motion makes it harder to scan.
