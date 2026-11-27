#!/usr/bin/env bash
# Remove the pre-rename Kotlin/Java/AIDL source trees that can remain when a ZIP
# is uploaded by merging files instead of replacing the repository tree.
# The canonical source tree is com.qusin.qusin; keeping both namespaces makes
# Android resolve generated R/BuildConfig against the wrong package and makes
# core tests scan duplicate source files.
set -euo pipefail

removed=0
while IFS= read -r -d '' dir; do
  echo "Removing stale source namespace: $dir"
  rm -rf -- "$dir"
  removed=$((removed + 1))
done < <(find app core -type d -path '*/com/wangzan101' -prune -print0 2>/dev/null)

prompt_dir="core/src/main/resources/prompts/tools"
if [[ -d "$prompt_dir" ]]; then
  while IFS= read -r -d '' file; do
    echo "Removing obsolete prompt description: $file"
    rm -f -- "$file"
  done < <(find "$prompt_dir" -maxdepth 1 -type f -name 'mika_*.md' -print0)
fi

# Fail fast if a stale tree survives, rather than allowing Gradle to emit
# hundreds of misleading unresolved R/BuildConfig errors later.
if find app core -type d -path '*/com/wangzan101' -print -quit 2>/dev/null | grep -q .; then
  echo "::error::Legacy com/wangzan101 source directories remain after cleanup"
  exit 1
fi
if find "$prompt_dir" -maxdepth 1 -type f -name 'mika_*.md' -print -quit 2>/dev/null | grep -q .; then
  echo "::error::Obsolete mika_*.md prompt files remain after cleanup"
  exit 1
fi

echo "Legacy cleanup complete: removed $removed source directories; canonical package is com.qusin.qusin."
