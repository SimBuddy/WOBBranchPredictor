# VexRiscv/SpinalHDL hardware implementation

Target: `https://github.com/SpinalHDL/VexRiscv.git`, commit `680756065e9e6fc50d8c3d6c58191a16e867d822`. The pinned build uses Scala 2.12.18, sbt 1.6.0, and SpinalHDL 1.13.0.

With Git, a compatible JDK, and sbt installed:

```bash
WOB_BUILD_ROOT=/tmp/wob-build bash vexriscv/scripts/generate-rtl.sh
```

An existing clean upstream checkout can be reused without modifying it:

```bash
WOB_BUILD_ROOT=/tmp/wob-build \
VEXRISCV_SOURCE=/path/to/VexRiscv \
bash vexriscv/scripts/generate-rtl.sh
```

The script clones the upstream source into its build tree, pins the commit, copies only the five packaged Scala files, and generates BTFNT, A_ONLY, B_ONLY, and WOB Verilog. SpinalHDL source is canonical; generated RTL is deliberately excluded and its accepted hashes are recorded in the clean-room report.
