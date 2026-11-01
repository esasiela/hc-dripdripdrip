#!/usr/bin/env python3
"""Generates the test maps under assets/maps. Run from the repo root."""
import json
import math
import os

OUT = os.path.join(os.path.dirname(__file__), "..", "assets", "maps")


def write(fname, name, w, h, fn):
    land = [" ".join(str(fn(x, y, w, h)) for x in range(w)) for y in range(h)]
    data = {
        "version": 1,
        "meta": {"name": name, "tileset": "roygbiv"},
        "size": {"width": w, "height": h},
        "land": land,
        "sources": [],
        "drains": [],
        "obstacles": [],
    }
    with open(os.path.join(OUT, fname + ".json"), "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def flat(x, y, w, h):
    return 3


def stairs(x, y, w, h):
    return 1 + min(6, x * 7 // w)


def bowl(x, y, w, h):
    d = max(abs(x - (w - 1) / 2) / (w / 2), abs(y - (h - 1) / 2) / (h / 2))
    return max(1, min(7, 1 + int(d * 7)))


def void_island(x, y, w, h):
    cx, cy = (w - 1) / 2, (h - 1) / 2
    d = math.hypot(x - cx, y - cy)
    if d > 9:
        return 0
    if (x, y) in ((cx - 3, cy), (cx + 3, cy + 2)) or (abs(x - cx) < 1.5 and abs(y - cy + 4) < 1):
        return 0
    return 1 + int(d * 6 / 9)


def large(x, y, w, h):
    v = math.sin(x / 9.0) + math.cos(y / 7.0) + math.sin((x + y) / 13.0)
    return max(1, min(7, int((v + 3) / 6 * 7) + 1))


write("flat", "Flat", 16, 16, flat)
write("stairs", "Stairs", 24, 12, stairs)
write("bowl", "Bowl", 20, 14, bowl)
write("void-island", "Void Island", 24, 24, void_island)
write("large", "Large 128x128", 128, 128, large)
