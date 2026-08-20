#!/usr/bin/env bash
set -euo pipefail

package_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
source "$package_root/vexriscv/config/upstream.env"
build_root="${WOB_BUILD_ROOT:-$package_root/.build}"
upstream="$build_root/VexRiscv"
output="${WOB_RTL_OUTPUT:-$package_root/vexriscv/generated}"

if [[ -e "$upstream" ]]; then
  echo "build checkout already exists: $upstream" >&2
  exit 2
fi
mkdir -p "$build_root" "$output"

if [[ -n "${VEXRISCV_SOURCE:-}" ]]; then
  git clone --quiet --no-hardlinks "$VEXRISCV_SOURCE" "$upstream"
else
  git clone --quiet "$VEXRISCV_REPOSITORY" "$upstream"
fi
git -C "$upstream" checkout --quiet "$VEXRISCV_COMMIT"
mkdir -p "$upstream/src/main/scala/vexriscv/wob"
cp "$package_root/vexriscv/src/main/scala/vexriscv/wob/"*.scala "$upstream/src/main/scala/vexriscv/wob/"

generate() {
  local main_class=$1 output_name=$2
  (cd "$upstream" && sbt --error "runMain $main_class")
  cp "$upstream/VexRiscv.v" "$output/$output_name"
}
generate vexriscv.wob.GenBaseline VexRiscv_btfnt.v
generate vexriscv.wob.GenWobAOnly VexRiscv_aonly.v
generate vexriscv.wob.GenWobBOnly VexRiscv_bonly.v
generate vexriscv.wob.GenWobFull VexRiscv_wob.v
sha256sum "$output"/*.v
