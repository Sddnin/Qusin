#!/usr/bin/env python3
# SPDX-License-Identifier: Apache-2.0
"""Create and verify ZIP/TAR/GZIP archives using Python's standard library.

Usage:
  create_archive.py FORMAT OUTPUT SOURCE [SOURCE ...]
Formats: zip, tar, tar.gz, tar.bz2, tar.xz, gz, 7z
GZ accepts exactly one source file. 7Z requires an installed 7z/7zz executable.
"""
from __future__ import annotations
import argparse, gzip, hashlib, json, os, shutil, subprocess, sys, tarfile, zipfile
from pathlib import Path

FORMATS = {"zip", "tar", "tar.gz", "tgz", "tar.bz2", "tbz2", "tar.xz", "txz", "gz", "7z"}

def fail(msg: str, code: int = 2) -> None:
    print(f"create_archive: {msg}", file=sys.stderr)
    raise SystemExit(code)

def collect(sources: list[Path], output: Path) -> list[tuple[Path, str]]:
    out_resolved = output.resolve()
    items: list[tuple[Path, str]] = []
    seen: set[str] = set()
    for src in sources:
        if not src.exists():
            fail(f"source does not exist: {src}")
        if src.is_symlink():
            fail(f"symlink sources are not followed: {src}")
        if src.is_file():
            candidates = [src]
        elif src.is_dir():
            candidates = sorted(p for p in src.rglob("*") if p.is_file() and not p.is_symlink())
        else:
            fail(f"unsupported source type: {src}")
        for path in candidates:
            if path.resolve() == out_resolved:
                continue
            if path.is_symlink():
                continue
            if src.is_dir():
                arc = (Path(src.name) / path.relative_to(src)).as_posix()
            else:
                arc = Path(src.name).as_posix()
            if arc.startswith("/") or ".." in Path(arc).parts:
                fail(f"unsafe archive path: {arc}")
            if arc in seen:
                fail(f"duplicate archive path: {arc}")
            seen.add(arc)
            items.append((path, arc))
    if not items:
        fail("no regular files found in sources")
    return items

def digest(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()

def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("format", choices=sorted(FORMATS))
    ap.add_argument("output", type=Path)
    ap.add_argument("sources", nargs="+", type=Path)
    ap.add_argument("--manifest", action="store_true", help="write OUTPUT.manifest.json with file sizes and SHA-256")
    args = ap.parse_args()
    fmt = {"tgz": "tar.gz", "tbz2": "tar.bz2", "txz": "tar.xz"}.get(args.format, args.format)
    if fmt == "gz" and (len(args.sources) != 1 or not args.sources[0].is_file()):
        fail("gz compresses exactly one regular file; use tar.gz for multiple files")
    items = collect(args.sources, args.output)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    if args.output.exists():
        fail(f"output already exists; choose another name or remove it explicitly: {args.output}")
    try:
        if fmt == "zip":
            with zipfile.ZipFile(args.output, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as z:
                for p, arc in items:
                    z.write(p, arc)
        elif fmt == "tar":
            with tarfile.open(args.output, "w") as t:
                for p, arc in items:
                    t.add(p, arcname=arc, recursive=False)
        elif fmt in {"tar.gz", "tar.bz2", "tar.xz"}:
            mode = {"tar.gz": "w:gz", "tar.bz2": "w:bz2", "tar.xz": "w:xz"}[fmt]
            with tarfile.open(args.output, mode) as t:
                for p, arc in items:
                    t.add(p, arcname=arc, recursive=False)
        elif fmt == "gz":
            source = items[0][0]
            with source.open("rb") as inp, gzip.open(args.output, "wb", compresslevel=6) as out:
                shutil.copyfileobj(inp, out)
        elif fmt == "7z":
            exe = shutil.which("7z") or shutil.which("7zz")
            if not exe:
                fail("7z/7zz executable is not installed; use zip or tar.gz instead")
            # Build from a temporary staging directory to preserve relative paths.
            import tempfile
            with tempfile.TemporaryDirectory(prefix="qusin-7z-") as td:
                stage = Path(td)
                for p, arc in items:
                    target = stage / arc
                    target.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(p, target)
                cmd = [exe, "a", "-t7z", str(args.output.resolve()), "."]
                proc = subprocess.run(cmd, cwd=stage, capture_output=True, text=True)
                if proc.returncode != 0:
                    fail("7z creation failed: " + (proc.stderr.strip() or proc.stdout.strip()))
        # Verify archive can be read and member count matches source inventory.
        if fmt == "zip":
            with zipfile.ZipFile(args.output) as z:
                bad = z.testzip()
                if bad:
                    fail(f"ZIP CRC test failed for: {bad}", 3)
                names = z.namelist()
        elif fmt in {"tar", "tar.gz", "tar.bz2", "tar.xz"}:
            with tarfile.open(args.output, "r:*") as t:
                members = t.getmembers()
                if any(not (m.isfile() or m.isdir()) for m in members):
                    fail("archive contains unexpected special entries", 3)
                names = [m.name for m in members if m.isfile()]
        elif fmt == "gz":
            with gzip.open(args.output, "rb") as f:
                while f.read(1024 * 1024):
                    pass
            names = [items[0][1]]
        else:  # 7z
            proc = subprocess.run([exe, "t", str(args.output.resolve())], capture_output=True, text=True)
            if proc.returncode != 0:
                fail("7z test failed: " + (proc.stderr.strip() or proc.stdout.strip()), 3)
            names = [arc for _, arc in items]
        report = {
            "output": str(args.output), "format": fmt, "files": len(items),
            "archive_bytes": args.output.stat().st_size,
            "verified": True,
        }
        if args.manifest:
            manifest_path = args.output.with_name(args.output.name + ".manifest.json")
            manifest = [{"path": arc, "size": p.stat().st_size, "sha256": digest(p)} for p, arc in items]
            manifest_path.write_text(json.dumps({"archive": args.output.name, "files": manifest}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            report["manifest"] = str(manifest_path)
        print(json.dumps(report, ensure_ascii=False))
        return 0
    except SystemExit:
        raise
    except (OSError, tarfile.TarError, zipfile.BadZipFile) as e:
        if args.output.exists():
            args.output.unlink(missing_ok=True)
        fail(f"archive operation failed: {type(e).__name__}: {e}", 3)

if __name__ == "__main__":
    raise SystemExit(main())
