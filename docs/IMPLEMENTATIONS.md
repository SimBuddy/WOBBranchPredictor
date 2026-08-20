# Implementation distinction

The ChampSim subtree is a sequential software/simulation reference. It composes the pinned upstream bimodal and perceptron modules and implements EC, selection, selective B training, and B-history maintenance.

The VexRiscv subtree is synthesizable, pipeline-oriented SpinalHDL. It implements the same selector while preserving exact timing and memory-port behavior. A uses asynchronous-read/single-write distributed-memory organization with a 13-bit pending early logical update. EC is two-bit state. B uses duplicated synchronous prediction/training memories, history snapshots, exact signed saturation, and newest-current-state forwarding; its prediction reads are physically gated on bypass.

The duplicated B memories and forwarding structures are hardware implementation details, not a change to the WOB selector principle.
