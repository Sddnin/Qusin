#!/usr/bin/env python3
# SPDX-License-Identifier: Apache-2.0
"""Import a CSV file into a new SQLite database, preserving values as TEXT.
Usage: csv_to_sqlite.py INPUT.csv OUTPUT.sqlite [--table TABLE]
"""
import argparse, csv, json, re, sqlite3, sys
from pathlib import Path

def quote_ident(value: str) -> str:
    return '"' + value.replace('"', '""') + '"'

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument('input', type=Path)
    ap.add_argument('output', type=Path)
    ap.add_argument('--table', default='records')
    a = ap.parse_args()
    if not a.input.is_file(): ap.error(f'input not found: {a.input}')
    if a.output.exists(): ap.error(f'output already exists: {a.output}')
    with a.input.open('r', encoding='utf-8-sig', newline='') as f:
        sample = f.read(8192); f.seek(0)
        try: dialect = csv.Sniffer().sniff(sample, delimiters=',;\t|')
        except csv.Error: dialect = csv.excel
        reader = csv.reader(f, dialect)
        try: headers = next(reader)
        except StopIteration: ap.error('CSV is empty')
        if not headers: ap.error('CSV has no header columns')
        names, seen = [], set()
        for i, h in enumerate(headers, 1):
            base = h.strip() or f'column_{i}'
            name, n = base, 2
            while name.casefold() in seen:
                name = f'{base}_{n}'; n += 1
            seen.add(name.casefold()); names.append(name)
        a.output.parent.mkdir(parents=True, exist_ok=True)
        conn = sqlite3.connect(a.output)
        try:
            cols = ', '.join(f'{quote_ident(n)} TEXT' for n in names)
            conn.execute(f'CREATE TABLE {quote_ident(a.table)} ({cols})')
            marks = ','.join('?' for _ in names)
            sql = f'INSERT INTO {quote_ident(a.table)} VALUES ({marks})'
            batch, count = [], 0
            for line_num, row in enumerate(reader, 2):
                if len(row) != len(names):
                    raise ValueError(f'row {line_num}: expected {len(names)} columns, got {len(row)}')
                batch.append(row)
                if len(batch) >= 1000:
                    conn.executemany(sql, batch); count += len(batch); batch.clear()
            if batch: conn.executemany(sql, batch); count += len(batch)
            conn.commit()
            check = conn.execute('PRAGMA integrity_check').fetchone()[0]
            if check != 'ok': raise RuntimeError(f'integrity_check failed: {check}')
            total = conn.execute(f'SELECT COUNT(*) FROM {quote_ident(a.table)}').fetchone()[0]
        except Exception:
            conn.rollback(); conn.close(); a.output.unlink(missing_ok=True); raise
        conn.close()
    print(json.dumps({'database': str(a.output), 'table': a.table, 'columns': names, 'rows': total, 'integrity_check': 'ok'}, ensure_ascii=False))
    return 0
if __name__ == '__main__':
    try: raise SystemExit(main())
    except Exception as e:
        print(f'csv_to_sqlite: {type(e).__name__}: {e}', file=sys.stderr); raise SystemExit(2)
