# Word, Excel, CSV, PDF, PowerPoint

All snippets write to `/workspace/out/`. Put them in a `.py` file with `write_file`, then run it with `run_command` (`python3 /workspace/tmp/make.py`). Replace the sample text with the user's real content.

## Word (.docx), python-docx

```python
import os
from docx import Document
from docx.shared import Pt, Cm
from docx.oxml.ns import qn

os.makedirs("/workspace/out", exist_ok=True)
doc = Document()
sec = doc.sections[0]
sec.left_margin = sec.right_margin = Cm(2.5)
normal = doc.styles["Normal"]
normal.font.name = "Calibri"
normal.font.size = Pt(11)
normal.element.rPr.rFonts.set(qn("w:eastAsia"), "SimSun")   # font for Chinese/Japanese/Korean text

doc.add_heading("Báo cáo tuần", level=0)                    # level 0 = title, 1-3 = headings
doc.add_paragraph("Đoạn mở đầu: tóm tắt trong một hai câu.")
doc.add_heading("1. Kết quả", level=1)
for item in ["Hoàn thành mục A", "Mục B còn chờ phản hồi"]:
    doc.add_paragraph(item, style="List Bullet")            # "List Number" for 1. 2. 3.

table = doc.add_table(rows=1, cols=3)
table.style = "Light Grid Accent 1"
for cell, text in zip(table.rows[0].cells, ["Tên", "Số lượng", "Giá"]):
    cell.text = text
for row in [("Cà phê", "2", "45.000"), ("Bánh mì", "1", "20.000")]:
    for cell, text in zip(table.add_row().cells, row):
        cell.text = text

# doc.add_picture("/workspace/out/chart.png", width=Cm(12))   # a picture made earlier
# doc.add_page_break()
doc.save("/workspace/out/bao-cao.docx")
```

## Excel (.xlsx), openpyxl

Use for data the user will edit. Formulas written by a library have **no stored result**: some phone viewers show them empty. Write the number when the user does not need a live formula; otherwise use the xlsxwriter form below, which can store the result too.

```python
import os
from openpyxl import Workbook
from openpyxl.styles import Font, PatternFill, Alignment
from openpyxl.utils import get_column_letter

os.makedirs("/workspace/out", exist_ok=True)
wb = Workbook()
ws = wb.active
ws.title = "Chi tiêu"
rows = [("Ngày", "Khoản", "Số tiền"), ("2026-11-01", "Ăn trưa", 45000), ("2026-11-02", "Xăng", 120000)]
for r in rows:
    ws.append(r)
for c in ws[1]:
    c.font = Font(bold=True, color="FFFFFF")
    c.fill = PatternFill("solid", fgColor="3269D7")
    c.alignment = Alignment(horizontal="center")
ws.freeze_panes = "A2"
for col in range(1, ws.max_column + 1):
    ws.column_dimensions[openpyxl.utils.get_column_letter(col)].width = 16
for (cell,) in ws.iter_rows(min_row=2, min_col=3, max_col=3):
    cell.number_format = "#,##0"
last = ws.max_row
ws.cell(last + 1, 2, "Tổng").font = Font(bold=True)
ws.cell(last + 1, 3, sum(r[2] for r in rows[1:])).number_format = "#,##0"   # plain number, shows everywhere
wb.save("/workspace/out/chi-tieu.xlsx")
```

## Excel with a chart or stored formula results, xlsxwriter

```python
import os
import xlsxwriter

os.makedirs("/workspace/out", exist_ok=True)
wb = xlsxwriter.Workbook("/workspace/out/doanh-thu.xlsx")
ws = wb.add_worksheet("Doanh thu")
head = wb.add_format({"bold": True, "bg_color": "#3269D7", "font_color": "white", "align": "center"})
money = wb.add_format({"num_format": "#,##0"})
data = [("Tháng 1", 120), ("Tháng 2", 150), ("Tháng 3", 90)]
ws.write_row(0, 0, ["Tháng", "Doanh thu"], head)
for i, (month, value) in enumerate(data, start=1):
    ws.write(i, 0, month)
    ws.write(i, 1, value, money)
n = len(data)
ws.write_formula(n + 1, 1, f"=SUM(B2:B{n + 1})", money, sum(v for _, v in data))   # last argument = stored result
ws.set_column(0, 1, 16)
chart = wb.add_chart({"type": "column"})            # also "line", "pie", "bar"
chart.add_series({"name": "Doanh thu", "categories": ["Doanh thu", 1, 0, n, 0], "values": ["Doanh thu", 1, 1, n, 1]})
chart.set_title({"name": "Doanh thu theo tháng"})
ws.insert_chart("D2", chart)
wb.close()
```

## CSV

Write with `utf-8-sig` (adds a BOM) or Excel shows accented letters as garbage.

```python
import csv
import os

os.makedirs("/workspace/out", exist_ok=True)
rows = [["Họ tên", "Điện thoại"], ["Nguyễn Văn A", "0901234567"]]
with open("/workspace/out/danh-sach.csv", "w", newline="", encoding="utf-8-sig") as f:
    csv.writer(f).writerows(rows)
```

## PDF, reportlab

The default PDF fonts cannot draw Vietnamese. Always pick a font that has every character with `find_font`. If it returns `None`, stop and tell the user the PDF cannot show those letters here; offer a Word file or an HTML page instead. Escape the user's text with `escape()` because Paragraph reads `<` and `&` as markup.

```python
import os
import sys
from xml.sax.saxutils import escape

sys.path.insert(0, "/opt/qusin/skills/create-files/scripts")
import find_font as font_finder
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import cm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle

title = "Báo cáo tuần"
paragraphs = ["Đoạn một: tóm tắt kết quả chính.", "Đoạn hai: việc cần làm tiếp theo & lưu ý."]
rows = [["Tên", "Số lượng", "Giá"], ["Cà phê", "2", "45.000"], ["Bánh mì", "1", "20.000"]]

text = title + "".join(paragraphs) + "".join("".join(r) for r in rows)
font = font_finder.find_font(text)
if not font:
    raise SystemExit("no installed font has all these characters")
pdfmetrics.registerFont(TTFont("Body", font["path"], subfontIndex=font["index"]))
pdfmetrics.registerFontFamily("Body", normal="Body", bold="Body", italic="Body", boldItalic="Body")  # so <b> does not crash

h1 = ParagraphStyle("h1", fontName="Body", fontSize=22, leading=28, spaceAfter=14, textColor=colors.HexColor("#1B2A41"))
p = ParagraphStyle("p", fontName="Body", fontSize=11, leading=17, spaceAfter=8)
story = [Paragraph(escape(title), h1)]
story += [Paragraph(escape(t), p) for t in paragraphs]
story.append(Spacer(1, 0.4 * cm))
table = Table(rows, hAlign="LEFT", colWidths=[6 * cm, 3 * cm, 3 * cm])
table.setStyle(TableStyle([
    ("FONTNAME", (0, 0), (-1, -1), "Body"), ("FONTSIZE", (0, 0), (-1, -1), 10),
    ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#3269D7")), ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
    ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#C9D1DD")),
    ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F3F6FB")]),
    ("TOPPADDING", (0, 0), (-1, -1), 6), ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
]))
story.append(table)
os.makedirs("/workspace/out", exist_ok=True)
SimpleDocTemplate("/workspace/out/bao-cao.pdf", pagesize=A4, title=title,
                  leftMargin=2.2 * cm, rightMargin=2.2 * cm, topMargin=2 * cm, bottomMargin=2 * cm).build(story)
```

## PowerPoint (.pptx), python-pptx

python-pptx does not ship with the runtime. Install it once, which needs the network ("Allow scripts to go online" in Qusin's settings):

`pip install --break-system-packages --target /workspace/.pylibs python-pptx`

If that fails because the network is off, tell the user which setting to turn on and offer a PDF or an HTML page instead. Do not hand-write the .pptx file format.

Lay shapes out yourself on a blank layout: the built-in placeholders are sized for 4:3 and sit in the wrong place once the slide is 16:9.

```python
import os
import sys

sys.path.insert(0, "/workspace/.pylibs")
from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.util import Inches, Pt

prs = Presentation()
prs.slide_width, prs.slide_height = Inches(13.333), Inches(7.5)     # 16:9
blank = prs.slide_layouts[6]
INK, ACCENT = RGBColor(0x1B, 0x2A, 0x41), RGBColor(0x32, 0x69, 0xD7)


def addText(slide, text, left, top, width, height, size, color=INK, bold=False):
    box = slide.shapes.add_textbox(Inches(left), Inches(top), Inches(width), Inches(height))
    tf = box.text_frame
    tf.word_wrap = True
    lines = text if isinstance(text, list) else [text]
    for i, line in enumerate(lines):
        para = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        para.text = line
        para.font.size, para.font.bold, para.font.color.rgb = Pt(size), bold, color
        para.space_after = Pt(10)
    return box


s = prs.slides.add_slide(blank)
addText(s, "Kế hoạch quý 4", 0.9, 2.6, 11.5, 1.4, 44, ACCENT, bold=True)
addText(s, "Trình bày bởi nhóm sản phẩm", 0.9, 4.1, 11.5, 0.8, 20)

s = prs.slides.add_slide(blank)
addText(s, "Ba ưu tiên", 0.9, 0.6, 11.5, 1.0, 32, ACCENT, bold=True)
addText(s, ["• Ra mắt tính năng giọng nói", "• Giảm thời gian khởi động", "• Hoàn thiện bản dịch"], 0.9, 1.9, 11.5, 4.5, 24)
# s.shapes.add_picture("/workspace/out/chart.png", Inches(7), Inches(1.8), width=Inches(5.5))

os.makedirs("/workspace/out", exist_ok=True)
prs.save("/workspace/out/ke-hoach.pptx")
```

One idea per slide, at most six lines, text at least 20 pt.
