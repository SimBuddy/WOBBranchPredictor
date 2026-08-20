# Package audit

## Technical gates

- Actual final ChampSim WOB source identified from `cfg_A10_perc.json` and `wob_a10`: PASS.
- Exact ChampSim baseline recovered: PASS.
- Clean ChampSim WOB/A_ONLY/B_ONLY compilation: PASS.
- ChampSim WOB smoke execution: PASS.
- ChampSim hidden dependency scan: PASS.
- Final VexRiscv production source present and structurally re-audited: PASS.
- Clean VexRiscv four-mode elaboration: PASS.
- Frozen generated-Verilog hashes: PASS, four of four exact matches.
- Software and hardware roles documented distinctly: PASS.

## Cleanliness scan

No Git metadata, build products, traces, workloads, results, logs, route data, bitstreams, or superseded implementation directories are present. No Windows path, user profile, `/home/` path, secret-like assignment, checkpoint name, or research verdict was found.

The word scan for `stage` has only framework-language hits: VexRiscv's `Stageable` pipeline API and `cmdForkOnSecondStage` configuration option. These are required source identifiers, not research chronology.

The two clean-room reports necessarily record their `/opt/` clean-checkout locations because the requested report schema requires exact paths. Those paths are validation records, not build dependencies; all user-facing build scripts use relative package paths and caller-provided locations.

`.git` is absent. Generated RTL is consistently excluded; canonical SpinalHDL source and verified hashes are provided.

## Licence gate

Upstream ChampSim and VexRiscv licences are identified and preserved, and upstream algorithm authorship is attributed. No owner-selected licence grant exists for the WOB-authored controller and hardware sources. Therefore the licence/provenance release gate does not pass.

Per the hard-gate instruction for a non-ready result, no ZIP was created.

## Verdict

**LICENSE-PROVENANCE-BLOCKED**
