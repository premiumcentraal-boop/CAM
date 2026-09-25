# CAM — Camera-Spoof Engine Build Bible
**Repo:** https://github.com/premiumcentraal-boop/CAM
**Version:** 1.0 — planning document, pre-green-light
**Method:** strictly linear — each Run builds on the previous Run's verified output. No Run starts until the prior Run's acceptance test passes and is pushed to GitHub.

---

## 0. Product Definition

One LSPosed-rooted Android library + companion tooling that makes any app — including
KYC/liveness SDKs — see a fully consistent, persona-controlled camera:

- **Feed source:** per-persona video/image library with procedural sensor-noise injection
- **Hook surface:** Camera1 API, Camera2, CameraX, MediaCodec/MediaRecorder, WebRTC bridge
- **Meta Engine:** `CameraCharacteristics` spoofed from real device dumps (bit-coherent with persona)
- **Structural tier:** v4l2loopback HAL input for self-controlled ROMs (redroid fleet)
- **Persona rule:** one `persona.json` drives feed + metadata + EXIF + GPS + device model atomically

**Target platforms:** Android 11 → 15 (API 30–35), arm64-v8a.
**Non-goals (v1):** depth/LiDAR liveness bypass, iOS, non-rooted devices.

---

## 1. The Linear Build — 12 Runs

Each Run lists: purpose, what gets built, estimated size (LOC = lines of code,
"runs" = estimated build/test cycles before acceptance), and the acceptance gate
that must pass before the next Run starts.

---

### RUN 0 — Repo Skeleton & CI (est. 3 runs, ~400 LOC)
**Builds:** repo structure, Gradle multi-module project, CI pipeline, test harness skeleton.
```
CAM/
├── settings.gradle.kts
├── app-demo/                 # test app that exercises every camera path
├── cam-core/                 # persona model + feed service (Java/Kotlin)
├── cam-xposed/               # LSPosed hook module
├── cam-meta/                 # CameraCharacteristics meta engine
├── cam-farm/                 # redroid/v4l2loopback provisioning (Python)
├── personas/                 # persona.json schemas + samples
├── device-dumps/             # real device camera dumps (git-LFS)
├── tests/                    # probe suite + stream forensics
└── docs/                     # this bible + runbooks
```
**Acceptance:** `./gradlew assembleDebug` + CI green on push; demo app installs and
shows a live preview from the REAL camera (baseline capture path working).

---

### RUN 1 — Persona Schema & Generator (est. 2 runs, ~600 LOC)
**Builds:** `personas/schema.json` + `cam-core` persona loader.
Fields: device model, Android version, patch level, sensor array size, orientation,
focal lengths, max JPEG res, FPS ranges, capability flags, GPS, lens info,
feed file paths, noise seed, EXIF template.
Generator CLI (`cam-core` tools): produce a valid persona for any (device model, Android version)
pair from a device dump.
**Acceptance:** generator emits schema-valid persona; loader round-trips it;
coherence assertions pass (feed res ≤ claimed sensor capability).

---

### RUN 2 — Feed Service (est. 4 runs, ~1,200 LOC)
**Builds:** `cam-core` feed engine — decodes persona video/image loops, outputs frames:
- frame pacing follows requested capture FPS (not video-native rate)
- procedural per-frame noise (luminance grain, exposure micro-variation, noise seed)
- EXIF stamping for still captures (model, lens, GPS from persona)
- no two frames byte-identical across any 10 sessions (hash-test)
**Acceptance:** deterministic test — 10 sessions of N frames, all byte-distinct,
correct EXIF, monotonic timestamps. Runs as a plain JVM library (no Android yet).

---

### RUN 3 — Camera1 Hook (est. 3 runs, ~800 LOC)
**Builds:** `cam-xposed` module part 1 — legacy `android.hardware.Camera` (API 1):
`open()`, `setPreviewDisplay`, `setPreviewTexture`, `takePicture` paths fed from Feed Service.
**Rationale for doing this ancient API first:** most KYC SDKs still use it; it's the
smallest surface; proves the hook→feed pipeline end-to-end.
**Acceptance:** demo app `Camera1Activity` shows persona feed instead of real camera;
still capture produces EXIF-stamped persona image.

---

### RUN 4 — Camera2 Hook (est. 6 runs, ~2,000 LOC) ⚠ largest run
**Builds:** `cam-xposed` part 2 — full `camera2` surface:
`CameraManager` (device enumeration filtered to persona cameras), `CameraCharacteristics`
served from Meta Engine, `CameraCaptureSession` frame replacement, `ImageReader` /
`SurfaceTexture` re-routing, capture-request FPS negotiation.
**Acceptance:** demo `Camera2Activity` + Google Camera-style test app show persona feed;
`CameraCharacteristics` probe suite (tests/) matches device dump bit-for-bit;
preview and capture paths agree.

---

### RUN 5 — CameraX + MediaCodec/MediaRecorder + WebRTC bridge (est. 5 runs, ~1,500 LOC)
**Builds:** `cam-xposed` part 3 — `androidx.camera.*` interception (CameraX sits on
Camera2 but adds its own use-case plumbing), video encode paths, WebRTC JNI bridge check.
**Acceptance:** CameraX demo app records persona video via MediaRecorder;
a video-call test app (Jitsi-class) shows persona feed in both directions.

---

### RUN 6 — Meta Engine Full (est. 3 runs, ~1,000 LOC)
**Builds:** `cam-meta` — real device dump ingestion pipeline (public device trees from
GitHub/XDA → normalized JSON), consistency assertions, multi-camera (physical ID) support,
per-Android-version behavior matrix (11→15).
**Acceptance:** for 5 reference devices: probe suite passes on all; CI runs the
matrix on every push.

---

### RUN 7 — KYC Acceptance Tests (est. 5 runs, test-only + fixes)
**Builds:** no new features — this is the reliability gauntlet:
- 3 KYC vendor demo apps (Onfido/Veriff/Jumio-class) pass document+liveness demo flows
- emulator-detection SDK sweep (RootBeer-class, Play-services checks, vendor SDKs)
- stream forensics: byte-distinctness, timestamp monotonicity, EXIF validity
Fixes land as patches to Runs 3–5 hooks. This run "completes" when all vendors pass.
**Acceptance:** all three KYC demo flows pass 10/10 runs on Android 12–14 devices.

---

### RUN 8 — Android 14/15 Hardening (est. 4 runs, ~800 LOC)
**Builds:** version-specific paths — vendor extension emulation (low-light/bokeh
extension queries return persona-consistent availability), attestation coherence with
TrickyStore (integrity verdict and camera persona agree), API 35 edge cases.
**Acceptance:** probe suite green on Android 15 emulator + one real 15 device;
TrickyStore + CAM active together without Play Integrity regression.

---

### RUN 9 — v4l2loopback HAL Tier (est. 4 runs, ~600 LOC Python + configs)
**Builds:** `cam-farm` — redroid provisioning pipeline:
- `v4l2loopback` device fed by ffmpeg from persona library
- HAL config with persona camera characteristics baked in
- orchestrator script: persona → provision container → boot → verify → snapshot → halt
**Acceptance:** one redroid container passes the full probe suite with NO LSPosed
module installed (structural, not hooked). Second container proves reproducibility
with a different persona.

---

### RUN 10 — Orchestrator Integration (est. 4 runs, ~1,500 LOC Python)
**Builds:** the glue — `cam-farm` CLI:
`cam create-persona` → `cam provision <persona> --target emulator|device` →
`cam verify` (runs full probe suite) → `cam heartbeat <task>` (scheduled sessions).
Persona DB (SQLite) with device/proxy/feed bindings; atomic provisioning =
one command spins a complete identity (ROM props + hooks + feed + proxy).
**Acceptance:** `cam create-persona && cam provision && cam verify` runs end-to-end
unattended; 10 personas provisioned sequentially without collision.

---

### RUN 11 — Fleet Hardening & Docs (est. 5 runs, ~500 LOC + docs)
**Builds:** retry/telemetry in the incident loop, per-persona risk scoring,
automated regression matrix (all reference devices × all Android versions),
full documentation: setup runbook, persona authoring guide, KYC test protocol.
**Acceptance:** 24-hour unattended soak test — 10 personas × 4 sessions each,
zero incidents, zero cross-persona leakage. Tag v1.0.0.

---

## 2. Cumulative Effort Estimate

| Metric | Estimate |
|---|---|
| Total runs (build/test cycles) | **~40–45** |
| Total code | **~10,000 LOC** (Kotlin/Java ~6k, Python ~2.5k, infra ~1.5k) |
| Calendar (1 strong Android dev) | **12 weeks** linear |
| Peak-risk runs | Run 4 (Camera2) and Run 7 (KYC acceptance) — budget 2× if they stall |
| Maintenance after v1 | ~1 week per Android release |

## 3. Git Discipline

- One branch per Run: `run-3-camera1-hook` → PR → merge to `main` only when acceptance passes
- Every push to `main` is green CI; tags `v0.1` per Run
- Device dumps in Git-LFS; persona videos never in git (local + encrypted backup)

## 4. Definition of Done (v1.0)

1. All 12 runs merged, CI green
2. 3 KYC vendor demos pass 10/10
3. Probe suite bit-exact on 5 reference devices × Android 11–15
4. One redroid container provisioned end-to-end from a single `cam` command
5. This bible updated with actuals per run

---

*End of bible. Green light required before Run 0 begins.*
