# Roadmap

Big themes only. Each theme is delivered as small stories, one PR each, tracked in `docs/backlog.md`. Rule: one story per PR (see `CLAUDE.md`, "Small PRs"). Each story ends with a green build and a PR for review. Don't start the next story until the previous one is merged.

## Done
- **M0 Scaffold.** `sim`, `core`, `lwjgl3` modules; `sim` has no libGDX dependency; JUnit 5; window with FPS.
- **M0.5 CI and quality gates.** GitHub Actions `build` job (required check on `main`), Spotless (google-java-format), Checkstyle, pre-commit hook blocking commits on `main`.
- **M1 Land render.** Map model and JSON loader with tests, `Tileset` interface with a code-generated ROYGBIV tileset, camera pan/zoom with clamping, HUD, and test maps (flat, stairs, bowl, void-island, non-square, large 128x128).
- **Viewing aids (M1.1 to M1.4).** Grid wireframe, F1 settings panel, `?` help overlay, hovered-tile highlight; readable CI output.
- **Water display (M02.01 to M02.03).** Water layer drawn from the map's `water` layer, inspector panel, tile HUD.
- **Flow tick v1 (M02.04).** The `Flow` class in `sim` (double-buffered, 4-neighbor, `MAX_OUTFLOW` and `MIN_DIFFERENTIAL`) with headless tests for the invariants.

## Now: flow on screen
Watch the flow run and judge it by eye.
- Manual single-step and reset, then play/pause at 1x, then speed controls.
- Sim ms per tick in the inspector; tune `MAX_OUTFLOW` and `MIN_DIFFERENTIAL` by eye.
- More in-code flow tests; later an 8-neighbor option.
  Acceptance: all invariants pass; water on screen settles with no visible sustained oscillation.

## Next: map authoring
Build, keep and re-run scenario maps from inside the game.
- Edit mode: paint a proposed land level (and, in edit mode only, an initial water depth) per tile, then commit.
- Save maps to a user folder, alongside the bundled maps.
- A library of tiny scenario maps with documented expected flow outcomes (the acceptance targets for flow).

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
