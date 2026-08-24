# WOB Predictor

This repository tree contains two final implementations:

1. [ChampSim software/reference implementation](champsim/README.md)
2. [VexRiscv/SpinalHDL hardware implementation](vexriscv/README.md)

**WOB is an experimental methodology**. Its goal is to provide a novel means of refactoring source code for data intensive applications, and increase their effectiveness.

**WOB Compared with Conventional Refactoring**
Traditional refactoring improves code structure and maintainability.
Performance optimization finds slow parts and makes them faster.
Profile-guided optimization measures where execution time is being spent and concentrates effort there.
Adaptive systems vary behaviour depending on conditions, usually within a specific mechanism.


**WOB takes a broader system-level view:**
Is this processing justified here, at this level, and in this form?
A conventional optimizer might find an expensive function and make it faster.
WOB may instead discover that the larger system can avoid, reduce, defer, reuse, reorder, or selectively deepen that processing.
The distinction is:
Traditional optimization improves execution. WOB reconsiders how much processing should happen, where, and when.
WOB can refactor code that has been already been conventionally refactored, because it is doing something entirely different. 



**EXPERIMENT 1** - In the experiment below, WOB is being applied to bimodal and perceptron caching algorithms found in moderns CPUs.

WOB treats the caching computation itself as a resource to be allocated selectively. Instead of running a complex predictor for every branch instruction, WOB first uses a cheaper predictor and a small amount of state to decide whether the more expensive predictor is likely to be worth invoking.

In a simulated hardware implementation based on a bimodal predictor paired with a perceptron predictor, the approach substantially reduced memory activity and arithmetic work while preserving the validated behaviour of the original prediction system.

The final implementation performed 3.43 million fewer memory reads and 2.01 million fewer physical memory writes than an exact version that used the perceptron predictor continuously. It also avoided more than 97,000 complete perceptron dot-product evaluations and more than 2.3 million associated arithmetic operations across the tested workloads.

**The savings were not limited to software simulation.**

The design was implemented using native FPGA memory blocks, and all 25 block-memory units used for expensive prediction were physically controlled by the WOB selection signal. When WOB methodology-code determined that the expensive predictor was unnecessary, the corresponding block-memory reads were disabled at the hardware enable pins.  The methodology extended itself into the actual hardware implementation.

This distinction matters because many apparent computational savings disappear when translated into hardware. A design may skip using a result while the underlying memory and logic continue switching internally. In the WOB implementation, the expensive memory access itself was suppressed.

**Allocating computation rather than replacing the predictor**

The perceptron predictor used in the experiment resembles a small machine-learning model. It combines a history vector with learned signed weights and computes a dot product to decide whether a branch is likely to be taken.

WOB does not attempt to replace this predictor with a cheaper one. Instead, it asks a different question: does the expensive predictor need to run on this particular branch?
A small selector based on the state of the cheap predictor and a compact error counter determines when the perceptron predictor should be consulted.
This makes WOB closer to a computation-allocation system than a new prediction model.

**Exact behaviour preserved**

One of the main challenges was ensuring that reduced computation did not introduce hidden changes in predictor state. During development, several apparently successful implementations were rejected after detailed testing showed differences that were invisible in headline accuracy figures.

The final design was required to match branch-by-branch selector behaviour, predictor state, training events and final memory contents.
The completed implementation recorded zero selector mismatches across 147,894 tested events. It also passed 128 exhaustive local-state cases, matched 24 final mode-and-workload state comparisons, reproduced the full 32-row regression matrix and remained deterministic across repeated runs.

**Prediction latency was unchanged.**

The final design also retained 22 additional flushes associated with genuine bypass behavior rather than attempting to hide that cost with speculative or warming mechanisms.

**Hardware overhead reduced**

An important question was whether the machinery required to decide when to skip computation would itself become expensive.
A related technique, called Nitpicker, was used to reduce this overhead without altering WOB's decision policy.

Nitpicker examines the implementation for state, logic and distinctions that are unnecessary for exact behaviour. In this case, much of the apparent WOB overhead was found to result from inefficient hardware mapping rather than from the WOB principle itself.

The WOB implementation was reduced from 11,535 to 2,814 FPGA lookup tables and from 5,539 to 1,466 flip-flops. This corresponds to reductions of approximately 76% and 74%, respectively. After these changes, the genuine WOB hardware premium over the exact always-on perceptron design was 467 lookup tables, 31 flip-flops and 128 distributed-memory primitives, with no additional large block memories. The optimized WOB design reached 13.15 MHz compared with 13.77 MHz for the always-on perceptron version in the fixed-seed physical implementation. Across five matched placement seeds, the median performance gap was 3.52%.
Most of the remaining critical-path delay came from physical routing rather than from the selector logic itself.

**Power savings likely, but not yet measured**

The results provide strong evidence that WOB reduces physical computational activity, but they do not yet establish an electrical energy saving.
No sufficiently credible power-analysis flow was available that could model the specific FPGA memories, routing and switching activity used in the experiment. Direct board-level power measurement was also not completed.

As a result, the claim of a reduction in watts, joules or percentage energy consumption is not yet substantiated. However, the direction of the activity result is notable because WOB reduces memory reads, memory writes and arithmetic operations simultaneously. That makes a reduction in dynamic predictor power plausible, although the size of any real power saving remains to be measured. 

A rough engineering expectation is that branch-predictor dynamic power could fall substantially, potentially on the order of tens of percent, but that remains an estimate rather than an experimental result.

**A broader computing principle?**

The significance of the experiment may extend beyond branch prediction.

WOB is based on the idea that not all computation has equal marginal value. If a cheap mechanism can identify cases where additional work is unlikely to improve the result enough to justify its cost, that work can be selectively avoided.

The branch-prediction study provides a hardware demonstration of that principle: the expensive model remains available, but is used only where the selector determines that its additional computation is worthwhile.

The next major question is whether the same approach can produce similar gains in very different algorithms.
If it does, WOB could point toward a broader class of systems in which computation is not simply optimized for speed, but actively budgeted according to its expected usefulness.

See `docs/ALGORITHM.md` for final behavior and `docs/IMPLEMENTATIONS.md` for the distinction between simulator and hardware machinery. Research experiments, traces, workloads, results, and superseded designs are excluded.
