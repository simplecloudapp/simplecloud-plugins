#!/usr/bin/env python3
"""Check a Minecraft text palette against real backgrounds and legacy clients.

Usage:
  python3 check_palette.py brand=#B46FFF highlight=#E8D4FF muted=#A09DA3 error=#FD393F ...
  Name accents accent.<name>=#RRGGBB, for example accent.cyan=#00CEEB.

For every role it prints:
  - the vanilla color Paper sends when it has to downsample (Adventure's
    NamedTextColor.nearestTo, reproduced bit for bit, including its float math),
  - the WCAG contrast ratio on the backgrounds Minecraft text actually sits on,
  - a warning when a color that should carry meaning is pastel,
  - warnings when accents are too alike, or crowd the brand's hue.

Downsampling only reaches Java clients below 1.16 (ViaVersion), the server list
hover, and Bedrock players (Geyser maps to these 16 plus a few Bedrock-only
shades, so treat its result as close, not exact). Modern Java clients get the exact hex.
Treat legacy warnings as relevant only if the network supports those players.
Warnings are advice; the exit code is 0 either way.
"""
import struct
import sys

LEGACY = {
    "black": "000000", "dark_blue": "0000AA", "dark_green": "00AA00",
    "dark_aqua": "00AAAA", "dark_red": "AA0000", "dark_purple": "AA00AA",
    "gold": "FFAA00", "gray": "AAAAAA", "dark_gray": "555555",
    "blue": "5555FF", "green": "55FF55", "aqua": "55FFFF", "red": "FF5555",
    "light_purple": "FF55FF", "yellow": "FFFF55", "white": "FFFFFF",
}
STATES = ("success", "warning", "error")
COLORED = ("brand", "success", "warning", "error")
# Category colors that should form one family: same lightness and chroma, only the hue changes.
FAMILY = ("brand", "info", "success", "warning", "error", "social")
RED_FAMILY = ("red", "dark_red")


def f32(x):
    """Round to a 32-bit float, as Adventure computes in Java floats."""
    return struct.unpack("f", struct.pack("f", x))[0]


def parse_hex(value):
    h = value.lstrip("#")
    if len(h) != 6:
        raise ValueError(f"not a #RRGGBB color: {value}")
    return h.upper()


def rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def adventure_hsv(h):
    r, g, b = (f32(c / f32(255.0)) for c in rgb(h))
    lo, hi = min(r, g, b), max(r, g, b)
    delta = f32(hi - lo)
    s = f32(delta / hi) if hi != 0 else 0.0
    if s == 0:
        return 0.0, s, hi
    if r == hi:
        hue = f32(f32(g - b) / delta)
    elif g == hi:
        hue = f32(2 + f32(f32(b - r) / delta))
    else:
        hue = f32(4 + f32(f32(r - g) / delta))
    hue = f32(hue * 60)
    if hue < 0:
        hue = f32(hue + 360)
    return f32(hue / 360), s, hi


def adventure_distance(a, b):
    dh = f32(abs(f32(a[0] - b[0])))
    dh = f32(3 * min(dh, f32(1 - dh)))
    ds = f32(a[1] - b[1])
    dv = f32(a[2] - b[2])
    return f32(f32(f32(dh * dh) + f32(ds * ds)) + f32(dv * dv))


def nearest_adventure(h):
    """Port of Adventure's TextColor.nearestColorTo over NamedTextColor.VALUES."""
    target = adventure_hsv(h)
    best, best_distance = None, f32(3.4028235e38)
    for name, value in LEGACY.items():
        d = adventure_distance(target, adventure_hsv(value))
        if d < best_distance:
            best, best_distance = name, d
        if d == 0:
            break
    return best


def oklch(h):
    """OKLCH lightness (0-1), chroma, and hue in degrees."""
    import math
    def lin(c):
        c /= 255
        return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4
    r, g, b = (lin(c) for c in rgb(h))
    l = (0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b) ** (1 / 3)
    m = (0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b) ** (1 / 3)
    s = (0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b) ** (1 / 3)
    L = 0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s
    a = 1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s
    bb = 0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s
    return L, math.hypot(a, bb), math.degrees(math.atan2(bb, a)) % 360


def saturation(h):
    """HSV saturation, 0 for gray and 1 for a fully saturated color."""
    r, g, b = rgb(h)
    hi, lo = max(r, g, b), min(r, g, b)
    return 0.0 if hi == 0 else (hi - lo) / hi


def luminance(c):
    def channel(v):
        v /= 255
        return v / 12.92 if v <= 0.03928 else ((v + 0.055) / 1.055) ** 2.4
    r, g, b = map(channel, c)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def contrast(a, b):
    la, lb = luminance(a), luminance(b)
    return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)


def blend(fg, bg, alpha):
    return tuple(round(x * alpha + y * (1 - alpha)) for x, y in zip(fg, bg))


BACKGROUNDS = {
    "chat/night": blend((0, 0, 0), rgb("101018"), 0.5),
    "chat/day": blend((0, 0, 0), rgb("78A7FF"), 0.5),
    "tooltip": rgb("100010"),
    "inv title": rgb("C6C6C6"),
    "oak sign": rgb("B8945F"),
}


def main(args):
    if not args or args[0] in ("-h", "--help"):
        print(__doc__.strip())
        return 1
    roles = {}
    for arg in args:
        name, _, value = arg.partition("=")
        roles[name] = parse_hex(value)

    header = f"{'role':<12} {'hex':<8} {'L':>5} {'C':>5}  {'paper legacy':<14}" + "".join(f"{b:>11}" for b in BACKGROUNDS)
    print(header)
    print("-" * len(header))
    legacy = {}
    for name, h in roles.items():
        legacy[name] = nearest_adventure(h)
        cells = "".join(f"{contrast(rgb(h), bg):>11.1f}" for bg in BACKGROUNDS.values())
        L, C, _ = oklch(h)
        print(f"{name:<12} {h:<8} {L:>5.2f} {C:>5.2f}  {legacy[name]:<14}{cells}")

    print()
    print("Contrast is the WCAG ratio without Minecraft's drop shadow; the shadow makes")
    print("light text read better than the number suggests. Inventory titles and light")
    print("signs need dark text (or glowing sign text); everything else needs light text.")

    warnings = []
    if "separator" in roles and contrast(rgb(roles["separator"]), BACKGROUNDS["chat/day"]) < 1.02:
        warnings.append("separator vanishes over daylight chat; use #5C5C5C to #6A686E.")
    for name in ("muted", "highlight"):
        if name in roles and contrast(rgb(roles[name]), BACKGROUNDS["chat/day"]) < 2.0:
            warnings.append(f"{name} is hard to read over daylight chat.")
    for name in COLORED:
        if name in roles and oklch(roles[name])[1] < 0.12:
            warnings.append(f"{name} is pastel (OKLCH chroma {oklch(roles[name])[1]:.2f}); "
                            "colors that carry meaning need about 0.14 or more.")
    family = {n: oklch(roles[n]) for n in FAMILY if n in roles}
    if len(family) >= 3:
        lights = [v[0] for v in family.values()]
        chromas = [v[1] for v in family.values() if v[1] >= 0.05]
        if max(lights) - min(lights) > 0.14:
            warnings.append("category colors differ a lot in lightness ("
                            + ", ".join(f"{n} {v[0]:.2f}" for n, v in family.items())
                            + "); keep them equally strong: yellows, oranges, and greens around 0.80; purples, blues, pinks, and reds around 0.66 to 0.70.")
        if chromas and max(chromas) - min(chromas) > 0.07:
            warnings.append("category colors differ a lot in saturation ("
                            + ", ".join(f"{n} {v[1]:.2f}" for n, v in family.items())
                            + "); keep chroma within about 0.05 so they read as one family.")
    accents = {n: oklch(h) for n, h in roles.items() if n.startswith("accent.")}
    for name, (L, C, _) in accents.items():
        if C < 0.12:
            warnings.append(f"{name} is pastel (OKLCH chroma {C:.2f}); accents need about 0.14 or more.")
    names = list(accents)
    for i, a in enumerate(names):
        for b in names[i + 1:]:
            dh = min(abs(accents[a][2] - accents[b][2]), 360 - abs(accents[a][2] - accents[b][2]))
            if dh < 20:
                warnings.append(f"{a} and {b} are almost the same hue ({dh:.0f} degrees apart); "
                                "neighbors would look like one group.")
    for name, (L, C, h) in accents.items():
        for state in STATES:
            if state in roles:
                sl, sc, sh = oklch(roles[state])
                dh = min(abs(h - sh), 360 - abs(h - sh))
                if dh < 10 and abs(L - sl) < 0.05:
                    warnings.append(f"{name} is almost the {state} color; move it a step away, "
                                    f"so a label in it never reads as {state}.")
    if "brand" in roles:
        bh = oklch(roles["brand"])[2]
        for name, (_, C, h) in accents.items():
            dh = min(abs(h - bh), 360 - abs(h - bh))
            if dh < 20:
                warnings.append(f"{name} is close to the brand hue; leave the brand's hue out of the "
                                "accents so the brand stays a signature.")
    for name in ("muted", "separator"):
        if name in legacy and legacy[name] not in ("gray", "dark_gray", "white", "black"):
            warnings.append(f"legacy: {name} becomes {legacy[name]}; the gray is tinted too much "
                            "(keep OKLCH chroma around 0.01).")
    if "highlight" in roles and oklch(roles["highlight"])[1] < 0.04:
        warnings.append("highlight reads as white in game; use a clear tint of the brand "
                        "(lightness about 0.90, chroma about 0.065).")
    if "interactive" in roles and "brand" in roles:
        il, ic, ih = oklch(roles["interactive"])
        bl, bc, bh = oklch(roles["brand"])
        dh = min(abs(ih - bh), 360 - abs(ih - bh))
        if ic >= 0.04 and dh > 40:
            warnings.append(f"interactive (hue {ih:.0f}) is unrelated to brand (hue {bh:.0f}); "
                            "a light tint of the brand hue fits better than a new color.")

    seen = {}
    for state in STATES:
        if state in legacy:
            seen.setdefault(legacy[state], []).append(state)
    for group in seen.values():
        if len(group) > 1:
            warnings.append(f"legacy: {', '.join(group)} all become {legacy[group[0]]}.")
    if legacy.get("brand") in RED_FAMILY:
        warnings.append("legacy: brand becomes red and reads as an error.")
    if "interactive" in legacy and legacy.get("interactive") == legacy.get("warning"):
        warnings.append("legacy: interactive and warning become the same color.")

    if warnings:
        print()
        for w in warnings:
            print(f"WARNING: {w}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
