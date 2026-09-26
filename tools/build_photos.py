"""Crop, grade and scrim the chosen Commons photographs into tile art.

Each tile gets a different dominant tone. On a real Fire TV that variation is
most of how somebody finds the one they want without reading, and six frames
of the same green is one texture. The grade is gentle -- a lean, not a
duotone -- so they still read as photographs.

The label scrim is baked into the bottom of each image as an alpha ramp
rather than the whole frame being dimmed, so the picture keeps its own
contrast and the white label still clears 7:1 over it.
"""
from PIL import Image, ImageEnhance
import os, json

# SRC is a scratch directory of downloaded Commons originals, one per TILES
# entry below; it is not part of this repo and is recreated by hand from the
# sources in ATTRIBUTION.md before this script is ever run again. RES is the
# app's own resource tree, relative to this file's location in tools/.
SRC = "./source-photos"
RES = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")
PLINTH = (20, 18, 15)

# slug -> (source, crop-anchor 0..1 vertical, saturation, brightness, lean rgb, lean strength)
TILES = {
    "tell_me":       ("tell_me/01.jpg",   0.45, 0.80, 0.66, (168,  92,  78), 0.16),
    # Was a radio-museum piece (not even a television) -- an internal design
    # review named this directly as too generic. Now a rooftop TV aerial
    # against a night sky, top-anchored so the antenna's own silhouette (the
    # only content in the frame) survives the crop.
    "live_tv":       ("live_tv_v2/01.jpg", 0.0, 0.85, 0.62, (196, 150,  70), 0.16),
    "her_shows":     ("her_shows/05.jpg", 0.50, 0.72, 0.54, (150, 160,  96), 0.24),
    # Was a cinema auditorium interior -- the literal "a cinema for Films" an
    # internal design review named directly as too generic. Now a real
    # marquee's own neon reading CINEMAS, top-anchored: the real showtimes
    # board lower in the source frame falls inside this pipeline's own
    # label scrim (46-80% height) and is dark before it would ever read as
    # somebody else's listings.
    "films":         ("films_v2/01.jpg",  0.0, 0.85, 0.66, ( 92, 116, 180), 0.18),
    "box_sets":      ("box_sets/05.jpg",  0.50, 0.55, 0.42, (120, 142, 158), 0.24),
    "family_videos": ("family_videos/05.jpg", 0.5, 0.70, 0.54, (156, 120,  86), 0.26),
    "call_for_help": ("live_tv3/09.jpg",  0.50, 0.70, 0.60, (168, 120,  80), 0.18),
    "captions":      ("live_tv3/07.jpg",  0.50, 0.70, 0.60, (120, 150, 140), 0.18),
    "another_app":   ("films/00.jpg",     0.50, 0.70, 0.58, (140, 130, 150), 0.18),
}
# Kept for the record only; nothing generates from it any more. See the note
# where the hero used to be written, at the foot of this file.
HERO = ("hero/09.jpg", 0.52, 0.70, 0.44, (150, 128, 88), 0.18)


def crop169(im, anchor, ratio=16/9):
    w, h = im.size
    if w / h > ratio:
        nw = int(h * ratio); x = (w - nw) // 2
        return im.crop((x, 0, x + nw, h))
    nh = int(w / ratio); y = int((h - nh) * anchor)
    return im.crop((0, y, w, y + nh))


def grade(im, sat, bright, lean, strength):
    """Tone the frame without lifting its blacks.

    The first pass blended each frame toward a flat colour, which is what
    put a grey film over the whole wall: blending toward a mid tone raises
    the darkest pixels as much as it shifts the hue, and six washed-out
    photographs read worse than six flat greens did. Multiplying instead
    leaves black at black and only bends what already has light in it, so
    the tiles keep the depth that was the reason for using photographs.
    """
    im = ImageEnhance.Color(im).enhance(sat)
    im = ImageEnhance.Brightness(im).enhance(bright)
    avg = sum(lean) / 3.0
    lut = []
    for c in lean:
        k = 1.0 + (c / avg - 1.0) * strength * 2.4
        lut += [min(255, int(v * k)) for v in range(256)]
    im = im.point(lut)
    return ImageEnhance.Contrast(im).enhance(1.08)


def scrim(im, start=0.46, end=0.80, peak=0.94, target=PLINTH):
    """Alpha ramp to the plinth colour across the lower part of the frame."""
    w, h = im.size
    band = Image.new("RGB", (1, h), target)
    mask = Image.new("L", (1, h), 0)
    px = mask.load()
    for y in range(h):
        t = y / h
        if t <= start: a = 0.0
        elif t >= end: a = peak
        else:
            u = (t - start) / (end - start)
            a = peak * (u * u * (3 - 2 * u))      # smoothstep, no hard edge
        px[0, y] = int(a * 255)
    return Image.composite(band.resize((w, h)), im, mask.resize((w, h)))


def side_scrim(im, width=0.46, peak=0.8, target=PLINTH):
    """The same ramp turned on its side, for the hero's title corner.

    A photograph of a room is busiest where the furniture is, and the app's
    name landed on a lace tablecloth. Darkening the left of the frame gives
    the type a ground without dimming the picture, which is the same trade
    the bottom scrim makes.
    """
    w, h = im.size
    band = Image.new("RGB", (w, 1), target)
    mask = Image.new("L", (w, 1), 0)
    px = mask.load()
    for x in range(w):
        t = x / w
        if t >= width:
            a = 0.0
        else:
            u = 1.0 - t / width
            a = peak * (u * u * (3 - 2 * u))
        px[x, 0] = int(a * 255)
    return Image.composite(band.resize((w, h)), im, mask.resize((w, h)))


def write(im, name, widths):
    for folder, wpx in widths.items():
        out = os.path.join(RES, folder)
        os.makedirs(out, exist_ok=True)
        im.resize((wpx, int(wpx * im.size[1] / im.size[0])), Image.LANCZOS).save(
            os.path.join(out, name), "PNG", optimize=True)


TILE_W = {"drawable-hdpi": 480, "drawable-xhdpi": 640, "drawable-xxhdpi": 960}


def main() -> None:
    import sys
    # `--only slug,slug` regenerates a subset without requiring every other
    # tile's source photograph to be present in SRC -- useful when only one
    # or two tiles are being replaced and the rest of the cache has aged out.
    only = None
    if len(sys.argv) > 1 and sys.argv[1] == "--only":
        only = set(sys.argv[2].split(","))
    wanted = {k: v for k, v in TILES.items() if only is None or k in only}

    used = {}
    for slug, (src, anchor, sat, bright, lean, strength) in wanted.items():
        im = Image.open(os.path.join(SRC, src)).convert("RGB")
        im = scrim(grade(crop169(im, anchor), sat, bright, lean, strength))
        write(im, f"art_{slug}.png", TILE_W)
        used[slug] = src

    # The header photograph is not generated any more. It ran across the top of
    # the home screen and every inner screen at about a tenth of full
    # brightness, and an internal review of this batch's Fire TV apps found the same darkened
    # full-bleed band on all three of this repository's Fire TV home screens.
    # The band is a flat warm charcoal plane now, closed with the fern rule.
    # The HERO entry stays in the table so the pick is on the record; nothing
    # reads it.
    existing = {}
    used_path = os.path.join(SRC, "used.json")
    if os.path.exists(used_path):
        existing = json.load(open(used_path))
    existing.update(used)
    json.dump(existing, open(used_path, "w"), indent=1)
    print("\n".join(f"{k}: {v}" for k, v in used.items()))


if __name__ == "__main__":
    main()
