# Clean-room ChampSim report

- Pinned repository: `https://github.com/ChampSim/ChampSim.git`
- Pinned commit: `51588e1d6f97875fe8de1a3621d28668bff83fcf`
- Clean checkout: `/opt/wob-hard-gate-champsim-20260820`
- Isolated package copy: `/opt/wob-hard-gate-package-20260820/champsim`
- Files added: `branch/wob/wob.h`, `branch/wob/wob.cc`, `wob.json`, `a_only.json`, `b_only.json`; `branch/bimodal/bimodal.h` received the packaged raw-state accessor patch.

Commands used:

```bash
git clone https://github.com/ChampSim/ChampSim.git /opt/wob-hard-gate-champsim-20260820
git -C /opt/wob-hard-gate-champsim-20260820 checkout 51588e1d6f97875fe8de1a3621d28668bff83fcf
git -C /opt/wob-hard-gate-champsim-20260820 submodule update --init --recursive
bash champsim/scripts/install_into_champsim.sh /opt/wob-hard-gate-champsim-20260820
bash champsim/scripts/build.sh /opt/wob-hard-gate-champsim-20260820 wob
bash champsim/scripts/build.sh /opt/wob-hard-gate-champsim-20260820 a_only
bash champsim/scripts/build.sh /opt/wob-hard-gate-champsim-20260820 b_only
```

Build result: PASS. Executables produced:

- `/opt/wob-hard-gate-champsim-20260820/bin/champsim-wob`
- `/opt/wob-hard-gate-champsim-20260820/bin/champsim-a-only`
- `/opt/wob-hard-gate-champsim-20260820/bin/champsim-b-only`

Smoke command:

```bash
/opt/wob-hard-gate-champsim-20260820/bin/champsim-wob \
  --hide-heartbeat --warmup-instructions 0 --simulation-instructions 10000 \
  /tmp/wob-hard-gate-smoke.trace.xz
```

The external synthetic trace was not packaged. Exit status was 0. The run terminated normally at 10,000 instructions and 36,146 cycles, IPC 0.2767, branch-prediction accuracy 66.24%, and MPKI 337.6. Build warnings: none.

Dependency scan: PASS. Build logs and generated configuration were scanned for the original workspace name, Windows paths, WSL-mounted source paths, and research-directory names. No hit was found. The original research tree supplied discovery evidence and the initial package assembly, but it was not referenced by the clean checkout's compiler, configuration, includes, or executable.

**CHAMPSIM CLEANROOM: PASS**
