# Run 0 Notes — Actuals & Decisions

## What was built
- Multi-module Gradle project: `app-demo`, `cam-core`, `cam-xposed` (placeholder), `cam-meta`, `cam-farm` (README)
- Demo app with 4 surfaces: Camera1, Camera2, CameraX, Probe (characteristics dumper)
- `cam-core`: Persona data class (schema v0), JSON loader, coherence validator
- `cam-core`: `FrameSource` interface — THE seam Runs 2–5 plug into
- `cam-meta`: `CameraSpecSource` interface — seam for Run 6 Meta Engine
- Probe JSON schema established (`tests/probe/ProbeSchema.md`)
- CI: two jobs (full Android build; fast JVM-only cam-core test)

## Key decisions
1. **minSdk 30 (Android 11)** — project scope starts there; API surface of Camera2 we depend on is stable from 30.
2. **Camera2 fragment uses TextureView** not SurfaceView — keeps the surface in-process, which the Run 4 hook needs when it swaps the frame producer.
3. **Choke-points documented in Camera2Fragment** — Run 4 must only intercept `openCamera()` and frame-arrival; enforced by code comments + review.
4. **LSPosed dependency deliberately NOT added yet** (Run 3) — the module compiles standalone now so CI stays green before hooks exist.
5. **Placeholder `SimpleSpecSource` returns null** — honest about scope; Run 6 replaces it. Contract is final.

## Deviations from plan
- `org.json` used instead of Moshi/kotlinx — zero-config on Android, zero-reflection, fine for v0 schema.

## Next: Run 1 (persona generator CLI)
