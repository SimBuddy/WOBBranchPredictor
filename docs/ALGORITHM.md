# Final WOB algorithm

WOB combines a cheap predictor A, a small error state EC, and an expensive predictor B.

- **A:** a PC-indexed two-bit saturating bimodal predictor. Raw states 0–1 predict not-taken; 2–3 predict taken.
- **EC:** a PC-indexed two-bit saturating counter, initially zero. It increments when A mispredicts a conditional branch and never decrements.
- **Selector:** B is consulted when `rawA == 0 && EC >= 3`. Otherwise A supplies the prediction.
- **B:** a perceptron predictor. The hardware form uses 24 history bits, signed eight-bit bias/weights, exact saturation, and threshold 60.

A updates normally. On conditional branches, B trains only when it was consulted under the final accepted software policy. When B is bypassed, its speculative and committed global histories still advance with the resolved outcome, without a perceptron-table access or update. Thus a later consultation observes the correct history stream.

A_ONLY runs bimodal alone, B_ONLY runs perceptron alone, and WOB applies the selector above.
