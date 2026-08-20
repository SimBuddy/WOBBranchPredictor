# Clean-room VexRiscv report

- Pinned repository: `https://github.com/SpinalHDL/VexRiscv.git`
- Pinned commit: `680756065e9e6fc50d8c3d6c58191a16e867d822`
- Clean cloned checkout: `/opt/wob-hard-gate-vex-build-20260820/VexRiscv`
- Isolated package copy: `/opt/wob-hard-gate-vex-package-20260820/vexriscv`
- Generated output directory: `/opt/wob-hard-gate-vex-rtl-20260820`

Command:

```bash
WOB_BUILD_ROOT=/opt/wob-hard-gate-vex-build-20260820 \
VEXRISCV_SOURCE=/opt/VexRiscv \
WOB_RTL_OUTPUT=/opt/wob-hard-gate-vex-rtl-20260820 \
bash ./vexriscv/scripts/generate-rtl.sh
```

The script used `git clone --no-hardlinks`, checked the pinned commit, copied only the five packaged Scala sources, and elaborated BTFNT, A_ONLY, B_ONLY, and WOB. Java was OpenJDK 17.0.19; the host sbt launcher reported 2.0.6, while the pinned upstream build definition selects sbt 1.6.0, Scala 2.12.18, and SpinalHDL 1.13.0.

Regenerated RTL SHA-256:

- A_ONLY: `b3bed3e4649de2eb0f8446723e183f385a154572a071d8a27932165546f85bfb`
- B_ONLY: `c71aa368f983cdea7159b50ef7afea101cc2725645fc76354cf43a3f30142b22`
- BTFNT: `e4fe64b4fd722a813784a111442b7dec100ed53051f60d3ca5b6d40a9b29d311`
- WOB: `ade52e63d38e63091828e9892a60a5504da9a99d17a282c5fdf3fbcef58db219`

All hashes match the frozen accepted values. Build-log dependency scan found no original-workspace, mounted-source, Windows-path, or research-directory reference.

**VEXRISCV CLEANROOM: PASS**
