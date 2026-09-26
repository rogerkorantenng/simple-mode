"""Crop, grade and scrim the 17 Commons photographs for the app/channel list rows.

Copied from tools/build_photos.py, which this file does not import: its
module-level code regenerates the original ten tiles from a source directory
that is scratch space and may not exist in a given session, so importing it
can crash before this script's own table ever runs. The functions below are
identical to that file's -- see its docstrings for why the grade multiplies
rather than blends (keeps blacks black) and why the label scrim is baked in
as a bottom alpha ramp rather than dimming the whole frame. Only the source
directory and the TILES table are new here.

Each of these 17 tiles gets its own dominant tone, same principle as the
original ten: on a wall of 27 tiles total, colour is most of how someone
finds the one they want without reading a label, and colour variety only
works if the picks are spread around the wheel rather than clustered.
"""
from PIL import Image, ImageEnhance
import os, json

# SRC is a scratch directory of downloaded Commons originals, one per TILES
# entry below; it is not part of this repo and is recreated by hand from the
# sources in ATTRIBUTION.md before this script is ever run again. RES is the
# app's own resource tree, relative to this file's location in tools/.
SRC = "./source-photos-list"
RES = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")


def crop169(im, anchor, ratio=16 / 9):
    w, h = im.size
    if w / h > ratio:
        nw = int(h * ratio); x = (w - nw) // 2
        return im.crop((x, 0, x + nw, h))
    nh = int(w / ratio); y = int((h - nh) * anchor)
    return im.crop((0, y, w, y + nh))


def grade(im, sat, bright, lean, strength):
    """Tone the frame without lifting its blacks -- multiply, don't blend."""
    im = ImageEnhance.Color(im).enhance(sat)
    im = ImageEnhance.Brightness(im).enhance(bright)
    avg = sum(lean) / 3.0
    lut = []
    for c in lean:
        k = 1.0 + (c / avg - 1.0) * strength * 2.4
        lut += [min(255, int(v * k)) for v in range(256)]
    im = im.point(lut)
    return ImageEnhance.Contrast(im).enhance(1.08)


def scrim(im, start=0.46, end=0.80, peak=0.94, target=(20, 18, 15)):
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


def write(im, name, widths):
    for folder, wpx in widths.items():
        out = os.path.join(RES, folder)
        os.makedirs(out, exist_ok=True)
        im.resize((wpx, int(wpx * im.size[1] / im.size[0])), Image.LANCZOS).save(
            os.path.join(out, name), "PNG", optimize=True)


# slug -> (source jpg, crop-anchor 0..1 vertical, saturation, brightness, lean rgb, lean strength)
TILES = {
    "art_ch_news":         ("art_ch_news.jpg",         1.00, 0.75, 0.55, (140, 150, 172), 0.16),
    "art_ch_weather":      ("art_ch_weather.jpg",       0.42, 0.85, 0.62, (100, 152, 214), 0.16),
    "art_ch_classic":      ("art_ch_classic.jpg",       0.28, 0.70, 0.50, (182, 142,  90), 0.20),
    "art_ch_gameshows":    ("art_ch_gameshows.jpg",     0.42, 0.80, 0.58, (204, 112,  90), 0.18),
    "art_show_sunday":     ("art_show_sunday.jpg",      0.75, 0.75, 0.64, (206, 182, 122), 0.16),
    "art_show_detective":  ("art_show_detective.jpg",   0.50, 0.70, 0.44, ( 84,  92, 152), 0.22),
    "art_show_baking":     ("art_show_baking.jpg",      0.50, 0.75, 0.54, (172, 122,  82), 0.18),
    "art_film_lighthouse": ("art_film_lighthouse.jpg",  0.35, 0.80, 0.60, ( 90, 172, 182), 0.18),
    "art_film_western":    ("art_film_western.jpg",     0.55, 0.85, 0.58, (206, 122,  70), 0.20),
    "art_film_wedding":    ("art_film_wedding.jpg",     0.40, 0.70, 0.62, (206, 144, 152), 0.16),
    "art_film_walk":       ("art_film_walk.jpg",        0.55, 0.80, 0.56, (122, 172, 102), 0.18),
    "art_set_hospital":    ("art_set_hospital.jpg",     0.30, 0.55, 0.60, (142, 182, 164), 0.16),
    "art_set_sisters":     ("art_set_sisters.jpg",      0.40, 0.55, 0.48, (152, 122, 142), 0.16),
    "art_set_allotment":   ("art_set_allotment.jpg",    0.40, 0.70, 0.50, (142, 162,  92), 0.20),
    "art_fam_concert":     ("art_fam_concert.jpg",      0.50, 0.65, 0.52, (192, 152, 102), 0.18),
    "art_fam_dog":         ("art_fam_dog.jpg",          0.45, 0.80, 0.60, (152, 192, 102), 0.18),
    "art_fam_birthday":    ("art_fam_birthday.jpg",     0.40, 0.75, 0.54, (212,  92,  92), 0.18),
}

TILE_W = {"drawable-hdpi": 480, "drawable-xhdpi": 640, "drawable-xxhdpi": 960}

if __name__ == "__main__":
    used = {}
    for slug, (src, anchor, sat, bright, lean, strength) in TILES.items():
        im = Image.open(os.path.join(SRC, src)).convert("RGB")
        im = scrim(grade(crop169(im, anchor), sat, bright, lean, strength))
        write(im, f"{slug}.png", TILE_W)
        used[slug] = src
    json.dump(used, open(os.path.join(SRC, "used.json"), "w"), indent=1)
    print("\n".join(f"{k}: {v}" for k, v in used.items()))
