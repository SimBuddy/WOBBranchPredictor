# ChampSim software/reference implementation

Target: `https://github.com/ChampSim/ChampSim.git`, commit `51588e1d6f97875fe8de1a3621d28668bff83fcf`.

Prerequisites are Git, Python 3, a C++20 compiler, Make, CMake, and the standard ChampSim/vcpkg dependencies.

```bash
git clone https://github.com/ChampSim/ChampSim.git ChampSim
git -C ChampSim checkout 51588e1d6f97875fe8de1a3621d28668bff83fcf
git -C ChampSim submodule update --init --recursive
bash champsim/scripts/install_into_champsim.sh ChampSim
bash champsim/scripts/build.sh ChampSim wob
```

Use `a_only` or `b_only` instead of `wob` for those modes. The resulting executables are `bin/champsim-wob`, `bin/champsim-a-only`, and `bin/champsim-b-only` inside the checkout.

Smoke example:

```bash
ChampSim/bin/champsim-wob --hide-heartbeat --warmup-instructions 0 \
  --simulation-instructions 10000 /path/to/legal-small-trace.xz
```

No trace is included.
