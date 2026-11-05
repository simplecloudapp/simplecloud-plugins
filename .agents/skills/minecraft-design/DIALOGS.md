# Dialogs

Dialogs (Minecraft 1.21.6+ clients; Paper's `io.papermc.paper.dialog` API since 1.21.7) are forms drawn by the client: a title, text, inputs, and buttons. Use them only where the player has to type: a name to submit or search for, a report reason, an amount.

## When to use one

| Task | Use |
| --- | --- |
| Type a name, a search, a reason, or an amount | Dialog with an input |
| Confirm an action | A chat panel (CHAT.md) or a menu (MENUS.md) |
| Change settings, pick one of several options | Menu |

Never use a sign or an anvil for text input when the client supports dialogs.

## Text

- **Title**: one plain noun or short phrase in Title case, like a menu title, in highlight or the vanilla white. `Search Players`, `Rename Pet`.
- **Body**: at most one short line that says what to type, in muted, with highlight for the one value that matters. Leave it out when the input's label says enough.
- **Labels on buttons and inputs** stay uncolored. The client draws them on its own gray widgets, and colored labels look broken there.
- **Inputs** start with the current value when there is one (the pet's current name), and set a maximum length that fits where the value appears (LIMITS.md).

## Buttons

- Two buttons: the submit verb (`Search`, `Rename`, `Report`) and `Cancel`. Never `OK` or `Submit`: the label says what happens.
- `Cancel` is the second button, and it is the one Escape triggers.
- Keep the default width (150).

## Behavior

- `canCloseWithEscape` true, except while waiting for a server answer.
- `afterAction`: `CLOSE` when the submit finishes the task, `WAIT_FOR_RESPONSE` when the server answers with another screen (search results in a menu).
- Answer every submit: the next screen (the menu with the results), or one chat line in the chat style (CHAT.md). Never close silently.
- Validate input on the server and reopen the dialog with the entered values and the error in error color as a body line. Do not lose what the player typed.
- `pause` false on multiplayer servers.
