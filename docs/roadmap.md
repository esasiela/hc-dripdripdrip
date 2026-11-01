# Roadmap

Big themes only. Each theme is delivered as small stories, one PR each, tracked in `docs/backlog.md`. Rule: one story per PR (see `CLAUDE.md`, "Small PRs"). Each story ends with a green build and a PR for review. Don't start the next story until the previous one is merged.

## Done
- **M0 Scaffold.** `sim`, `core`, `lwjgl3` modules; `sim` has no libGDX dependency; JUnit 5; window with FPS.
- **M0.5 CI and quality gates.** GitHub Actions `build` job (required check on `main`), Spotless (google-java-format), Checkstyle, pre-commit hook blocking commits on `main`.
- **M1 Land render.** Map model and JSON loader with tests, `Tileset` interface with a code-generated ROYGBIV tileset, camera pan/zoom with clamping, HUD, and test maps (flat, stairs, bowl, void-island, non-square, large 128x128).

## Now: CI readability and viewing aids
Make the screen easy to read before adding water.
- Readable CI output.
- Grid wireframe, then a settings screen to toggle it.
- Then: hover highlight, map border with camera padding, cheap edge shading, richer cursor inspector.

## Next: water display and scenario maps
- Render water from the map's `water` layer with no physics, including showcase maps at different depths.
- A library of tiny scenario maps with documented expected flow outcomes (the acceptance targets for flow).

## Then: flow
Pre-existing water flows predictably.
- `World` tick in `sim` with a pass pipeline, double-buffered flow, neighbor table (8 default, 4 switchable).
- Fixed-timestep loop in `core`; pause, single-step, speed controls.
- Water changing over time on screen; sim ms per tick in the HUD.
- Tests: mass conservation, non-negativity, void blocking, determinism, symmetry, equilibrium in a closed basin.
  Acceptance: all invariants pass; scenario maps behave as documented; no visible oscillation.

## Then: sources and drains
- Map format fills in `sources` and `drains` (x, y, rate); source and drain passes in the tick pipeline; markers on screen.
- Tests: conservation accounting, steady state where source rate equals drain capacity.

## Then: terraforming
- Click to remove or place one chunk (+1 / -1 height), clamped to 1..7, void untouchable.
- Timed action with all-or-nothing completion (cancel means nothing happens).
- Decide the water-on-placed-land behavior (see open questions in `docs/design.md`).
  Acceptance: you can dam and divert a stream by hand.

## Later
Textured tilesets (winter/spring), active-cell optimization, multithreading, terraformer unit with carried soil, richer controls, erosion.
