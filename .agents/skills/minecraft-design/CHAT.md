# Chat

Chat is the surface every plugin uses and every player shares. A system message competes with conversations, other plugins, and the game itself. Earn the line.

## Chat styles

`DESIGN.md` picks one style for the whole network. Default: **highlighted values**. Neither needs a symbol between the parts of a message.

### Highlighted values (default)

The body is a quiet neutral, and the one thing the player looks for (a name, a number, a place) is in highlight. Errors are in the error color, so the failure is unmistakable; a command's syntax or a player name inside the error may stay neutral.

```
<muted>Teleporting you to <highlight>Bedwars<muted>.
<muted><highlight><player><muted> was eliminated by <highlight><killer><muted>.
<muted>You finished in <highlight>2:04.310<muted>, <error>3.2s<muted> behind your record.
<error>You need to finish the tutorial before you can do that.
```

Calm, and the eye still lands on the value. Works for every kind of server. A value that is a thing with its own accent (coins, a game, a rank) may use that accent instead of highlight: `<coins_color>250 coins`.

### Line color

Every system message is one color for the whole line, chosen by its category (for example success, info, social, error). Values are not highlighted; the only thing that keeps its own color inside the line is a player's rank tag and name. A clickable word inside the line takes the network's clickable look (Hover and click).

```
<success>You paid 1,250 coins to <rank_tag> <rank_color><player><success>!
<info>Teleporting to Spawn!
<social>You invited <rank_tag> <rank_color><player><social> to your party.
<error>Usage: /trade <player>
```

The strongest at a glance and the simplest to keep consistent. Needs one color per category in `DESIGN.md`: `<info>` and `<social>` join the success and error roles.

## Prefixes

`DESIGN.md` says whether messages have a prefix. Default: **no prefix**.

A prefix names what is talking, when the player cannot tell otherwise:

- **A game's name**, inside a game, when several games share one chat. Lobby messages have none.
- **A feature's name** (`Party`, `Friends`), on that feature's messages, so a party notice never looks like a lobby message.
- **An icon**, with a resource pack: one small glyph per category, at the start of the line (RESOURCE-PACK.md).

It never names the network or the plugin: the player knows where they are.

`DESIGN.md` defines one form, and every prefix uses it:

- **Frame**: brackets (`[Bedwars]`), a trailing symbol (`Bedwars »`, `Bedwars |`), or the name alone.
- **Color**: the brand, or the game's or feature's own accent; and the frame's color.
- **Case**: as written, or small caps.
- **Icon**: with a resource pack, an icon or a badge before or instead of the name.

Without a `DESIGN.md` decision on the form, use the accent in dark gray brackets:

```
<separator>[<accent>Bedwars<separator>] <muted>Red team's bed was destroyed by <highlight><player><muted>.
<separator>[<accent>Bedwars<separator>] <error>You can't place blocks here.
<separator>[<social>Party<separator>] <rank_color><player><muted> joined your party.
```

## Writing a message

Write the sentence a friendly person would say to the player, in second person, and stop.

| Before | After | Why |
| --- | --- | --- |
| `You have been successfully teleported to the server Bedwars-3!` | `Teleporting you to Bedwars.` | Says what happens; the server number means nothing to a player |
| `Error: Not enough coins` | `You need 250 more coins to buy this.` | Says what is missing and how much |
| `[Bedwars] » Player Alex has been killed by Sam` | `[Bedwars] Alex was eliminated by Sam.` | A plain sentence; no arrow, no "Player" |
| `Usage: /party <invite\|kick\|leave> <player>` | `Use /party invite <player> to invite someone.` | Shows the common case; the full list goes in help |
| `!!! Double coins event started !!!` | `Double coins is active for the next hour!` | One exclamation mark carries the good news |

- Exclamation marks are for good news: rewards, wins, a friend coming online. Never for errors.
- End sentences with a period, or an exclamation mark for good news.
- Name the thing: the game, the item, the player. Never "the server" or "the target" when there is a name.

## One message, one line

- A response is one line. If it needs two, the second is the fix or the next step.
- Several related facts are a short block: a headline, then one line per fact. Use blocks for game results, rewards with several items, and help pages; keep everything else to one line.

```
<success>You received the following rewards:
<separator>• <highlight>75 coins
<separator>• <highlight>1 Bronze crate
```

## Panels

A panel is a message that waits for the player: a party invite, a friend or trade request, a confirmation, an NPC talking. Other messages report something; a panel asks for an answer, so it stands apart from the conversation around it.

It has three parts:

- **The source**: the feature's name (`Party`, `Friends`) in the prefix form, or the speaker's name in their accent.
- **The message**, in the chat style. A request says who wants what in one sentence; an NPC may speak two or three lines.
- **The answers**, on their own line: one or two buttons with verb labels (`Accept`, `Decline`, `Open shop`), never `Yes` and `No`. Each in the color of what it does: accepting in success, declining or a destructive action in error, anything else in highlight. Brackets or not, as clickable words look.

A short panel is two lines:

```
<separator>[<social>Party<separator>] <rank_color><player><muted> invited you to their party.
<success>[Accept]  <error>[Decline]
```

A longer one is a block with an empty line before and after it, and the lines under the source indented with spaces, so it reads as one piece:

```

<accent>Maren
   <muted>Back already? Bring me <highlight>amber shards<muted> and
   <muted>I'll forge you something nice.
   <highlight>[Open shop]

```

- **Break the lines yourself**, each well under 50 characters (LIMITS.md). A line the client wraps loses its indent.
- **Every button has a hover** that says what the click does, and the panel's hover carries its limit when it has one (Hover and click).
- **A panel is answered once.** Each button runs a command with the request's id, and a later click says what happened instead of acting twice: `<warning>You already joined this party.`, `<warning>This invite has expired.` An expired panel says nothing until it is clicked.
- **A panel for this player plays one quiet sound** when it arrives (SOUNDS.md); one the player caused (a confirmation) does not.
- **NPC dialogue is an alternative to a menu, never an addition**: an NPC that opens a menu says nothing in chat, and an NPC that speaks offers its menu as a button.

## Command feedback

- Every typed command answers, every time. Silence reads as "it did not work".
- Long-running work answers twice: when accepted and when done or failed.
- Errors never leak stack traces or error codes to players. Log them; say "Something went wrong. Try again in a moment."
- Invalid arguments name the argument and the accepted form: `Alex is not online.`, `Use /join <game>.`
- Tab completion is part of the design. Every argument with a fixed set of values completes them.

## Feedback from the world and menus

Refusals from clicking blocks, NPCs, or locked items ("You can't build here") are rate-limited to once every few seconds per message, so spam-clicking does not flood chat. Prefer showing them where the player is looking: the action bar for world clicks, the tooltip for menu items (MENUS.md).

## Player chat

`DESIGN.md` defines how a rank looks in chat, and the same format appears in the tab list and on name tags:

- **Tag**: plain (`VIP`), small caps (`ᴠɪᴘ`), in brackets (`[VIP]`), a badge with a resource pack, or no tag and only the name color.
- **What carries the rank color**: the tag only, the tag and the name, or the tag, the name, and the separator.
- **Separator**: a colon straight after the name, `»`, or another symbol, in the rank color or in separator.
- **Message text**: a light tint of the rank color, highlight, or muted.
- **Players without a rank**: the name alone in a neutral gray, or a gray default tag (`PLAYER`) so every line looks the same.

Without a `DESIGN.md` decision, use this:

```
<rank_color>VIP <player>: <rank_tint><message>
```

- The rank tag, the player's name, and the colon share the rank color, so the rank reads as one unit. Players without a rank show only the name in a neutral gray.
- The message text (`<rank_tint>`) is a light tint of the rank color (OKLCH lightness about 0.91, chroma about 0.05). It stays easy to read and ties the line together; tints much paler than that look white in game.

Whatever the format:

- A rank has one color wherever it appears.
- Message text is never the full rank color: a paragraph in a saturated hue is tiring.
- Insert player-typed text as an unparsed placeholder (`Placeholder.unparsed`), so players cannot inject tags or click events.
- Mentions: highlight the mentioned name for the mentioned player only, with a quiet sound (SOUNDS.md).
- Channels (party, team, staff) get one short marker before the tag, such as `Party` in the social color, so a party message never looks like public chat.
- Private messages show direction and both names: `<muted>You <separator>→ <highlight><player><separator>: <highlight><message>`. Click on the line to reply (`suggest_command /msg <player> `).

## Hover and click

- Put the identity in the line and the details in the hover: the game name in the line; players, map, and state in the hover.
- **A hover on a message adds what the line has no room for, and only when the player needs it.** Most messages have none. The common case is a limit: a request or confirmation that expires says so (`Valid for 5 minutes`). Write a duration that stays true, not `Expires in 5 minutes`, which is wrong a minute later; use a clock time only when the network knows the player's time zone.
- **Clickable words inside a sentence** look the same in every message, in the form `DESIGN.md` picks: in brackets or without, in highlight or in a tint of their own. Without a decision, use highlight in brackets: `<muted>Open the <highlight>[server selector]<muted> to play.` Whatever the form, stay close to the line's colors: a yellow word in a blue line looks like a mistake, and complementary colors (purple and yellow) clash. Every clickable word has a hover that says what the click does.
- `run_command` only for safe actions (paging, joining) and panel buttons. Other destructive actions use `suggest_command`, so the player confirms with Enter.
- `copy_to_clipboard` for IDs and codes, with "Click to copy" in the hover.

## Confirmation

Destructive actions ask once, name the consequence, and expire. An action started in a menu confirms in a menu (MENUS.md); one started in chat or by a command confirms with a short panel, whose hover says how long it is valid.

```
<warning>This disbands your party for everyone in it.
<error>[Disband]  <highlight>[Keep]
```
