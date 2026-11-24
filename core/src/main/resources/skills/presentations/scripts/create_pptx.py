#!/usr/bin/env python3
# SPDX-License-Identifier: Apache-2.0
# Copyright (c) 2026 Qusin authors

"""Create a simple, standards-based PowerPoint (.pptx) without third-party packages.

Usage: python3 create_pptx.py INPUT.json OUTPUT.pptx
Input JSON: {"title": "Deck title", "author": "Name", "slides": [
  {"title": "Slide title", "bullets": ["First point", "Second point"]},
  {"title": "Another slide", "text": "A short paragraph"}
]}
"""
from __future__ import annotations

import json
import os
import re
import sys
import zipfile
from xml.sax.saxutils import escape

EMU_W = 12_192_000
EMU_H = 6_858_000
NS_A = "http://schemas.openxmlformats.org/drawingml/2006/main"
NS_P = "http://schemas.openxmlformats.org/presentationml/2006/main"
NS_R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"


def fail(message: str) -> "NoReturn":
    print(f"create_pptx: {message}", file=sys.stderr)
    raise SystemExit(2)


def xml_text(value: object) -> str:
    text = str(value)
    # XML 1.0 disallows most C0 controls; preserve tab/newline and printable chars.
    text = "".join(c for c in text if c in "\t\n\r" or ord(c) >= 0x20)
    return escape(text)


def run(text: str, *, bold: bool = False, size: int = 1800, color: str = "24324A") -> str:
    b = '<a:b/>' if bold else ''
    return (f'<a:r><a:rPr lang="vi-VN" sz="{size}" dirty="0">{b}'
            f'<a:solidFill><a:srgbClr val="{color}"/></a:solidFill>'
            '<a:latin typeface="Arial"/><a:ea typeface="Arial"/><a:cs typeface="Arial"/>'
            f'</a:rPr><a:t xml:space="preserve">{xml_text(text)}</a:t></a:r>')


def paragraph(text: str, *, bullet: bool = False, size: int = 1800, bold: bool = False) -> str:
    ppr = '<a:pPr marL="342900" indent="-228600"><a:buChar char="•"/></a:pPr>' if bullet else '<a:pPr marL="0" indent="0"/>'
    return f'<a:p>{ppr}{run(text, bold=bold, size=size)}<a:endParaRPr lang="vi-VN"/></a:p>'


def shape_header(shape_id: int, name: str, x: int, y: int, cx: int, cy: int) -> str:
    return (f'<p:nvSpPr><p:cNvPr id="{shape_id}" name="{name}"/><p:cNvSpPr txBox="1"/>'
            '<p:nvPr/></p:nvSpPr>'
            f'<p:spPr><a:xfrm><a:off x="{x}" y="{y}"/><a:ext cx="{cx}" cy="{cy}"/></a:xfrm>'
            '<a:prstGeom prst="rect"><a:avLst/></a:prstGeom><a:noFill/><a:ln><a:noFill/></a:ln></p:spPr>')


def text_shape(shape_id: int, name: str, x: int, y: int, cx: int, cy: int, paragraphs: list[str]) -> str:
    return (f'<p:sp>{shape_header(shape_id, name, x, y, cx, cy)}'
            '<p:txBody><a:bodyPr wrap="square" lIns="91440" rIns="91440" tIns="45720" bIns="45720" anchor="t"/>'
            '<a:lstStyle/>' + ''.join(paragraphs) + '</p:txBody></p:sp>')


def slide_xml(slide: dict, index: int) -> str:
    title = str(slide.get("title", f"Slide {index}"))[:500]
    bullets = slide.get("bullets", [])
    if not isinstance(bullets, list):
        fail(f"slides[{index - 1}].bullets must be an array")
    body_items: list[str] = []
    text = slide.get("text")
    if text is not None and str(text).strip():
        body_items.extend(paragraph(line, size=1900) for line in str(text).splitlines() if line.strip())
    for item in bullets:
        if isinstance(item, (str, int, float)):
            body_items.append(paragraph(str(item), bullet=True, size=1850))
        elif isinstance(item, dict):
            label = str(item.get("label", ""))
            detail = str(item.get("text", ""))
            if label:
                body_items.append(paragraph(label, bullet=True, size=1900, bold=True))
            if detail:
                body_items.append(paragraph(detail, size=1650))
        else:
            fail(f"slides[{index - 1}].bullets entries must be text or objects")
    if not body_items:
        body_items.append(paragraph("", size=1700))
    # Placeholders are ordinary editable PowerPoint text boxes, not flattened images.
    tree = ('<p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>'
            '<p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/>'
            '<a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>')
    tree += text_shape(2, "Title", 550000, 380000, 11000000, 1150000,
                       [paragraph(title, size=3000, bold=True)])
    tree += text_shape(3, "Body", 700000, 1700000, 10800000, 4700000, body_items)
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            f'<p:sld xmlns:a="{NS_A}" xmlns:r="{NS_R}" xmlns:p="{NS_P}">'
            f'<p:cSld><p:spTree>{tree}</p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sld>')


def content_types(slide_count: int) -> str:
    overrides = [
        '<Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>',
        '<Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>',
        '<Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>',
        '<Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>',
    ]
    overrides += [f'<Override PartName="/ppt/slides/slide{i}.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>' for i in range(1, slide_count + 1)]
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
            '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
            '<Default Extension="xml" ContentType="application/xml"/>' + ''.join(overrides) + '</Types>')


def presentation_xml(slide_count: int) -> str:
    ids = ''.join(f'<p:sldId id="{255 + i}" r:id="rId{i + 1}"/>' for i in range(1, slide_count + 1))
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            f'<p:presentation xmlns:a="{NS_A}" xmlns:r="{NS_R}" xmlns:p="{NS_P}">'
            '<p:sldMasterIdLst><p:sldMasterId id="2147483648" r:id="rId1"/></p:sldMasterIdLst>'
            f'<p:sldIdLst>{ids}</p:sldIdLst>'
            f'<p:sldSz cx="{EMU_W}" cy="{EMU_H}" type="wide"/><p:notesSz cx="6858000" cy="9144000"/>'
            '<p:defaultTextStyle><a:defPPr><a:defRPr lang="vi-VN"/></a:defPPr></p:defaultTextStyle>'
            '</p:presentation>')


def theme_xml() -> str:
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="Qusin Simple">'
            '<a:themeElements><a:clrScheme name="Qusin"><a:dk1><a:srgbClr val="182235"/></a:dk1>'
            '<a:lt1><a:srgbClr val="FFFFFF"/></a:lt1><a:dk2><a:srgbClr val="24324A"/></a:dk2>'
            '<a:lt2><a:srgbClr val="F4F6FA"/></a:lt2><a:accent1><a:srgbClr val="2864C5"/></a:accent1>'
            '<a:accent2><a:srgbClr val="2C8C83"/></a:accent2><a:accent3><a:srgbClr val="D98B35"/></a:accent3>'
            '<a:accent4><a:srgbClr val="8B68B5"/></a:accent4><a:accent5><a:srgbClr val="C65B70"/></a:accent5>'
            '<a:accent6><a:srgbClr val="6B7B91"/></a:accent6><a:hlink><a:srgbClr val="0563C1"/></a:hlink>'
            '<a:folHlink><a:srgbClr val="954F72"/></a:folHlink></a:clrScheme>'
            '<a:fontScheme name="Qusin"><a:majorFont><a:latin typeface="Arial"/><a:ea typeface="Arial"/><a:cs typeface="Arial"/></a:majorFont>'
            '<a:minorFont><a:latin typeface="Arial"/><a:ea typeface="Arial"/><a:cs typeface="Arial"/></a:minorFont></a:fontScheme>'
            '<a:fmtScheme name="Qusin"><a:fillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:fillStyleLst>'
            '<a:lnStyleLst><a:ln w="6350"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln>'
            '<a:ln w="12700"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln>'
            '<a:ln w="19050"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln></a:lnStyleLst>'
            '<a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst>'
            '<a:bgFillStyleLst><a:solidFill><a:schemeClr val="lt1"/></a:solidFill></a:bgFillStyleLst>'
            '</a:fmtScheme></a:themeElements><a:objectDefaults/><a:extraClrSchemeLst/></a:theme>')


def build(data: dict, output: str) -> None:
    slides = data.get("slides")
    if not isinstance(slides, list) or not slides:
        fail("input must contain a non-empty slides array")
    if len(slides) > 200:
        fail("maximum 200 slides")
    if any(not isinstance(s, dict) for s in slides):
        fail("every slide must be an object")

    parts: dict[str, str] = {
        "[Content_Types].xml": content_types(len(slides)),
        "_rels/.rels": ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>'
            '</Relationships>'),
        "ppt/presentation.xml": presentation_xml(len(slides)),
        "ppt/_rels/presentation.xml.rels": (
            '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>'
            + ''.join(f'<Relationship Id="rId{i + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide{i}.xml"/>' for i in range(1, len(slides) + 1))
            + '</Relationships>'),
        "ppt/slideMasters/slideMaster1.xml": (
            '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            f'<p:sldMaster xmlns:a="{NS_A}" xmlns:r="{NS_R}" xmlns:p="{NS_P}">'
            '<p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>'
            '<p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>'
            '</p:spTree></p:cSld><p:clrMap bg1="lt1" tx1="dk1" bg2="lt2" tx2="dk2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/>'
            '<p:sldLayoutIdLst><p:sldLayoutId id="1" r:id="rId1"/></p:sldLayoutIdLst>'
            '<p:txStyles><p:titleStyle/><p:bodyStyle/><p:otherStyle/></p:txStyles></p:sldMaster>'),
        "ppt/slideMasters/_rels/slideMaster1.xml.rels": (
            '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>'
            '<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="../theme/theme1.xml"/>'
            '</Relationships>'),
        "ppt/slideLayouts/slideLayout1.xml": (
            '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            f'<p:sldLayout xmlns:a="{NS_A}" xmlns:r="{NS_R}" xmlns:p="{NS_P}" type="blank" preserve="1">'
            '<p:cSld name="Blank"><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>'
            '<p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>'
            '</p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sldLayout>'),
        "ppt/slideLayouts/_rels/slideLayout1.xml.rels": (
            '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/>'
            '</Relationships>'),
        "ppt/theme/theme1.xml": theme_xml(),
    }
    for i, slide in enumerate(slides, 1):
        parts[f"ppt/slides/slide{i}.xml"] = slide_xml(slide, i)
        parts[f"ppt/slides/_rels/slide{i}.xml.rels"] = (
            '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
            '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>'
            '</Relationships>')

    os.makedirs(os.path.dirname(os.path.abspath(output)), exist_ok=True)
    temp = output + ".tmp"
    try:
        with zipfile.ZipFile(temp, "w", compression=zipfile.ZIP_DEFLATED) as z:
            for name, content in parts.items():
                z.writestr(name, content.encode("utf-8"))
        # Check ZIP integrity and that every XML part is well formed before replacing the target.
        from xml.etree import ElementTree as ET
        with zipfile.ZipFile(temp, "r") as z:
            bad = z.testzip()
            if bad:
                fail(f"corrupt ZIP member: {bad}")
            for name in z.namelist():
                if name.endswith(".xml") or name.endswith(".rels"):
                    ET.fromstring(z.read(name))
        os.replace(temp, output)
    except Exception:
        try:
            os.remove(temp)
        except OSError:
            pass
        raise


def main() -> None:
    if len(sys.argv) != 3 or sys.argv[1] in {"-h", "--help"}:
        print(__doc__.strip())
        raise SystemExit(0 if len(sys.argv) in (1, 2) else 2)
    try:
        with open(sys.argv[1], "r", encoding="utf-8") as f:
            data = json.load(f)
    except (OSError, json.JSONDecodeError) as e:
        fail(f"cannot read input JSON: {e}")
    if not isinstance(data, dict):
        fail("input JSON root must be an object")
    build(data, sys.argv[2])
    print(f"created {sys.argv[2]} ({os.path.getsize(sys.argv[2])} bytes)")


if __name__ == "__main__":
    main()
