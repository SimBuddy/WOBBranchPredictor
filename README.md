# WOB Predictor

This repository tree contains two final implementations:

1. [ChampSim software/reference implementation](champsim/README.md)
2. [VexRiscv/SpinalHDL hardware implementation](vexriscv/README.md)

WOB uses a cheap bimodal predictor by default and selectively consults a perceptron predictor when `rawA == 0 && EC >= 3`.

See `docs/ALGORITHM.md` for final behavior and `docs/IMPLEMENTATIONS.md` for the distinction between simulator and hardware machinery. Research experiments, traces, workloads, results, and superseded designs are excluded.

Both implementations passed their clean-room technical gates. Public release remains blocked because no licence grant for WOB-authored code was found; see `LICENSE_NOT_INCLUDED.md`.
