# Resource pack

A resource pack changes which surfaces exist. Icon glyphs, badges, painted menus, and a HUD are how the largest networks look the way they do. Read this file only when the network ships a pack; it overrides the other topic files where they conflict.

This file says what changes in the design. It does not cover how to draw the art or build the pack.

`DESIGN.md` says whether the pack is required. Most networks with a pack require it. If it is optional, every pack surface needs a text fallback that follows the other topic files.

## What changes

| Without a pack | With a pack |
| --- | --- |
| No prefix, or a game or feature name in brackets | One small icon before a system message, tinted with its category color |
| Tags in small caps | Badges: white label capitals on a shape in the tag's color |
| Sidebar scoreboard | Boss bar rows and an action bar HUD; a sidebar only for lists in games |
| Boss bar with a visible bar, one at a time | An invisible bar carries the top HUD, several rows at once |
| Chest menus with glass pane navigation | Painted menus: panel, heading, tabs, buttons, a pager, all on the slot grid |
| Unit words after values | Unit icons: `2,500` and a coin |
| Text titles | A display font or icon titles, for big moments only |

## Style

The default is **vanilla-plus**: the art extends vanilla's own look instead of replacing it. Light gray panels with black outlines and white rims, vanilla slots, the vanilla font for every sentence, vanilla tooltips. A player who has never joined recognizes every part, and the network can add menus for years without a designer. A network that wants its own look writes its own theme (colors, corners, button style, title alignment), decided in `DESIGN.md`. Whatever the theme, sentences stay in the vanilla font.

## Fonts

- **Keep the vanilla font** for all text: sentences, names, values. Every width in LIMITS.md still holds. A theme may set menu titles and section headings in the label font's small capitals; everything else stays vanilla.
- **Add a label font** for badges and HUD labels: small capitals, used only for tags and labels.
- **Never replace `minecraft:default`.** It changes every width on every surface, including other plugins' text.

Case follows TYPOGRAPHY.md: sentences stay in sentence case, labels stay short.

## Icons

- One icon per message category (info, success, error, social, reward), 8x8, sitting on the text baseline.
- Icons replace a prefix; they are not decoration. One icon per line, at the start.
- Keep icon meaning global: the same glyph for "reward" in chat, in menus, and on the HUD.
- Unit icons (a coin, a gem) replace currency words after values.
- A mouse icon can lead action lines in tooltips (`<mouse_left> Join`), if `DESIGN.md` defines it. Never a literal emoji.

## Badges

Rank, rarity, role, and status tags become badges, in the same accent the tag has everywhere else (COLORS.md).

- Rank badges sit before the name in chat, the tab list, and name tags.
- Rarity and category badges sit on the line under an item's name, up to three.
- Status badges (`AFK`, `LIVE`) sit after a name or inside a HUD element.

## HUD

- **Top rows** (invisible boss bars): context and timers. Location and time, the round timer, team scores, the queue. Each row has one job; context first, timers below.
- **Above the hotbar** (action bar): personal live values, such as currency, a cooldown, or a combo.
- Each element sits on a dark translucent pill, starts with an icon instead of a label, and stays in the same place on every server.
- Keep the vanilla HUD (hearts, hunger, hotbar, XP) unless the network reskins all of it.

## Menus

The layout rules in MENUS.md still apply: one job per menu, the stable shape, the tooltip order, the behavior. What changes:

- Navigation is painted and replaces the control row: tabs from the left of the top row when the menu has sibling views, and on the bottom row one joined pager (previous on columns 1–2, the page count on 3–5, next on 6–7), a tool on column 8, and the `?` help button on column 9. Buttons are colored by meaning. Glass panes are gone.
- Unavailable controls are drawn disabled, never hidden, with the reason in their tooltip.
- Content sits on drawn slots. The panel draws every content slot on a page, filled or not, so the shape stays the same.
- Every row spans all nine columns, so rows share their edges. Content fills whole rows from the left; a smaller block centered inside the grid looks crooked next to the full-width pager.
- The menu's name stays in the title row, like a vanilla chest. Section headings (dark gray with a line to the right edge) take a whole row and split content into groups.

## Titles

A title may be an icon (a trophy, an hourglass) instead of a word. The subtitle stays in the regular font and carries the value. When to use a title does not change (TITLES.md).
