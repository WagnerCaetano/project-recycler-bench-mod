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


STEEL = (58, 64, 72)
STEEL_DARK = (32, 36, 42)
STEEL_MID = (86, 94, 104)
STEEL_LIGHT = (168, 176, 184)
YELLOW = (232, 176, 28)
BLACK = (22, 24, 26)
GREEN = (42, 168, 64)
GREEN_DARK = (16, 92, 42)
BIN = (10, 16, 14)
GLOW = (28, 110, 58)
GLOW_HOT = (126, 236, 92)


def steel_pixel(x, y, seed=0):
    n = ((x * 17 + y * 31 + seed) % 7) - 3
    base = STEEL
    if (x + y) % 8 == 0:
        base = STEEL_MID
    if x in (0, 15) or y in (0, 15):
        base = STEEL_DARK
    return tuple(clamp(c + n) for c in base) + (255,)


def hazard_pixel(x, y):
    stripe = (x + y) // 2
    color = YELLOW if stripe % 2 == 0 else BLACK
    if y % 2 == 0 and color == YELLOW:
        color = mix(color, (255, 220, 90), 0.35)
    return color + (255,)


def rivet(x, y, cx, cy):
    return abs(x - cx) + abs(y - cy) <= 1 and (x != cx or y != cy or True)


def draw_logo(canvas, lit=False):
    green = GLOW_HOT if lit else GREEN
    dark = (20, 120, 48) if lit else GREEN_DARK
    cx, cy = 7.5, 8.2
    for y in range(16):
        for x in range(16):
            dx = x - cx
            dy = (y - cy) * 0.96
            radius = math.hypot(dx, dy)
            angle = (math.degrees(math.atan2(dy, dx)) + 360) % 360
            arc = angle % 120
            on_ring = 3.15 <= radius <= 5.15 and not (96 <= arc <= 118)
            # Arrow head at the end of each 120-degree arc.
            head = False
            for base in (20, 140, 260):
                tip = math.radians(base)
                tx = cx + math.cos(tip) * 5.3
                ty = cy + math.sin(tip) * 5.1
                if abs(x - tx) <= 1.2 and abs(y - ty) <= 1.2 and radius >= 3.4:
                    head = True
            if on_ring or head:
                shade = dark if radius < 3.7 else green
                canvas[y][x] = shade + (255,)


def front_pixels(lit=False):
    rows = [[steel_pixel(x, y, 2) for x in range(16)] for y in range(16)]
    for y in range(2):
        for x in range(16):
            rows[y][x] = hazard_pixel(x, y)
    for y in range(13, 16):
        for x in range(16):
            rows[y][x] = (18, 20, 22, 255) if y < 15 else hazard_pixel(x, y)
    # Intake slot and saw teeth.
    for y in range(11, 14):
        for x in range(2, 14):
            rows[y][x] = (14, 16, 18, 255)
    for x in range(2, 14):
        tooth = YELLOW if lit and x % 2 == 0 else (190, 198, 206)
        if x % 2 == 0:
            rows[12][x] = tooth + (255,)
        else:
            rows[11][x] = (46, 50, 56, 255)
    # Panel inset behind the logo.
    for y in range(3, 11):
        for x in range(3, 13):
            rows[y][x] = steel_pixel(x, y, 5)
            if x in (3, 12) or y in (3, 10):
                rows[y][x] = STEEL_DARK + (255,)
    draw_logo(rows, lit)
    # Corner bolts.
    for cx, cy in ((1, 3), (14, 3), (1, 10), (14, 10)):
        rows[cy][cx] = STEEL_LIGHT + (255,)
    return rows


def side_pixels(lit=False):
    rows = [[steel_pixel(x, y, 4) for x in range(16)] for y in range(16)]
    for y in range(16):
        rows[y][0] = STEEL_LIGHT + (255,)
        rows[y][15] = STEEL_DARK + (255,)
    for y in range(14, 16):
        for x in range(16):
            rows[y][x] = hazard_pixel(x, y)
    # Vertical vents.
    for vent_x in (4, 7, 10):
        for y in range(3, 12):
            color = GLOW_HOT if lit and y % 2 == 0 else (20, 24, 28)
            rows[y][vent_x] = color + (255,)
            rows[y][vent_x + 1] = STEEL_DARK + (255,)
    for cx, cy in ((2, 2), (13, 2), (2, 12), (13, 12)):
        rows[cy][cx] = STEEL_LIGHT + (255,)
    return rows


def inside_pixels(lit=False):
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            dx = x - 7.5
            dy = y - 7.5
            dist = math.hypot(dx, dy)
            if lit:
                color = mix(BIN, GLOW_HOT, max(0.0, 1.0 - dist / 8.0))
                if dist < 2.2:
                    color = (210, 255, 170)
            else:
                color = mix((6, 10, 8), GLOW, max(0.0, 1.0 - dist / 9.0))
            if (x + y) % 5 == 0 and dist > 3:
                color = mix(color, STEEL_DARK, 0.35)
            row.append(tuple(color) + (255,))
        rows.append(row)
    return rows


def bottom_pixels():
    rows = [[steel_pixel(x, y, 9) for x in range(16)] for y in range(16)]
    for y in range(16):
        for x in range(16):
            rows[y][x] = tuple(clamp(c - 18) for c in rows[y][x][:3]) + (255,)
    for cx, cy in ((2, 2), (13, 2), (2, 13), (13, 13)):
        rows[cy][cx] = STEEL_MID + (255,)
        if cy + 1 < 16:
            rows[cy + 1][cx] = STEEL_DARK + (255,)
    return rows


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
    blit_rows(BLOCK / "inside.png", inside_pixels(False))
    blit_rows(BLOCK / "inside_lit.png", inside_pixels(True))
    blit_rows(BLOCK / "bottom.png", bottom_pixels())
    write_png(BLOCK / "hazard.png", 16, 16, lambda x, y: hazard_pixel(x, y))
    write_png(GUI / "recycler_bench.png", 256, 256, gui_pixels)
    write_wav(SOUND / "chainsaw.wav")
    print("wrote textures and wav")


if __name__ == "__main__":
    main()
