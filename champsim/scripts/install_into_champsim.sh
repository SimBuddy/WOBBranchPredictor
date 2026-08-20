#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "usage: $0 /path/to/ChampSim" >&2
  exit 2
fi

package_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
target=$(cd "$1" && pwd)
source "$package_root/champsim/config/upstream.env"

test "$(git -C "$target" rev-parse HEAD)" = "$CHAMPSIM_COMMIT"
git -C "$target" apply --check "$package_root/champsim/patches/bimodal-confidence.patch"
git -C "$target" apply "$package_root/champsim/patches/bimodal-confidence.patch"
mkdir -p "$target/branch/wob"
cp "$package_root/champsim/branch/wob/wob.h" "$target/branch/wob/"
cp "$package_root/champsim/branch/wob/wob.cc" "$target/branch/wob/"
cp "$package_root/champsim/config/wob.json" "$target/wob.json"
cp "$package_root/champsim/config/a_only.json" "$target/a_only.json"
cp "$package_root/champsim/config/b_only.json" "$target/b_only.json"
echo "Installed WOB, A_ONLY and B_ONLY configurations into $target"
