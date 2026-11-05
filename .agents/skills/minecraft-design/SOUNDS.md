# Sounds and particles

Sound is feedback, not decoration. A sound confirms that something the player did had an effect, or marks a moment. Players hear it in their headphones, in every server, for hours. Use it sparingly.

## When to play a sound

| Event | Sound | Pitch |
| --- | --- | --- |
| Menu click that changes something | `ui.button.click` | 1.0 |
| Menu open | none; the inventory opening is feedback enough | |
| Action succeeded (purchase, joined queue) | `entity.experience_orb.pickup` | 1.0 to 1.5 |
| Action refused (no permission, locked item, full server) | `block.note_block.bass` | 0.5 to 0.8 |
| Countdown tick | `block.note_block.hat` or `block.note_block.pling` | rising per tick |
| Moment (match start, level up) | `entity.player.levelup` or `ui.toast.challenge_complete` | 1.0 |
| Teleport or server switch | `entity.enderman.teleport` | 1.0, quiet |

## Rules

- Typed commands are silent. Sounds belong to the world and to menus: clicks on items, blocks, and NPCs.
- One sound per action. Do not layer two sounds to make it "richer".
- Keep volume at or below 1.0 and play it to the player only, at their location. Never broadcast UI sounds to nearby players.
- Use the master or player source, so players who mute ambient or music still hear UI feedback.
- Pitch carries meaning: higher for success and progress, lower for refusal. Keep this consistent across all plugins.
- Refusal sounds from world and menu clicks are rate-limited together with their message (CHAT.md). A player spam-clicking a locked item must not get ten bass notes.

## Particles

- Use particles only to point at something in the world: the NPC that was clicked, the portal that is active.
- Short bursts (under one second) on events, never constant ambient particles around NPCs or holograms. Constant particles cost client performance and hide the thing they decorate.
- Prefer subtle particles (`happy_villager`, `end_rod`, `wax_on`) over loud ones (`flame`, `explosion`, `totem_of_undying`).
