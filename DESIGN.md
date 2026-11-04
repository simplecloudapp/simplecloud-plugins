# SimpleCloud plugins design

The SimpleCloud plugins are the default look of every network that runs on SimpleCloud: proxy commands, server
notifications, chat and tab list formats, the server list, kick screens, server signs, and NPCs with their holograms
and menus. Most readers are server owners and their
team, the rest are players on a fresh network. Everything stays close to vanilla Minecraft and gets out of the way.
Character: calm, clear, blue. Every string in every plugin must look like it came from the same product.

## Priority order

1. Preserve facts: names, counts, states, permissions, placeholders.
2. Preserve the server owner's configuration.
3. Make the state and the next action clear at a glance.
4. Follow the decisions and tokens in this file.
5. Fit the surface's limits.
6. Refine hover text, click actions, and alignment.

## Decisions

| Decision | Choice |
| --- | --- |
| Brand color | `#3B95FF`, a clear blue (the SimpleCloud Demo network's blue theme) |
| Brand spots | the prefix, the network name (tab list header, MOTD), the team's rank format, hover titles, menu item and hologram names without an accent, clickable things through `interactive` |
| Chat style | **Line color**: every message is one color for the whole line. Normal messages are `info`; only success, warning, and error change the line color |
| Prefix | a **bold lightning** `⚡` in brand, before every system message, followed by one space |
| Prefix form | `<brand><b>⚡</b></brand>`, then one space in the message; never a name, never brackets |
| Clickable words | no brackets, in `interactive` |
| Panels | not used yet; when added, buttons without brackets, accept in success, decline in error |
| Tone | friendly, second person, `!` only for good news |
| Tags | small caps, plain text |
| Rank format | team ranks show the rank in small caps, then name and `:` in brand, message in `#CDE3FF`; everyone else shows name and `:` in `#A8A8AB`, message in `#E1E1E4`. No brackets, no `»`, no `\|` |
| Sidebar | none |
| Menus | NPC menus stay vanilla: plain titles, no glass pane frames |
| Menu actions | `Click to join`: a short sentence in `interactive` |
| Signs | glowing text, the state in small caps |
| Resource pack | none: the plugins cannot send one, so everything is plain text |
| Language and numbers | English, `2,500`, `2h 41m` |

The palette, the line color chat style, the lightning prefix, the rank format, and the vanilla look come from the
SimpleCloud Demo network's `DESIGN.md`. That file is the source of truth for them. The current plugin defaults do not
follow it yet and will be changed to match this file.

## Project facts

- Proxies: Velocity, BungeeCord, and Waterdog (server-connection only). Servers: Paper and Minestom.
- Bedrock players can reach the network through Waterdog. They see the nearest legacy color, so `muted` and `separator`
  turn blue there. That is accepted.
- No resource pack, so no custom glyphs, badges, fonts, tooltip frames, or menu textures.
- Plugins and their surfaces:
    - command (`/cloud`): command feedback, lists, info blocks
    - notify (`/scnotify`): server, group, and persistent server notifications with hover; a clickable toggle list
    - server-connection: `/lobby`-style commands, fallback kick screens
    - proxy-essentials: tab list header and footer, server list (MOTD, version text, player list hover), join state kick
      screens, ping colors
    - prefixes: player chat, tab list names, name tags
    - signs (`/scsigns`): server signs, command feedback, a paged sign list
    - npcs (`/scnpcs`): holograms, click messages, server selector menus, the in-game editor, chat info pages
- Every plugin has a `messages.yml` with a `variables` section; `<prefix>` is defined there.

## Tokens

### Color roles

| Role | Hex | Use |
| --- | --- | --- |
| brand | `#3B95FF` | the prefix, the network name, hover titles, the team's rank format |
| interactive | `#62ADFF` | clickable words and list entries, hover action lines |
| highlight | `#CFE4FF` | values in hovers, the brightest text |
| muted | `#A8A8AB` | keys in hovers, quiet hints, command arguments, stopped and deleted things, second lines of kick screens and the MOTD |
| separator | `#5F5F63` | `( )`, `:`, `/`, `•` |
| info | `#4FA3FF` | the line color of every normal message, actions in progress |
| success | `#6AD13C` | completed actions, online servers |
| warning | `#FF9029` | no-ops, empty results, maintenance, temporary refusals |
| error | `#F5464F` | failures, missing permission, invalid input, full servers |

Rules:

- A chat message is the prefix plus one line color. Values are not highlighted; the only things that keep their own
  color inside a line are a player's rank format, name, and head.
- Pick the line color by what happened: `info` unless the message reports a success, a warning, or an error.
- Separators are always `separator`.

### Accents

| Accent | Hex |
| --- | --- |
| mint | `#1FD98E` |
| violet | `#9566FF` |
| pink | `#E94BA6` |
| gold | `#F2C230` |
| cyan | `#22C3DB` |
| amber | `#FFA329` |

The brand's blue is not an accent, so the brand stays a signature.

### Accent assignments

No plugin assigns an accent yet: chat uses the line color, and the team uses brand. A new kind of colored thing (a
server group on a sign, an NPC kind) takes the next free accent in table order: mint, violet, pink, gold, cyan, amber.

### Rank tints

| Rank | Name and `:` | Message |
| --- | --- | --- |
| team (`owner`, `admin`) | brand `#3B95FF` | `#CDE3FF` |
| everyone else (`default`) | muted `#A8A8AB` | `#E1E1E4` |

### Server states

The same state has the same color and word on every surface (signs, holograms, menus, the MOTD).

| State | Role | Word |
| --- | --- | --- |
| online | success | Online |
| starting | brand, shining to highlight | Starting |
| full | error | Full |
| maintenance | warning | Maintenance |
| offline | muted | Offline |

### Ping colors

| From | Role |
| --- | --- |
| 0 ms | success |
| 100 ms | warning |
| 200 ms | error |

### Syntax

- Every `messages.yml` gets a `palette` section with the role and accent hex values. Each entry is registered as a
  styling tag (`<brand>`, `<interactive>`, `<highlight>`, `<muted>`, `<separator>`, `<info>`, `<success>`,
  `<warning>`, `<error>`, `<mint>`, `<violet>`, …) that colors the text after it, unlike `variables`, which insert
  text.
- `<prefix>` stays in `variables`: `prefix: '<brand><b>⚡</b></brand>'`. Messages write one space after it:
  `<prefix> <info>...`.
- Placeholders are named (`<group>`, `<server>`, `<playername>`, `<count>`); anything a player typed (`<message>`,
  `<reason>` from input) goes in through `Placeholder.unparsed`.
- Sign layouts and NPC holograms and menus understand the same role tags. The NPC plugin reads its palette from its
  own `messages.yml`.
- No hex codes in message strings outside the `palette` section, except the rank tints and `<shine>` colors.

### Symbols

| Symbol | Use |
| --- | --- |
| `⚡` (bold, brand) | the prefix, nothing else |
| `•` (separator) | list bullets |
| `·` (separator) | between the state and the player count on signs, between facts in one line of an admin page |

No other decorative symbols: no `»`, `«`, `┃`, `|`, `-`, `├─`, `└─` as bullets, `═══` rules, `✦`, `★`.

## Components

### Messages

```
<prefix> <info>Starting 2 servers for group lobby.
<prefix> <success>Reloaded the prefixes!
<prefix> <warning>Group lobby has no running servers.
<prefix> <error>You don't have permission to use this command.
<prefix> <error>Use /cloud group start <group> instead.
```

Lists and info blocks: a title line with the prefix, then one entry per line, indented with a bullet. Entries are
`info`; separators stay `separator`.

```
<prefix> <info>Groups <separator>(</separator>3<separator>)</separator>
<separator>•</separator> <info>lobby <separator>(</separator>12/20 online<separator>)</separator>
<separator>•</separator> <info>bedwars <separator>(</separator>0/40 online<separator>)</separator>

<prefix> <info>Server lobby-1
<separator>•</separator> <info>State<separator>:</separator> Available
<separator>•</separator> <info>Players<separator>:</separator> 12/20
```

An empty list is one `warning` line instead of the title: `<prefix> <warning>No groups were found.`

Paged lists end with one control line: Previous and Next in `interactive`, shown only when that page exists, the page
count in `info`. An entry that does something on click is `interactive` and has a hover action line.

```
<interactive>Previous</interactive>  <info>Page 2/5</info>  <interactive>Next
```

Help lists: a title, then one command per line. The command itself is `interactive`, its arguments `muted`; clicking
types the command into the chat. Notes after the list are `muted`, without a bullet.

```
<prefix> <info>Notify commands
<separator>•</separator> <interactive>/scnotify list
<separator>•</separator> <interactive>/scnotify enable</interactive> <muted><type>
```

### Notifications

The line color follows the event. Something in progress (starting, stopping, created) is `info`, available is
`success`, and gone (stopped, deleted) is `muted`, like offline everywhere else. A server that stops is not an error;
`error` is only for failures. The name of the thing carries the hover.

```
<prefix> <info>Server lobby-1 is starting.
<prefix> <success>Server lobby-1 is now available.
<prefix> <info>Server lobby-1 is stopping.
<prefix> <muted>Server lobby-1 has stopped.
```

Command feedback follows the same rule: an action that was only started is `info` (`Stopping server lobby-1.`), one
that is done is `success` (`Reloaded the prefixes!`).

### Hovers

Order: title in brand, facts as `<muted>Key: <highlight>value`, an action line in `interactive` if clicking does
something.

```
<brand>lobby-1
<muted>State: <highlight>Available
<muted>Address: <highlight>10.0.0.4:25565
<muted>Players: <highlight>12/20
```

### Toggle lists

Each entry is clickable and in `interactive`. The state word is the one value in a line that keeps its own color:
`success` for enabled, `error` for disabled.

```
<prefix> <info>Your notifications
<separator>•</separator> <interactive>Server starting <separator>(</separator><success>enabled<separator>)</separator>
<separator>•</separator> <interactive>Server stopped <separator>(</separator><error>disabled<separator>)</separator>
```

Hover: `<interactive>Click to toggle`.

### Prompts

When the editor asks for chat input: one line with the prefix and the question in `info`, one `muted` line with what
to type, and `interactive` words for cancel.

```
<prefix> <info>Type the new hologram line in chat.
<muted>MiniMessage works, for example <brand>Lobby</brand>. <interactive>Cancel
```

### Player chat, tab list names, name tags

```
<brand>ᴏᴡɴᴇʀ <playername>:</brand> <#CDE3FF><message>
<brand>ᴀᴅᴍɪɴ <playername>:</brand> <#CDE3FF><message>
<muted><playername>:</muted> <#E1E1E4><message>
```

- Tab list: only the name in the rank color, no tag: `<brand><playername>`, `<muted><playername>`.
- Name tag: the same tag and name as in chat, without `:`, e.g. `<brand>ᴀᴅᴍɪɴ <playername>`.
- A rank tag is the rank name in small caps; the default rank has none.

### Tab list

The footer's first line has a space at both ends, so the tab list is wide enough when only one column of players
shows. Other lines have no extra spaces, because the client centers spaces too. The network name and the address are in
small caps.

```
header: '<br><brand>ꜱɪᴍᴘʟᴇᴄʟᴏᴜᴅ<br>'
footer: '<br> <highlight><online_players> <muted>online on your network <br><separator>ꜱɪᴍᴘʟᴇᴄʟᴏᴜᴅ.ᴀᴘᴘ<br>'
```

### Server list and kick screens

- MOTD line 1 is the network name in brand and small caps, centered; line 2 is one short sentence in `muted`, or the
  join state in its color.
- The version text only shows a state: `<warning>Maintenance`.
- The player list hover is plain: the network's address in `muted`.

```
line1: '<center><brand>ꜱɪᴍᴘʟᴇᴄʟᴏᴜᴅ</center>'
line2: '<center><muted>A network powered by simplecloud.app</center>'

line1: '<center><brand>ꜱɪᴍᴘʟᴇᴄʟᴏᴜᴅ</center>'
line2: '<center><warning>In maintenance, back soon.</center>'
```

Kick screens: no prefix. Line 1 says what happened in its state color, an empty line, then what to do in `muted`.

```
<warning>The network is in maintenance.<br><br><muted>Try again later.
<error>The network is full.<br><br><muted>Try again in a few minutes.
<error>There is no lobby available.<br><br><muted>Try again in a few seconds.
```

### Signs

Four lines, glowing text: empty, the server name in `highlight`, the state, empty. The state is a small caps word in
its state color; online and full add the player count in `muted` after a `·`.

```
''
'<highlight><server-name>'
'<success>ᴏɴʟɪɴᴇ <separator>· <muted><player-count>/<max-players>'
''

'<shine:ꜱᴛᴀʀᴛɪɴɢ:#3B95FF:#CFE4FF>'
'<error>ꜰᴜʟʟ <separator>· <muted><player-count>/<max-players>'
'<warning>ᴍᴀɪɴᴛᴇɴᴀɴᴄᴇ'
'<muted>ᴏꜰꜰʟɪɴᴇ'
```

### Holograms

Two lines: the target's name in `brand` (`muted` while offline), then one line in `muted` or the state color. No bold.

```
<brand><target_name>
<muted><target_online_players>/<target_max_players> online

<brand><target_name>
<warning>Maintenance

<muted><target_name>
<muted>Offline
```

Clicking a target that cannot be joined sends one chat message with the prefix:
`<prefix> <warning>Lobby is in maintenance.`, `<prefix> <error>Lobby is offline.`

### Menus

- The title is one plain noun without color tags, so it keeps vanilla's title gray: `Servers`.
- Item names never italic. A server or group is named in `brand`; controls (Previous Page, Next Page, Close) in
  `interactive`.
- Tooltip order: name, description in `highlight`, facts as `<muted>Key: <highlight>value`, one action line in
  `interactive`. A state that blocks the action replaces the action line, in the state color.
- No glass pane frames or filler. Empty slots stay empty.

```
<brand>lobby-1

<muted>Players: <highlight>12/20
<muted>State: <highlight>Online

<interactive>Click to join
```

```
<brand>bedwars-2

<muted>Players: <highlight>16/16

<error>This server is full.
```

### NPC editor

The editor is an admin tool built from menus and tooltips. It uses the same roles: item names in `brand`, descriptions
and keys in `muted`, values in `highlight`, quiet hints in `separator`, click keys (`Left`, `Right`, `Q`) in
`interactive`, and the state colors for on, off, warnings, and errors.

### Sounds

No plugin plays a sound by default; NPC actions only play the sounds an owner adds. Typed commands are silent.

## Writing

- Sentence case everywhere except item and menu entry names (Title Case): `Join a Server`, `Next Page`.
- End every sentence with a period, or `!` for good news. No `...`.
- Address the reader as "you". Name the thing: `Group lobby`, not "the target".
- Failures say what is missing or what to do: `Use /cloud group start <group> instead.`, never `Error!`.
- Name a plugin inside the sentence when it matters (`Reloaded the prefixes!`), never as a label in front of it.
- Skip "SimpleCloud" and "Successfully" in feedback: `Notify was reloaded!`, not
  `Successfully reloaded SimpleCloud Notify.`

## Reject

- A plugin or network name in front of a message (`SC |`, `[Lobby]`, `SimpleCloud »`).
- Any prefix other than the bold lightning, or a missing space after it.
- Highlighted values inside a chat line, or two line colors in one message (toggle state words excepted).
- `»`, `|`, or `-` as separators or bullets, in chat formats and lists alike.
- Rank tags in brackets, bold rank names, or a rank tag for the default rank.
- Colored or bold menu titles, glass pane frames, and filler items in player menus.
- `...` for loading or pending states; say the state (`Starting`).
- Pure white (`#FFFFFF`, `#F8FAFC`) or named colors (`<red>`, `<green>`, `<gold>`) in message strings. The one
  exception is `<white>` around a player head (`<white><head:<playername>:true></white>`), so the head is not tinted.
- Raw hex in message strings outside the `palette` section, except the rank tints.
- Gradients, rainbow text, bold or capitals for emphasis.
- `&` or `§` color codes in configs.
- Anything that needs a resource pack: custom glyphs, badges, fonts, negative space.

## Review in this order

1. **Facts**
2. **State**
3. **Decisions and tokens**
4. **Voice**
5. **Limits**
6. **Restraint**
