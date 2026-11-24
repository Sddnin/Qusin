# SPDX-License-Identifier: Apache-2.0
# Copyright (c) 2026 Qusin authors
"""Find a font file that can draw every character of a text (for PDFs and pictures made with Python).

Usage:
  find_font.py "TEXT" [--bold]

Prints one line of JSON. Exit 0: {"path", "index", "name"} is a font that has every character.
Exit 3: no single font has them all; {"missing": [...], "best": {...}} lists what the closest font lacks.

Inside a generator script:
  import sys; sys.path.insert(0, "/opt/qusin/skills/create-files/scripts")
  from find_font import find_font
  f = find_font("Xin chào"); path, index = f["path"], f["index"]

Why this exists: the only font that is always installed (WenQuanYi Zen Hei) has Chinese but lacks
most Vietnamese letters (ă ơ ư Đ and the stacked tone marks), so the default choice prints empty boxes.
Needs reportlab (reads the font tables); uses fc-list when present, else scans /usr/share/fonts.
"""
import json
import os
import subprocess
import sys

EXT = (".ttf", ".otf", ".ttc")
DIRS = ("/usr/share/fonts", "/usr/local/share/fonts", os.path.expanduser("~/.fonts"))


def _candidates():
    paths = []
    try:
        out = subprocess.run(["fc-list", "--format", "%{file}\n"], capture_output=True, text=True, timeout=15).stdout
        paths += [p.strip() for p in out.splitlines()]
    except Exception:
        pass
    for d in DIRS:
        for root, _, files in os.walk(d):
            paths += [os.path.join(root, f) for f in files]
    seen, res = set(), []
    for p in paths:
        if p and p.lower().endswith(EXT) and p not in seen and os.path.isfile(p):
            seen.add(p)
            res.append(p)
    return res


def _rank(path, bold):
    n = os.path.basename(path).lower()
    score = 0
    if any(w in n for w in ("mono", "italic", "oblique", "condensed", "light", "thin", "symbol", "emoji")):
        score += 5
    is_bold = "bold" in n
    score += 0 if is_bold == bold else 3
    if "sans" in n or "zenhei" in n:
        score -= 1
    return score, len(n)


def _missing(path, text, index=0):
    """Characters of text the font lacks; None when reportlab cannot read the font (e.g. CFF .otf)."""
    try:
        from reportlab.pdfbase.ttfonts import TTFontFile
        f = TTFontFile(path, subfontIndex=index)
    except Exception:
        return None
    return sorted({c for c in text if not c.isspace() and ord(c) not in f.charToGlyph})


def find_font(text, bold=False):
    """Best font that has every character of text: {"path", "index", "name"}, or None."""
    best = None
    find_font.last_best = None
    for p in sorted(_candidates(), key=lambda p: _rank(p, bold)):
        miss = _missing(p, text)
        if miss is None:
            continue
        info = {"path": p, "index": 0, "name": os.path.basename(p)}
        if not miss:
            return info
        if best is None or len(miss) < len(best[1]):
            best = (info, miss)
    find_font.last_best = best
    return None


def main(argv):
    args = [a for a in argv if a != "--bold"]
    if len(args) != 1:
        print('usage: find_font.py "TEXT" [--bold]', file=sys.stderr)
        return 2
    text = args[0]
    found = find_font(text, bold="--bold" in argv)
    if found:
        print(json.dumps(found, ensure_ascii=False))
        return 0
    best = getattr(find_font, "last_best", None)
    out = {"missing": best[1] if best else sorted(set(text)), "best": best[0] if best else None}
    print(json.dumps(out, ensure_ascii=False))
    return 3


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
