# Provenance

## Upstream components

- ChampSim framework and bimodal module: official ChampSim repository at the pinned commit; Apache-2.0 with the project's hardware exception.
- ChampSim perceptron module: the pinned upstream source credits Daniel A. Jiménez and Calvin Lin and cites their HPCA 2001 perceptron work. It is consumed from upstream, not copied here.
- VexRiscv framework: official SpinalHDL/VexRiscv repository at the pinned commit; MIT.
- Bimodal and perceptron predictor concepts are established upstream concepts; this package does not claim authorship of them.

## WOB modification

WOB additions are the EC state, `rawA == 0 && EC >= 3` selector/controller, selective B consultation/training, and B-history maintenance on bypass. The ChampSim integration composes upstream A and B and adds the minimal raw-A accessor.

## Final integration

The VexRiscv implementation realizes the same algorithm in a pipeline with A/EC distributed-memory forms, pending logical-update forwarding, duplicated synchronous B memories, current-state forwarding, history snapshots, precise updates, and physical B-read gating. These are hardware integration mechanisms rather than new upstream predictor concepts.

No owner-selected licence for the WOB-authored additions was found. Public redistribution remains blocked until the rights holder supplies one.
