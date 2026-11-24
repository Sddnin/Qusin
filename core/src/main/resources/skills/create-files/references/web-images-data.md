# Web pages, notes, pictures, SVG, data files, archives

## Web page (.html)

One self-contained file: styles inline, no link to the internet (fonts, scripts, pictures from a URL do not load offline). Embed small pictures as `data:` URLs. Start from this and write it with `write_file`:

```html
<!doctype html>
<html lang="vi">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Tiêu đề trang</title>
<style>
  :root { --bg:#ffffff; --ink:#1b2a41; --muted:#5b6b82; --accent:#3269d7; --card:#f3f6fb; }
  @media (prefers-color-scheme: dark) { :root { --bg:#10151d; --ink:#e8edf5; --muted:#9aa8bd; --accent:#7fa6ff; --card:#1a2230; } }
  body { margin:0; background:var(--bg); color:var(--ink); font:16px/1.6 system-ui, -apple-system, "Segoe UI", sans-serif; }
  main { max-width:720px; margin:0 auto; padding:24px 20px 48px; }
  h1 { font-size:1.8rem; line-height:1.25; }
  .card { background:var(--card); border-radius:16px; padding:16px 18px; margin:16px 0; }
  a { color:var(--accent); }
  table { border-collapse:collapse; width:100%; } th, td { text-align:left; padding:8px 10px; border-bottom:1px solid var(--card); }
</style>
</head>
<body><main>
<h1>Tiêu đề</h1>
<p class="muted">Một câu mô tả.</p>
<div class="card"><h2>Mục một</h2><p>Nội dung.</p></div>
</main></body>
</html>
```

## Notes and documents as text (.md, .txt)

`write_file` directly, UTF-8. Markdown shows as a formatted page in Qusin's viewer. Use `#` headings, `-` lists and `|` tables; no HTML inside.

## Pictures

- An illustration, photo, poster or wallpaper: use the `image_generate` / `image_edit` tools, not Python.
- A diagram, a chart image, a card with exact text, a resized or cropped photo: Pillow. Text needs a font that has every character (`find_font`); Pillow's default font cannot draw Vietnamese.

```python
import os
import sys

sys.path.insert(0, "/opt/qusin/skills/create-files/scripts")
import find_font as font_finder
from PIL import Image, ImageDraw, ImageFont

title, sub = "Xin chào, Việt Nam", "Thẻ chào mừng"
font = font_finder.find_font(title + sub)
if not font:
    raise SystemExit("no installed font has all these characters")
W, H = 1080, 1080
img = Image.new("RGB", (W, H), "#F3F6FB")
d = ImageDraw.Draw(img)
d.rounded_rectangle((60, 60, W - 60, H - 60), radius=48, fill="#FFFFFF", outline="#C9D1DD", width=3)
big = ImageFont.truetype(font["path"], 84, index=font["index"])
small = ImageFont.truetype(font["path"], 40, index=font["index"])
d.text((W // 2, H // 2 - 30), title, font=big, fill="#1B2A41", anchor="mm")
d.text((W // 2, H // 2 + 70), sub, font=small, fill="#3269D7", anchor="mm")
os.makedirs("/workspace/out", exist_ok=True)
img.save("/workspace/out/the-chao.png")                 # .jpg needs img.convert("RGB") and quality=90

# resize: Image.open(p).thumbnail((1280, 1280)); crop: im.crop((l, t, r, b)); rotate: im.rotate(90, expand=True)
# animated GIF from several frames:
# frames[0].save("/workspace/out/anim.gif", save_all=True, append_images=frames[1:], duration=400, loop=0)
```

## SVG and simple charts

SVG is plain text: write it with `write_file`, always with a `viewBox`. For a chart, generate the bars from the data:

```python
import os
from xml.sax.saxutils import escape

data = [("T1", 120), ("T2", 150), ("T3", 90), ("T4", 180)]
W, H, pad, base = 600, 360, 50, 300
top = max(v for _, v in data)
bw = (W - 2 * pad) / len(data)
parts = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" font-family="sans-serif">',
         f'<rect width="{W}" height="{H}" fill="#ffffff"/>',
         f'<line x1="{pad}" y1="{base}" x2="{W - pad}" y2="{base}" stroke="#C9D1DD"/>']
for i, (label, v) in enumerate(data):
    h = (base - 40) * v / top
    x = pad + i * bw + bw * 0.15
    parts.append(f'<rect x="{x:.1f}" y="{base - h:.1f}" width="{bw * 0.7:.1f}" height="{h:.1f}" rx="6" fill="#3269D7"/>')
    parts.append(f'<text x="{x + bw * 0.35:.1f}" y="{base - h - 8:.1f}" text-anchor="middle" font-size="14" fill="#1B2A41">{v}</text>')
    parts.append(f'<text x="{x + bw * 0.35:.1f}" y="{base + 20}" text-anchor="middle" font-size="14" fill="#5B6B82">{escape(label)}</text>')
parts.append("</svg>")
os.makedirs("/workspace/out", exist_ok=True)
open("/workspace/out/doanh-thu.svg", "w", encoding="utf-8").write("\n".join(parts))
```

To put a chart inside a Word/PDF/slide, make it as a PNG with Pillow instead (SVG is not accepted there). Excel charts: use xlsxwriter (see office-and-pdf.md). matplotlib is not installed.

## Data files

- **JSON**: `json.dump(data, f, ensure_ascii=False, indent=2)`.
- **Calendar event (.ics)**: lines must end with CRLF. To put an event on the user's own phone calendar use the calendar tool instead.

```python
import os

lines = ["BEGIN:VCALENDAR", "VERSION:2.0", "PRODID:-//Qusin//VI", "BEGIN:VEVENT",
         "UID:evt-20261201-1@qusin", "DTSTAMP:20261124T080000Z",
         "DTSTART:20261201T020000Z", "DTEND:20261201T030000Z",      # UTC; 09:00-10:00 in UTC+7
         "SUMMARY:Họp nhóm", "LOCATION:Phòng 3", "END:VEVENT", "END:VCALENDAR"]
os.makedirs("/workspace/out", exist_ok=True)
with open("/workspace/out/hop-nhom.ics", "w", encoding="utf-8", newline="") as f:
    f.write("\r\n".join(lines) + "\r\n")
```

- **Contact (.vcf)**: `BEGIN:VCARD` / `VERSION:3.0` / `FN:Tên` / `TEL:0901234567` / `END:VCARD`, one block per person.

## Archives (.zip)

```python
import os
import zipfile

os.makedirs("/workspace/out", exist_ok=True)
with zipfile.ZipFile("/workspace/out/tai-lieu.zip", "w", zipfile.ZIP_DEFLATED) as z:
    for name in ["bao-cao.docx", "chi-tieu.xlsx"]:
        z.write(f"/workspace/out/{name}", arcname=name)
```

## Cannot make here

Video; audio by hand (use `audio_generate`); old `.doc` `.xls` `.ppt` (make `.docx` `.xlsx` `.pptx`, which every current app opens); password-protected or signed files; files that need a live service (Google Docs, Notion). Say so in one sentence and offer the closest file.
