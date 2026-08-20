# ChampSim pull-request notes

Target baseline: ChampSim commit `51588e1d6f97875fe8de1a3621d28668bff83fcf`.

Likely additions are `branch/wob/wob.h` and `branch/wob/wob.cc`. The likely upstream modification is the small raw-state accessor in `branch/bimodal/bimodal.h`. This ChampSim version discovers branch modules from their directory/configuration name, so no separate registration table is required. A configuration selects `"branch_predictor": "wob"`.

Minimal validation:

```bash
bash champsim/scripts/install_into_champsim.sh /path/to/ChampSim
bash champsim/scripts/build.sh /path/to/ChampSim wob
/path/to/ChampSim/bin/champsim-wob --warmup-instructions 0 \
  --simulation-instructions 10000 /path/to/small-trace.xz
```

The module relies on public history fields in the pinned `perceptron` API. A future PR targeting another ChampSim revision must recheck that compatibility. No PR has been submitted.
