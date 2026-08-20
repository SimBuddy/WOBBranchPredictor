#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "usage: $0 /path/to/installed/ChampSim {wob|a_only|b_only}" >&2
  exit 2
fi

target=$(cd "$1" && pwd)
case "$2" in
  wob) config=wob.json; binary=champsim-wob ;;
  a_only) config=a_only.json; binary=champsim-a-only ;;
  b_only) config=b_only.json; binary=champsim-b-only ;;
  *) echo "unknown mode: $2" >&2; exit 3 ;;
esac

if [[ ! -x "$target/vcpkg/vcpkg" ]]; then
  (cd "$target" && ./vcpkg/bootstrap-vcpkg.sh -disableMetrics)
fi
if [[ ! -d "$target/vcpkg_installed" ]]; then
  (cd "$target" && ./vcpkg/vcpkg install)
fi

(cd "$target" && ./config.sh "$config" && make -j"${JOBS:-2}")
test -x "$target/bin/$binary"
echo "$target/bin/$binary"
