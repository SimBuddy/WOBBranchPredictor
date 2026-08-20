# Package manifest

| Path/group | Purpose | Origin / status |
|---|---|---|
| `README.md`, `.gitignore` | Root description and artifact exclusions | Final integration documentation |
| `PROVENANCE.md`, `LICENSE_NOT_INCLUDED.md` | Attribution and unresolved WOB licence status | Final audit documentation |
| `LICENSES/ChampSim-Apache-2.0.txt` | ChampSim upstream terms | Unmodified upstream licence |
| `LICENSES/VexRiscv-MIT.txt` | VexRiscv upstream terms | Unmodified upstream licence |
| `GITHUB_CLEANROOM_CHAMPSIM_REPORT.md` | Decisive software build/smoke evidence | Generated validation report |
| `GITHUB_CLEANROOM_VEXRISCV_REPORT.md` | Hardware elaboration/hash evidence | Generated validation report |
| `GITHUB_PACKAGE_MANIFEST.md` | Complete file inventory | Generated package documentation |
| `GITHUB_PACKAGE_AUDIT.md` | Cleanliness and release verdict | Generated validation report |
| `SHA256SUMS.txt` | Integrity manifest for all other tree files | Generated checksum file |
| `docs/ALGORITHM.md` | Final algorithm behavior | Final integration documentation |
| `docs/IMPLEMENTATIONS.md` | Simulator/hardware distinction | Final integration documentation |
| `docs/CHAMPSIM_PR_NOTES.md` | Prospective upstream integration notes | Final integration documentation |
| `champsim/README.md` | Pinned build/use instructions | Final integration documentation |
| `champsim/branch/wob/wob.h` | A/B composition, EC and pairing state | Cleaned final WOB software source |
| `champsim/branch/wob/wob.cc` | Selector, updates, selective B training/history maintenance | Cleaned final WOB software source |
| `champsim/patches/bimodal-confidence.patch` | Exposes upstream raw A state | Minimal WOB patch to Apache-licensed source |
| `champsim/config/upstream.env` | ChampSim URL and commit | Integration metadata |
| `champsim/config/wob.json` | WOB build mode | Integration metadata |
| `champsim/config/a_only.json` | A_ONLY build mode | Integration metadata |
| `champsim/config/b_only.json` | B_ONLY build mode | Integration metadata |
| `champsim/scripts/install_into_champsim.sh` | Validates pin and installs minimal files | Integration support |
| `champsim/scripts/build.sh` | Configures and builds each mode | Integration support |
| `vexriscv/README.md` | Pinned elaboration instructions | Final integration documentation |
| `vexriscv/config/upstream.env` | VexRiscv URL and commit | Integration metadata |
| `vexriscv/scripts/generate-rtl.sh` | Clean clone and four-mode elaboration | Final integration support |
| `vexriscv/src/main/scala/vexriscv/wob/WobPlugin.scala` | Final production hardware predictor | Canonical accepted WOB hardware source |
| `vexriscv/src/main/scala/vexriscv/wob/GenBaseline.scala` | BTFNT generator | Canonical integration source |
| `vexriscv/src/main/scala/vexriscv/wob/GenWobAOnly.scala` | A_ONLY generator | Canonical integration source |
| `vexriscv/src/main/scala/vexriscv/wob/GenWobBOnly.scala` | B_ONLY generator | Canonical integration source |
| `vexriscv/src/main/scala/vexriscv/wob/GenWobFull.scala` | WOB generator | Canonical integration source |

No generated RTL, binary, trace, workload, experiment result, log, or upstream project tree is included.
