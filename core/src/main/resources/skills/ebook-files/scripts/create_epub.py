#!/usr/bin/env python3
# SPDX-License-Identifier: Apache-2.0
"""Create a basic EPUB 3 book from a UTF-8 text/Markdown file.
Usage: create_epub.py INPUT.txt OUTPUT.epub --title TITLE [--author AUTHOR] [--language vi]
This helper intentionally creates a simple text-first EPUB, not a publishing layout engine.
"""
from __future__ import annotations
import argparse, html, re, sys, uuid, zipfile
from pathlib import Path
from datetime import datetime, timezone

def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument('input', type=Path)
    ap.add_argument('output', type=Path)
    ap.add_argument('--title', required=True)
    ap.add_argument('--author', default='')
    ap.add_argument('--language', default='vi')
    a = ap.parse_args()
    if not a.input.is_file():
        ap.error(f'input not found: {a.input}')
    if a.output.exists():
        ap.error(f'output already exists: {a.output}')
    raw = a.input.read_text(encoding='utf-8-sig')
    blocks = []
    for line in raw.splitlines():
        s = line.strip()
        if not s:
            continue
        m = re.match(r'^(#{1,6})\s+(.+)$', s)
        if m:
            level = min(len(m.group(1)) + 1, 6)
            blocks.append(f'<h{level}>{html.escape(m.group(2))}</h{level}>')
        else:
            blocks.append(f'<p>{html.escape(s)}</p>')
    body = '\n'.join(blocks) or '<p></p>'
    ident = 'urn:uuid:' + str(uuid.uuid4())
    author = f'<dc:creator>{html.escape(a.author)}</dc:creator>' if a.author else ''
    xhtml = f"""<?xml version="1.0" encoding="utf-8"?>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" lang="{html.escape(a.language)}" xml:lang="{html.escape(a.language)}">
<head><title>{html.escape(a.title)}</title><meta charset="utf-8"/><style>body{{font-family:serif;line-height:1.5;margin:5%;}}h2,h3{{page-break-after:avoid;}}</style></head>
<body><h1>{html.escape(a.title)}</h1>{body}</body></html>"""
    opf = f"""<?xml version="1.0" encoding="utf-8"?>
<package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="bookid" xml:lang="{html.escape(a.language)}">
<metadata xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:identifier id="bookid">{ident}</dc:identifier><dc:title>{html.escape(a.title)}</dc:title><dc:language>{html.escape(a.language)}</dc:language>{author}<meta property="dcterms:modified">{datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ')}</meta></metadata>
<manifest><item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/><item id="chapter" href="chapter.xhtml" media-type="application/xhtml+xml"/></manifest>
<spine><itemref idref="chapter"/></spine></package>"""
    nav = f"""<?xml version="1.0" encoding="utf-8"?>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" lang="{html.escape(a.language)}"><head><title>Contents</title></head><body><nav epub:type="toc"><h1>Contents</h1><ol><li><a href="chapter.xhtml">{html.escape(a.title)}</a></li></ol></nav></body></html>"""
    container = """<?xml version="1.0" encoding="utf-8"?>
<container xmlns="urn:oasis:names:tc:opendocument:xmlns:container" version="1.0"><rootfiles><rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/></rootfiles></container>"""
    a.output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(a.output, 'w') as z:
        z.writestr('mimetype', 'application/epub+zip', compress_type=zipfile.ZIP_STORED)
        z.writestr('META-INF/container.xml', container, compress_type=zipfile.ZIP_DEFLATED)
        z.writestr('OEBPS/content.opf', opf, compress_type=zipfile.ZIP_DEFLATED)
        z.writestr('OEBPS/nav.xhtml', nav, compress_type=zipfile.ZIP_DEFLATED)
        z.writestr('OEBPS/chapter.xhtml', xhtml, compress_type=zipfile.ZIP_DEFLATED)
    with zipfile.ZipFile(a.output) as z:
        assert z.namelist()[0] == 'mimetype'
        assert z.read('mimetype') == b'application/epub+zip'
        assert z.testzip() is None
    print(f'Created and ZIP-verified EPUB: {a.output} ({a.output.stat().st_size} bytes)')
    return 0
if __name__ == '__main__':
    try:
        raise SystemExit(main())
    except (OSError, UnicodeError, zipfile.BadZipFile) as e:
        print(f'create_epub: {e}', file=sys.stderr)
        raise SystemExit(2)
