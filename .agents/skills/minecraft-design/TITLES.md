# Titles, action bar, boss bar, and toasts

These surfaces sit in the middle of the player's view. Give each one a single job and keep it.

## Which one

| Surface | Job | Not for |
| --- | --- | --- |
| Title and subtitle | The few big moments of a game: a round starts, a bed is destroyed, a run is finished, the match is won | Welcomes, lobby events, command feedback, errors, anything routine |
| Action bar | Live personal state while doing something: a timer, active effects, spectating, a world-click refusal | Anything the player must read later; it fades after about 3 seconds |
| Boss bar | Status over time: a countdown, a queue, the round timer, the current objective, where the player is | A banner nobody reads after the first time |
| Toast | An unlock the player will care about: a new cosmetic, a completed quest | Errors, anything that needs an action |

If the player needs to act on the information or read it again, it belongs in chat or a menu.

## Title

A title takes over the screen. Use it only when the moment deserves the whole screen; most games need three or four, and a lobby needs almost none. If titles fire every minute, none of them feel big.

- Title: one to three words, under 16 characters, in one saturated color: the meaning (`Victory!` in success) or the game's accent for a game or round name, not the brand by default. `Victory!`, `Bed destroyed`, `Round 2`.
- Subtitle: the value or the context, under 35 characters, in highlight. Changes in success or error: `2:04.310 (-1.2s)`. `1st place, 12 kills`, `Red team vs. Blue team`.
- Timing: fade in 10 ticks, stay 40 to 70 ticks, fade out 20 ticks.
- One title per moment. If two big things happen within a few seconds (your bed falls, then you die), show the more important one; the other goes in chat.

Good moments for a title:

| Game | Moment | Title | Subtitle |
| --- | --- | --- | --- |
| Bedwars | your bed is destroyed | `Bed destroyed` | `You will no longer respawn` |
| A round-based game | a round starts | `Round 2` | `Blue team vs. Red team` |
| Jump and run | a module is finished | `Checkpoint!` | `0:42.310 (-1.2s)` |
| Any game | the match ends | `Victory!` | `1st place` |

### Countdowns

A start countdown is the one place titles repeat. Show the number as the title, one per second, from 5 (or 3) down, with no fades between numbers, then a final `Go!`. Pair each number with a rising tick sound (SOUNDS.md).

## Action bar

- Under 40 characters, one line.
- It fades after about 3 seconds. To keep a live value visible, resend it every second and stop as soon as the state ends.
- Only one thing can own the action bar at a time. Do not use it for state another system (combat, cooldowns) shows at the same moment.

```
<muted>Timer <highlight>0:42
<muted>Spectating <highlight><player>
```

## Boss bar

The text says what the bar measures, and the bar moves.

| Use | Text | Progress |
| --- | --- | --- |
| Countdown (game start, restart) | `<muted>Starting in <highlight>0:30` | drains to empty |
| Queue | `<muted>Looking for players <highlight>3<separator>/<highlight>12` | fills with players |
| Round timer | `<muted>Time left <highlight>14:02` | drains with the round |
| Objective | `<highlight>Find the hidden relic` | progress, or full if none |

- Without a resource pack, use one bar per player. A countdown replaces an announcement while it runs, and the announcement comes back afterwards.
- Remove the bar as soon as the process ends.
- Rotating announcements change at most every 20 seconds.

With a resource pack, the bar texture is hidden and the boss bar becomes the top HUD: several bars stacked or side by side, each section on its own background shape. See RESOURCE-PACK.md.

## Toasts

Advancement toasts (the box in the top right) are the right place for unlocks. Paper can show one with a temporary advancement that is granted and removed right away.

- Title: what happened, short. `Quest complete!`, `New cosmetic`.
- Description: the name of the thing, one line.
- An item icon that matches.

Toasts cannot be clicked, so never use them for errors or anything that needs an action.

## Sound

Pair a title with a sound only for real moments (match start, win, level up). See SOUNDS.md.
