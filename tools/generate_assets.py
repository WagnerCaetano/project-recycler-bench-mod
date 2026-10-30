"""Generate recycler bench textures and a short chainsaw sample."""

import math
import random
import struct
import wave
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TEX = ROOT / "src" / "main" / "resources" / "assets" / "recyclerbench" / "textures"
BLOCK = TEX / "block"
GUI = TEX / "gui"
SOUND = ROOT / "src" / "main" / "resources" / "assets" / "recyclerbench" / "sounds"


def write_png(path: Path, width: int, height: int, pixels) -> None:
    raw = bytearray()
    for y in range(height):
        raw.append(0)
        for x in range(width):
            raw.extend(pixels(x, y))
    def chunk(tag: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def clamp(value: int) -> int:
    return max(0, min(255, value))


def mix(a, b, t: float):
    return tuple(clamp(int(a[i] + (b[i] - a[i]) * t)) for i in range(3))


# Vanilla stone, copper, and iron. Colors stay close to the vanilla textures.
STONE = ((122, 122, 122), (136, 136, 136), (108, 108, 108), (148, 148, 148), (96, 96, 96))
MORTAR = (58, 58, 58)
COPPER = (184, 102, 70)
COPPER_LIGHT = (214, 138, 98)
COPPER_DARK = (132, 68, 46)
IRON = (198, 198, 198)
IRON_LIGHT = (232, 232, 232)
IRON_DARK = (146, 146, 146)
DARK = (42, 44, 48)
DARK_LIGHT = (68, 70, 74)
PIT = (28, 30, 32)


def px(color):
    return color + (255,)


def stone_pixel(x, y):
    row = y // 4
    local_x = (x + (4 if row % 2 else 0)) % 8
    local_y = y % 4
    if local_x == 0 or local_y == 0:
        return MORTAR
    return STONE[(x * 3 + y * 5 + local_x) % len(STONE)]


def copper_pixel(x, y):
    if x == 0 or y == 0:
        return COPPER_LIGHT
    if x == 15 or y == 15:
        return COPPER_DARK
    if (x + y * 2) % 5 == 0:
        return COPPER_DARK
    if (x * 2 + y) % 4 == 0:
        return COPPER_LIGHT
    return COPPER


def iron_pixel(x, y, lit=False):
    if lit and (y < 2 or (x + y) % 6 == 0):
        return (232, 164, 96)
    if y % 4 == 0:
        return IRON_LIGHT
    if y % 4 == 3:
        return IRON_DARK
    if (x + y) % 5 == 0:
        return IRON_DARK
    return IRON


def paint_map(lines, lit=False):
    rows = []
    for y, line in enumerate(lines):
        row = []
        for x, mark in enumerate(line):
            if mark == "S":
                color = stone_pixel(x, y)
            elif mark == "s":
                color = tuple(clamp(c - 26) for c in stone_pixel(x, y))
            elif mark == "C":
                color = copper_pixel(x, y)
            elif mark == "c":
                color = COPPER_DARK
            elif mark == "I":
                color = (214, 146, 86) if lit and (x + y) % 5 == 0 else iron_pixel(x, y, False)
            elif mark == "L":
                color = (236, 186, 120) if lit else IRON_LIGHT
            elif mark == "i":
                color = IRON_DARK
            elif mark == ".":
                color = PIT
            else:
                color = DARK
            row.append(px(color))
        rows.append(row)
    return rows


def front_pixels(lit=False):
    # Stone body, a one-pixel copper frame, and a blank iron plate.
    return paint_map([
        "ssssssssssssssss",
        "SSSSSSSSSSSSSSSS",
        "SSSSSSSSSSSSSSSS",
        "SSSccccccccccSSS",
        "SSScIIIIIIIIcSSS",
        "SSScILIIIIILcSSS",
        "SSScIIIIIIIIcSSS",
        "SSScIIIIIIIIcSSS",
        "SSScILIIIIILcSSS",
        "SSScIIIIIIIIcSSS",
        "SSScIIIIIIIIcSSS",
        "SSScIIIIIIIIcSSS",
        "SSSccccccccccSSS",
        "SSSSSSSSSSSSSSSS",
        "SSSSSSSSSSSSSSSS",
        "ssssssssssssssss",
    ], lit)


def side_pixels(lit=False):
    # Stone bricks, a copper cap, and an iron band.
    lines = [
        "CCCCCCCCCCCCCCCC",
        "SSSSSSSSSSSSSSSS",
        "SSSSSSSSSSSSSSSS",
        "SSSSSSSSSSSSSSSS",
        "SSsSSSSSSSsSSSSS",
        "SSSSSSSSSSSSSSSS",
        "IIIIIIIIIIIIIIII",
        "ILIIIIIIIIIIILII",
        "IIIIIIIIIIIIIIII",
        "SSSSSSSSSSSSSSSS",
        "SSsSSSSSSSsSSSSS",
        "SSSSSSSSSSSSSSSS",
        "SSSSSSSSSSSSSSSS",
        "SSSSSSSSSSSSSSSS",
        "SSSSSSSSSSSSSSSS",
        "ssssssssssssssss",
    ]
    rows = paint_map(lines, lit)
    for x in (2, 13):
        rows[3][x] = px(COPPER_LIGHT)
        rows[3][x + 1] = px(COPPER_DARK)
        rows[12][x] = px(COPPER_LIGHT)
        rows[12][x + 1] = px(COPPER_DARK)
    return rows


def bottom_pixels():
    return [[px(tuple(clamp(c - 28) for c in stone_pixel(x, y))) for x in range(16)] for y in range(16)]


def trim_pixels():
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            if (x % 5 == 1 and y % 5 == 1):
                color = IRON_LIGHT
            elif x % 5 == 2 and y % 5 == 2:
                color = IRON_DARK
            else:
                color = copper_pixel(x, y)
            row.append(px(color))
        rows.append(row)
    return rows


def dark_pixels():
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            color = DARK_LIGHT if (x * 3 + y * 5) % 7 == 0 else DARK if (x + y) % 3 else PIT
            row.append(px(color))
        rows.append(row)
    return rows


def shredder_pixels(lit=False):
    # Round blade inside a copper ring. The model shows pixels 3..12.
    tooth = "L" if lit else "I"
    hub = "L" if lit else "i"
    ring = [
        f"...cccc...",
        f"..c....c..",
        f".c..{tooth}{tooth}..c.",
        f"c..{tooth}..{tooth}..c",
        f"c.{tooth}.{hub}{hub}.{tooth}.c",
        f"c.{tooth}.{hub}{hub}.{tooth}.c",
        f"c..{tooth}..{tooth}..c",
        f".c..{tooth}{tooth}..c.",
        f"..c....c..",
        f"...cccc...",
    ]
    for line in ring:
        if len(line) != 10:
            raise ValueError(f"shredder row {line!r} is {len(line)} wide")
    padded = ["." * 16] * 3 + ["..." + line + "..." for line in ring] + ["." * 16] * 3
    return paint_map(padded, lit)


def blit_rows(path: Path, rows) -> None:
    height = len(rows)
    width = len(rows[0])
    write_png(path, width, height, lambda x, y: rows[y][x])


def gui_pixels(x, y):
    if x >= 176 and y < 16 and x < 200:
        # Progress arrow, left to right, head in the last 6 pixels.
        local_x = x - 176
        if 4 <= y <= 11 and local_x < 16:
            return (46, 170, 62, 255)
        if 2 <= y <= 13 and 16 <= local_x <= 22 and abs(y - 7.5) <= (22 - local_x):
            return (46, 170, 62, 255)
        return (0, 0, 0, 0)

    if x >= 176 or y >= 180:
        return (0, 0, 0, 0)

    # Outer metal panel.
    if x < 2 or y < 2 or x >= 174 or y >= 178:
        return (48, 54, 50, 255)
    if x < 4 or y < 4 or x >= 172 or y >= 176:
        return (198, 206, 196, 255)
    color = (168, 176, 166, 255)
    # Divider above the player inventory.
    if 88 <= y <= 90:
        color = (92, 100, 92, 255) if y == 89 else (120, 128, 118, 255)

    def slot(sx, sy):
        return sx - 1 <= x <= sx + 16 and sy - 1 <= y <= sy + 16

    slots = [(20, 35)]
    for row in range(3):
        for col in range(3):
            slots.append((98 + col * 18, 17 + row * 18))
    for row in range(3):
        for col in range(9):
            slots.append((8 + col * 18, 98 + row * 18))
    for col in range(9):
        slots.append((8 + col * 18, 156))

    for sx, sy in slots:
        if slot(sx, sy):
            on_edge_x = x in (sx - 1, sx + 16)
            on_edge_y = y in (sy - 1, sy + 16)
            if x == sx - 1 or y == sy - 1:
                return (55, 55, 55, 255)
            if x == sx + 16 or y == sy + 16:
                return (255, 255, 255, 255)
            if on_edge_x or on_edge_y:
                return (139, 139, 139, 255)
            return (122, 126, 120, 255)

    # Empty arrow track.
    if 48 <= x <= 71 and 35 <= y <= 50:
        local_x = x - 48
        if 4 <= (y - 35) <= 11 and local_x < 16:
            return (70, 78, 72, 255)
        if 2 <= (y - 35) <= 13 and 16 <= local_x <= 22 and abs((y - 35) - 7.5) <= (22 - local_x):
            return (70, 78, 72, 255)
    return color


def write_wav(path: Path) -> None:
    sample_rate = 22050
    duration = 0.46
    count = int(sample_rate * duration)
    rng = random.Random(11)
    noise = 0.0
    frames = bytearray()
    for i in range(count):
        t = i / sample_rate
        noise = noise * 0.72 + rng.uniform(-1, 1) * 0.28
        engine = math.sin(2 * math.pi * 86 * t)
        engine += 0.55 * (1.0 if math.sin(2 * math.pi * 172 * t) > 0 else -1.0)
        rattle = 0.62 + 0.38 * math.sin(2 * math.pi * 54 * t)
        buzz = 0.45 + 0.55 * abs(math.sin(2 * math.pi * 41 * t))
        sample = (engine * 0.34 + noise * 0.78) * rattle * buzz
        fade = min(1.0, i / (sample_rate * 0.012), (count - i) / (sample_rate * 0.02))
        value = max(-1.0, min(1.0, sample * fade * 0.9))
        frames += struct.pack("<h", int(value * 32767))
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "wb") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(sample_rate)
        handle.writeframes(frames)


def main() -> None:
    blit_rows(BLOCK / "front.png", front_pixels(False))
    blit_rows(BLOCK / "front_lit.png", front_pixels(True))
    blit_rows(BLOCK / "side.png", side_pixels(False))
    blit_rows(BLOCK / "side_lit.png", side_pixels(True))
    blit_rows(BLOCK / "bottom.png", bottom_pixels())
    blit_rows(BLOCK / "trim.png", trim_pixels())
    blit_rows(BLOCK / "dark.png", dark_pixels())
    blit_rows(BLOCK / "iron.png", [[px(iron_pixel(x, y, False)) for x in range(16)] for y in range(16)])
    blit_rows(BLOCK / "iron_lit.png", [[px(iron_pixel(x, y, True)) for x in range(16)] for y in range(16)])
    blit_rows(BLOCK / "shredder.png", shredder_pixels(False))
    blit_rows(BLOCK / "shredder_lit.png", shredder_pixels(True))
    write_png(GUI / "recycler_bench.png", 256, 256, gui_pixels)
    write_wav(SOUND / "chainsaw.wav")
    print("wrote textures and wav")


if __name__ == "__main__":
    main()
