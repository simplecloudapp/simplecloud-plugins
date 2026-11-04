# Menus

Large networks draw their menus with a resource pack: painted panels, tabs, buttons, and headings. Without a pack, a chest menu can still look deliberate. The difference is layout: a clear shape, empty space used on purpose, controls that always sit in the same place, and the same tooltip order on every item.

With a resource pack, read RESOURCE-PACK.md as well; the layout and tooltip rules here still apply.

## When to use a menu

| Task | Use |
| --- | --- |
| Pick one of several destinations, kits, or options | Menu |
| Browse many items with icons | Menu with pages |
| Toggle a few settings | Menu |
| Confirm an action started in a menu | Menu |
| Type a name, a search, or an amount | Dialog on 1.21.6+ (DIALOGS.md) |
| Answer an invite, confirm a command | Chat panel (CHAT.md) |
| Show a report or a list of names | Chat |

Bedrock players see menus through Geyser as a translated inventory. For flows they use often, consider Floodgate forms.

## One job, no filler

- One menu does one job. A navigator navigates; it does not also show stats, news, and a shop.
- Never fill empty slots with glass panes, not even as a frame. Empty space is what makes a menu look calm. If empty slots let players drop items in, fix the click handling.
- Never put menu buttons in the player's own inventory below the chest.

## Size and shape

Size the chest for the layout, not for the item count. Eight games crammed into a 3-row chest edge to edge look like a storage box; the same eight games centered in a 5-row chest with room around them look like a menu.

- **Use the inner seven columns** (2 to 8) for content. The empty column on each side frames the menu without any decoration.
- **Align rows the same way everywhere.** Centered rows look calm: center odd counts on column 5, even counts with the middle slot empty. Rows filled from the left inside the inner columns also work, if every menu does it.
- **Group with empty space.** Separate groups (games, then teleports) with an empty row, not with a divider item. Put the most important group first.
- **Give highlights room.** In a navigator with many games, put the three to five most played ones in their own centered row at the top, and the rest in a grid below. New or featured games can sit alone in the middle of a row.
- **Keep the shape stable.** Size the menu for its fullest page and keep every item in the same slot across pages and visits. Players learn positions, not names.

```
Navigator, 6 rows, 15 games and 3 teleports
. . G . G . G . .     featured games, centered, with gaps
. G G G G G G G .     all other games, inner seven columns
. . G G G G G . .     a short last row, centered
. . . . . . . . .     empty row between groups
. . . T T T . . .     teleports, centered
. . . . . . . . .     control row, empty when there is only one page
```

## The control row

Menus with pages, levels, or tools use the bottom row for controls, and every menu puts each control in the same column (counted 1 to 9 from the left):

| Column | Control |
| --- | --- |
| 1 | Previous page; on the first page of a submenu, Back |
| 5 | Page indicator (`Page 2/5`), optional |
| 3, 7 | Tools: sort, filter, search, or the menu's main action (create, claim all) |
| 9 | Next page |

- Previous and Next sit in the outer corners, the first and last slot of the row, where players look for "back" and "forward". Content never uses those slots, because it stays in the inner seven columns.
- Show Previous and Next only when that page exists. Never a button that does nothing.
- Back is not a separate button: on the first page of a submenu, the first slot turns into Back (same pane, named `Back`) and returns to the previous menu at the same page.
- No close button. Escape and the inventory key already close every menu.

**Navigation items are colored glass panes.** Without a resource pack there is no good arrow item, so use stained glass panes for navigation across the whole network, with the direction in the name: `Previous page`, `Next page`, `Back`. Previous and Next share one color that stands out from the content and matches the palette (the brand's pane color, or lime). Back gets a different color, by default red, because it leaves the menu instead of turning a page. White or gray panes disappear against the light inventory background. A pane reads as part of the menu's frame, not as content, and keeps every menu consistent. Doors, arrows, and arrow-skinned heads look like items the player could take or buy.

**Tools use a fitting vanilla item**: a hopper to sort, a comparator to filter, a name tag or an oak sign to search, a book for help. Each tool's item and slot are the same in every menu.

**Tabs** go in the top row when several menus belong together (profile, friends, party, settings). One item per sibling menu, in the same slots on each of them, with the active one marked by the enchantment glint and its name in highlight.

## Title

- One plain noun: `Games`, `Settings`, `Cosmetics`. No colors, symbols, or network name.
- Leave the vanilla title color. The background is light gray; light colors disappear (COLORS.md).
- Show depth with a breadcrumb: `Settings > Privacy`, `Realm > Members`. It tells the player where Back leads.

## Item names

- Title case, one to three words, so the name stands above the description.
- **The name's color follows the thing.** A thing with its own accent keeps it in menus too: categories, tabs, skills, crate tiers, games, ranks, and rarities are named in their accent, so a row of them reads as different things and matches their tags, NPCs, and holograms. Items without an accent of their own (a profile, a mailbox, a settings entry) are named in the brand color. Controls are not content: page buttons, Back, and tools are named in the interactive color.
- Real items (an Iron Ingot in a shop or a reward) keep their own name, or their rarity color if the network has rarities.
- A row of brand-colored names next to brand-colored tabs is the monotone menu to avoid.
- Turn off the default italics: `<!italic>` on custom names. On 1.20.5+ the `item_name` component is never italic and is preferred, **except for player heads with a profile**: the client renders `<name>'s Head` there, so heads still need a custom name with italics off.
- The icon is the meaning: the most literal vanilla item, or a custom model with a resource pack (`item_model` component, 1.21.2+).

## Tooltip order

Every item follows the same order. Leave out blocks that do not apply, and put a blank line between blocks:

```
<!italic><brand>Skyfall
<!italic><minigame_color>ᴍɪɴɪɢᴀᴍᴇ <pvp_color>ᴘᴠᴘ                   tags, each its own accent

<!italic><highlight>Fight on islands that crumble            description, 2 to 3 lines,
<!italic><highlight>into the void, one block at a time.      about 30 to 40 characters each

<!italic><muted>Playing: <highlight>42                          facts
<!italic><muted>Your best: <highlight>2:04.310

<!italic><interactive>Left-click to join                       actions
<!italic><interactive>Right-click to spectate
```

1. **Name**, in the brand or rarity color.
2. **Tags**: up to three short small caps words (TYPOGRAPHY.md), each in its accent (rarity, category, mode). Tags side by side have different accents, and a tag keeps its accent on every item: `PVP` is the same red everywhere. A status that describes the item (`POPULAR`, `NEW`) goes here too. Optional.
3. **Description**: what it is or does, in highlight or one light tint that `DESIGN.md` reserves for descriptions. Not in the quiet gray: the description is what the player reads. Break lines yourself; lore never wraps.
4. **Facts**: `Key: value` lines, keys in the quiet gray, values in highlight. Live values (players waiting, time left, price) go here. Show changes and requirements next to the value: `Level: 29 (+1)`, `Oak Planks (12/64)` with the missing count in error.
5. **Actions**: one line per mouse button, as a short sentence: `Left-click to join`. A keybind component (`<key:key.attack>`) instead of the words "Left-click" follows the player's own controls.
6. **Blocker**: if an action is not possible, replace that action line with the reason, in error: `× Not enough coins (250/500)`, `Requires VIP`. Keep the actions that still work, such as `Left-click to preview`.

Aim for under 12 lines. Tooltips that list requirements or upgrade stats can run longer if every block keeps its heading and blank line. Hide vanilla noise: attribute modifiers, enchantments, potion effects, dye colors.

`DESIGN.md` may give each mouse button its own color (for example left green, right cyan, shift orange) so players learn them across all menus, or pick a shorter form such as `Left-click » Join`. Whatever it picks, every item in every menu uses it.

## State

State is a changed tag or blocker plus different actions, not a different-looking menu:

| State | Shown as | Actions |
| --- | --- | --- |
| Available | nothing extra, or a `POPULAR` / `NEW` tag | normal |
| Selected, equipped, active | `SELECTED` tag in success, plus the enchantment glint | `Left-click to unequip` |
| In progress | `IN PROGRESS` tag in the blue accent, the same on every menu | the actions for the running state |
| Locked | blocker line in error saying what unlocks it: `Requires VIP`, `Reach level 10` | only actions that still work, such as preview |
| Unknown, undiscovered | name `???` or `Unknown …` in muted, a silhouette icon (a gray dye, or a silhouette model with a pack) | none |

Use the enchantment glint override only for selected or active, never to make an item look special.

## Behavior

- Block every action that could move items into or out of the menu: clicks in the menu, shift-clicks and double-clicks from the player's own inventory, number keys, and drags (a separate event). Item duplication through menus is the most common menu bug.
- Open, close, or switch menus one tick after the click, not inside the click handler.
- Respond to every click: open, toggle, a sound, or a changed tooltip. A click that does nothing reads as lag.
- Opening a menu is its own feedback, whatever opened it: a command, an NPC, a block, an accepted request. Never send a chat line as well ("Opening the shop…", an NPC greeting, "Trading with Alex"). Put what the player needs to know into the menu: a tooltip, the title, an info item. A sound may come with it, nothing else.
- Update items in place. Never close and reopen a menu to refresh it.
- Close the menu before sending the player to another server or teleporting them.
- Sort content predictably (available first, then by name) and keep the order stable while the menu is open.
