# Roadmap

One milestone per Claude Code session. Each milestone is developed on a feature branch and merged via PR. Each ends with a working build, passing tests, and a PR for review. Don't start the next milestone until the acceptance criteria are met.

## M0: Scaffold (done)
Goal: project structure in place and verified.
- `sim`, `core`, `lwjgl3` modules wired; `sim` has no libGDX dependency; `core` depends on `sim`.
- JUnit 5 set up; one trivial sim test passes via `./gradlew test`.
- Window opens, clears to a solid color, shows FPS.
- `CLAUDE.md` and `docs/` in place; `.gitignore` correct; first commit.
  Acceptance: `./gradlew build` and `./gradlew test` pass; `./gradlew lwjgl3:run` opens a window.

## M0.5: CI and quality gates
Goal: automated checks on every PR.
- GitHub Actions workflow running `./gradlew build` on PRs and pushes to `main`.
- Spotless (google-java-format) and Checkstyle wired into `build`.
- Local pre-commit hook refusing commits on `main`.
- `main` branch protection requires the CI check (configured in GitHub after the first CI run).
  Acceptance: a PR shows a passing `build` check; a mis-formatted file fails the build.

## M1: Land render
Goal: see the land with no water.
- Map data model and JSON loader in `sim` (format in design.md), with loader tests including error cases.
- A few hand-written maps (flat, ramp/stairs, bowl, void island) under `core` assets or `sim` test resources.
- `Tileset` interface and code-generated `RoygbivTileset`.
- Renderer with camera culling; void drawn black.
- Camera pan and zoom with clamping; map switching (key or CLI arg).
- Perf HUD (FPS).
  Acceptance: each test map loads and renders correctly; camera clamps; loader tests pass.

## M2: Test map library
Goal: a stable set of predictable maps for physics work.
- Maps with obvious expected behavior: bowl, slope, channel, dam with spillway, two basins connected by a channel, staircase, void-walled arena, a large (e.g. 256x256) map for perf.
- A tiny map-authoring convention documented in `docs/`.
- Optional: simple debug overlay showing the tile under the cursor (land, water, surface).
  Acceptance: maps documented with the expected flow outcome for each.

## M3: Water flow
Goal: pre-existing water flows predictably.
- `World` tick in `sim` with the pass pipeline, double-buffered flow, neighbor table (8 default, 4 switchable).
- Fixed-timestep loop in `core`; pause, single-step, speed controls.
- Water overlay (alpha by depth) and debug depth overlay toggle.
- Sim tests: mass conservation, non-negativity, void blocking, determinism, symmetry, equilibrium in a closed basin.
- Perf HUD adds sim ms per tick.
  Acceptance: all invariants pass; bowl and channel maps behave as documented in M2; no visible oscillation.

## M4: Sources and drains
Goal: water enters and leaves.
- Map format fills in `sources` and `drains` (x, y, rate).
- Source and drain passes in the tick pipeline.
- Render markers for sources and drains.
- Tests: conservation accounting (water in = water out + stored change), equilibrium where source rate equals drain capacity.
  Acceptance: a source plus a drain reaches a steady state on a test map.

## M5: Terraforming
Goal: reshape the land.
- Click to remove or place one chunk (+1 / -1 height), clamped to 1..7, void untouchable.
- Timed action with all-or-nothing completion (cancel = nothing happens).
- Decide the water-on-placed-land behavior (see open questions in design.md).
- Tests for clamping, void protection, and interrupted actions.
  Acceptance: you can dam and divert a stream by hand.

## Later
Shading at height edges, textured tilesets (winter/spring), active-cell optimization, multithreading, terraformer unit with carried soil, richer controls, erosion.
