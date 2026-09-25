# Run 0 — Repo Skeleton & Baseline Capture Path

**Goal:** lay the foundation that every future Run plugs into, and prove the *baseline*
(unspoofed) camera pipeline works end-to-end. When this run is done we have:
a buildable multi-module project, a demo app that enumerates devices and renders a live
preview from the real camera through every API surface we will later hook, a CI
pipeline, and a probe harness that will become our regression oracle.

**Design principle for Run 0:** every architectural seam that later Runs will
insert hooks into must already exist as an interface. The hook work in Runs 3–5
then becomes "implement the interface," not "restructure the app."

---

## Module Layout (built this run)

```
CAM/
├── settings.gradle.kts            # multi-module wiring
├── build.gradle.kts               # root build config
├── gradle.properties              # AndroidX, jetifier off, memory
├── app-demo/                      # demo app (baseline capture paths)
│   └── src/main/java/.../
│       ├── MainActivity.kt        # surface-flipping menu: API1 / API2 / CameraX / Probe
│       ├── Camera1Fragment.kt     # legacy Camera + SurfaceView + takePicture
│       ├── Camera2Fragment.kt     # Camera2 preview + ImageReader stills
│       ├── CameraXFragment.kt     # CameraX Preview + ImageCapture
│       └── ProbeFragment.kt       # dumps CameraCharacteristics to JSON
├── cam-core/                      # pure-Kotlin library (no Android deps in core data classes)
│   └── src/main/java/.../
│       ├── persona/Persona.kt           # data class (schema v0)
│       ├── persona/PersonaLoader.kt     # JSON ↔ Persona
│       ├── persona/PersonaValidator.kt  # coherence rules (Run 1 expands)
│       └── feed/FrameSource.kt          # interface: the seam Feed Service (Run 2) implements
├── cam-xposed/                    # placeholder module (manifest + empty module class)
├── cam-meta/                      # placeholder module (interface only)
│   └── .../CameraSpecSource.kt    # interface: seam Meta Engine (Run 6) implements
├── cam-farm/                      # Python placeholder (README only this run)
├── tests/
│   └── probe/ProbeSchema.md       # defines the JSON probe output format
├── .github/workflows/ci.yml       # build + unit tests on push/PR
└── docs/run0_notes.md             # actuals, deviations, decisions
```

## Key Interfaces Seeded (the puzzle-piece seams)

```kotlin
// FrameSource — Run 2 implements; Runs 3–5 consume via hooks.
interface FrameSource {
    fun start(request: FrameRequest)
    fun stop()
    fun latestFrame(): ByteArray?      // NV21/YUV_420_888-format frame
    fun frameMeta(): FrameMeta?        # ts, fps, dimensions, stride info
}

// CameraSpecSource — Run 6 (Meta Engine) implements; Run 4 hook consumes.
interface CameraSpecSource {
    fun specFor(personaId: String): CameraSpec?
}
```

Camera2 fragment structured so the hook (Run 4) can intercept at exactly two
choke-points: `CameraManager.openCamera()` and session/frame arrival. No other
refactor should be needed later.

## Scope guards (what we deliberately do NOT build this run)
- No hooking, no Xposed activation, no persona application — Run 3+
- No noise injection / EXIF — Run 2
- No farm scripting — Run 9
- Placeholder modules exist so CI wires up now and stays stable.

## Acceptance Gate (definition of done)
1. `./gradlew assembleDebug` green locally + CI green on push
2. Demo app: 4 screens (Camera1, Camera2, CameraX previews + Probe dump)
3. Probe screen emits schema-conformant JSON of all camera characteristics
4. Unit tests for Persona loader/validator green
5. All of it on `main` on GitHub, tagged `v0.1.0-run0`
