# Server list

The server list entry is the network's storefront: a 64×64 icon, two MOTD lines, a player count, and a hover. Players compare it with every other server they have saved, so restraint stands out.

## Anatomy

| Part | What to show |
| --- | --- |
| Icon | The network logo, 64×64 PNG, readable at 32×32. A gray version for maintenance. |
| MOTD line 1 | The network's identity: name or one-line description, brand color. |
| MOTD line 2 | Live or seasonal state: current event, new mode, maintenance reason. Muted, with highlight values. Empty is fine. |
| Player count | Real numbers. Never fake the online or maximum count. |
| Player hover | The player sample list. Use it for a few lines of info (address, website), not for ads. It is plain legacy text on every client: write it with the 16 vanilla colors, hex never reaches it. |
| Version text | Replaces the ping bars when the version does not match. Use it for maintenance (`<error>Maintenance`) or the supported version range (`1.20–1.21`). |

## MOTD

- Two lines, each under 270px (about 45 characters).
- Center through a helper that measures glyph widths (for example a custom `<center>` tag), never by counting spaces.
- One color per line besides the neutrals (muted, separator, highlight), two at most per MOTD. No gradients, no symbols as decoration, no "NEW!!" or "BEST SERVER".
- Line 2 changes; line 1 does not. Players recognize the network by line 1.

```
line1: <center><brand>Example Network
line2: <center><muted>New mode<separator>: <highlight>Bedwars
```

## Maintenance

Change three things together, so maintenance is obvious at a glance:

- Line 1 switches to muted.
- The icon switches to the gray version.
- The version text shows `<error>Maintenance`, which replaces the ping bars and marks the entry red.

Line 2 says when it ends, if known: `<muted>Back at <highlight>18:00 CET`.

## Compatibility

- The proxy sends one MOTD to every client version. Hex colors fall back to the 16 vanilla colors on old clients (see COLORS.md), so the fallback must still read correctly.
- Bedrock players do not see the Java server list; they see the Geyser MOTD configured separately.

## Kick screens

Kick and disconnect screens are full-screen, centered, and the last thing a player sees. Three parts, separated by blank lines:

```
<brand>Example Network

<error>The network is under maintenance.

<muted>We are back at <highlight>18:00 CET<muted>.
<muted>Updates: <interactive>status.example.net
```

1. The network name, so the player knows who kicked them.
2. What happened, in error (or warning for temporary states such as a full lobby).
3. What to do next, in muted, with the one value that matters in highlight.

State facts that are true for the screen's trigger: a "full" kick says which server is full, not the network maximum.

