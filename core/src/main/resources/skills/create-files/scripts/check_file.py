# SPDX-License-Identifier: Apache-2.0
# Copyright (c) 2026 Qusin authors
"""Check a file you just made and print a one-line JSON summary of what is really inside.

Usage:
  check_file.py FILE

Understands docx, xlsx, pptx, pdf, png/jpg/gif/webp, html, md/txt, csv, json, svg/xml, ics, vcf, zip.
Prints {"ok": true|false, "type", "bytes", ...details, "warnings": [...]}.
Exit 0: ok. Exit 2: file not found. Exit 3: the file is empty or does not open as its type.
Standard library only; pdf needs pypdf and pictures need Pillow (both ship with the runtime).
"""
import csv
import json
import os
import re
import sys
import zipfile
import xml.etree.ElementTree as ET

W = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"
A = "{http://schemas.openxmlformats.org/drawingml/2006/main}"
S = "{http://schemas.openxmlformats.org/spreadsheetml/2006/main}"
# http(s) resources a page pulls in (src/href attributes and CSS url(...)): none of them load offline
EXTERNAL_ATTR = re.compile(r"""(?:src|href)\s*=\s*["'](https?://[^"']+)""", re.I)
EXTERNAL_CSS = re.compile(r"""url\(\s*["']?(https?://[^)"']+)""", re.I)


def _sample(text, n=160):
    return re.sub(r"\s+", " ", text).strip()[:n]


def _zip_xml(z, name):
    return ET.fromstring(z.read(name))


def check_docx(path, r):
    with zipfile.ZipFile(path) as z:
        doc = _zip_xml(z, "word/document.xml")
        paras = [p for p in doc.iter(W + "p")]
        text = "\n".join("".join(t.text or "" for t in p.iter(W + "t")) for p in paras)
        r.update(paragraphs=len(paras), tables=len(list(doc.iter(W + "tbl"))),
                 images=sum(1 for n in z.namelist() if n.startswith("word/media/")), characters=len(text), sample=_sample(text))
    if not text.strip():
        r["warnings"].append("the document has no text")


def check_xlsx(path, r):
    with zipfile.ZipFile(path) as z:
        wb = _zip_xml(z, "xl/workbook.xml")
        names = [s.get("name") for s in wb.iter(S + "sheet")]
        sheets = sorted(n for n in z.namelist() if re.fullmatch(r"xl/worksheets/sheet\d+\.xml", n))
        formulas = uncached = errors = cells = 0
        for n in sheets:
            for c in _zip_xml(z, n).iter(S + "c"):
                cells += 1
                f, v = c.find(S + "f"), c.find(S + "v")
                if f is not None:
                    formulas += 1
                    if v is None or v.text in (None, ""):
                        uncached += 1
                if c.get("t") == "e":
                    errors += 1
        r.update(sheets=names, cells=cells, formulas=formulas, charts=sum(1 for n in z.namelist() if n.startswith("xl/charts/chart")))
    if uncached:
        r["warnings"].append(f"{uncached} formula cells have no stored result: some viewers show them empty or 0. "
                             "Write the value too (xlsxwriter: write_formula(cell, f, fmt, value)) or write plain numbers.")
    if errors:
        r["ok"] = False
        r["warnings"].append(f"{errors} cells hold an error value (#REF!, #DIV/0! ...)")


def check_pptx(path, r):
    with zipfile.ZipFile(path) as z:
        slides = sorted((n for n in z.namelist() if re.fullmatch(r"ppt/slides/slide\d+\.xml", n)),
                        key=lambda n: int(re.findall(r"\d+", n)[0]))
        texts = []
        for n in slides:
            texts.append(" ".join(t.text or "" for t in _zip_xml(z, n).iter(A + "t")))
        r.update(slides=len(slides), images=sum(1 for n in z.namelist() if n.startswith("ppt/media/")),
                 first_slide=_sample(texts[0]) if texts else "")
    if not slides:
        r["ok"] = False
    elif any(not t.strip() for t in texts):
        r["warnings"].append("some slides have no text: " + ", ".join(str(i + 1) for i, t in enumerate(texts) if not t.strip()))


def check_pdf(path, r):
    from pypdf import PdfReader
    rd = PdfReader(path)
    if rd.is_encrypted:
        r["warnings"].append("the PDF is encrypted")
    texts = [(p.extract_text() or "") for p in rd.pages]
    r.update(pages=len(rd.pages), characters=sum(len(t) for t in texts), sample=_sample(texts[0]) if texts else "")
    empty = [i + 1 for i, t in enumerate(texts) if not t.strip()]
    if empty:
        r["warnings"].append("pages without text (blank or pictures only): " + ",".join(map(str, empty[:20])))
    if not rd.pages:
        r["ok"] = False
    r["warnings"].append("text extraction cannot see missing glyphs: render page 1 "
                         "(pdftoppm -r 60 -png -f 1 -l 1 FILE.pdf out/page) and look at it")


def check_image(path, r):
    from PIL import Image
    with Image.open(path) as im:
        im.verify()
    with Image.open(path) as im:
        r.update(format=im.format, width=im.width, height=im.height, mode=im.mode, frames=getattr(im, "n_frames", 1))


def check_html(path, r):
    text = open(path, encoding="utf-8").read()
    low = text.lower()
    r.update(characters=len(text), title=(re.search(r"<title>(.*?)</title>", text, re.I | re.S) or [None, ""])[1].strip())
    if "<html" not in low:
        r["warnings"].append("no <html> tag")
    if "charset" not in low:
        r["warnings"].append('no <meta charset="utf-8">: accented letters may show wrong')
    ext = EXTERNAL_ATTR.findall(text) + EXTERNAL_CSS.findall(text)
    if ext:
        r["warnings"].append(f"{len(ext)} external links/resources (they will not load offline): " + ", ".join(ext[:3]))
    if "viewport" not in low:
        r["warnings"].append("no viewport meta tag: the page will look tiny on a phone")


def check_text(path, r):
    text = open(path, encoding="utf-8").read()
    r.update(characters=len(text), lines=text.count("\n") + 1, sample=_sample(text))
    if not text.strip():
        r["ok"] = False


def check_csv(path, r):
    raw = open(path, "rb").read()
    r["bom"] = raw.startswith(b"\xef\xbb\xbf")
    rows = list(csv.reader(raw.decode("utf-8-sig").splitlines()))
    widths = {len(x) for x in rows}
    r.update(rows=len(rows), columns=max(widths) if widths else 0, header=rows[0] if rows else [])
    if len(widths) > 1:
        r["warnings"].append(f"rows have different column counts: {sorted(widths)}")
    if not r["bom"] and any(ord(c) > 127 for c in raw.decode("utf-8", "replace")):
        r["warnings"].append("no BOM: Excel will garble accented letters; write with encoding='utf-8-sig'")


def check_json(path, r):
    data = json.load(open(path, encoding="utf-8"))
    r.update(json_type=type(data).__name__, items=len(data) if hasattr(data, "__len__") else 1)


def check_xml(path, r):
    root = ET.parse(path).getroot()
    r.update(root=root.tag.split("}")[-1])
    if root.tag.endswith("svg") and not (root.get("viewBox") or (root.get("width") and root.get("height"))):
        r["warnings"].append("svg has no viewBox or width/height: it may render at the wrong size")


def check_ics(path, r):
    raw = open(path, "rb").read().decode("utf-8")
    r.update(events=raw.count("BEGIN:VEVENT"))
    if "BEGIN:VCALENDAR" not in raw or "END:VCALENDAR" not in raw:
        r["ok"] = False
    if "\r\n" not in raw:
        r["warnings"].append("lines must end with CRLF (\\r\\n) or some calendar apps reject the file")


def check_vcf(path, r):
    raw = open(path, encoding="utf-8").read()
    r.update(contacts=raw.count("BEGIN:VCARD"))
    if not raw.count("BEGIN:VCARD") or raw.count("BEGIN:VCARD") != raw.count("END:VCARD"):
        r["ok"] = False


def check_zip(path, r):
    with zipfile.ZipFile(path) as z:
        bad = z.testzip()
        names = z.namelist()
    r.update(entries=len(names), names=names[:20])
    if bad:
        r["ok"] = False
        r["warnings"].append(f"corrupt entry: {bad}")


CHECKS = {
    ".docx": check_docx, ".xlsx": check_xlsx, ".pptx": check_pptx, ".pdf": check_pdf,
    ".png": check_image, ".jpg": check_image, ".jpeg": check_image, ".gif": check_image, ".webp": check_image,
    ".html": check_html, ".htm": check_html, ".md": check_text, ".txt": check_text, ".csv": check_csv,
    ".json": check_json, ".svg": check_xml, ".xml": check_xml, ".ics": check_ics, ".vcf": check_vcf, ".zip": check_zip,
}


def main(argv):
    if len(argv) != 1:
        print("usage: check_file.py FILE", file=sys.stderr)
        return 2
    path = argv[0]
    if not os.path.isfile(path):
        print(json.dumps({"ok": False, "error": "file not found: " + path}))
        return 2
    ext = os.path.splitext(path)[1].lower()
    r = {"ok": True, "type": ext.lstrip(".") or "unknown", "bytes": os.path.getsize(path), "warnings": []}
    if r["bytes"] == 0:
        r["ok"] = False
        r["warnings"].append("the file is empty")
    elif ext in CHECKS:
        try:
            CHECKS[ext](path, r)
        except Exception as e:  # does not open as its type
            r["ok"] = False
            r["error"] = f"{type(e).__name__}: {e}"
    print(json.dumps(r, ensure_ascii=False))
    return 0 if r["ok"] else 3


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
