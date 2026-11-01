# Backlog

A story is one feature or change delivered as **one small PR**: a one-line goal plus a few acceptance bullets. Order within a section is priority. The roadmap (`docs/roadmap.md`) holds the big themes; this file holds the stories. When a story is merged, it moves to Done by name (no PR number; `git log` has it) in the same PR that delivers it. Planning changes (new stories, reordering) go in their own small docs PR.

Story prompts live in `docs/prompts/`. Stories not yet written up as prompts are kept short here and expanded when they come up.

## Now
1. **M1.2 Settings screen** (`prompts/M1.2-settings-screen.md`). A mouse-operable settings panel with one checkbox that toggles the grid.

## Next (viewing aids)
- **Hover tile highlight.** A slightly thicker outline on the tile under the cursor, as a checkbox in the settings screen.
- **Map border and camera padding.** A bright border around the whole map, with the camera clamped so the border is fully visible at the edges.
- **Edge shading (cheap version).** Darken a thin strip along a tile edge where the neighbor to the north or west is higher, for a drop-shadow effect. No wang/blob tilesets yet.
- **Richer cursor inspector.** HUD shows land, water depth and surface height for the tile under the cursor (land height is already shown).

## Next (water display and scenario maps)
- **Static water rendering.** Draw water from the map's `water` layer (blue, alpha by depth, capped), with no physics. Includes showcase maps with deliberately impossible columns of water at different depths, to tune how depth looks.
- **Water debug overlay toggle** (depth as grayscale or numbers), as a settings checkbox.
- **Flow scenario maps with documented expected outcomes.** Tiny, very predictable maps, each with a written expected result. Examples: flat map with one tile dipped one level and one unit of water next to it (water must fall into the dip); a few variants with obvious outcomes (two dips, a wall, a ramp). These become the acceptance tests for flow.

## Later (flow and beyond)
Rough breakdown of the flow theme into stories. Each should be its own PR:
- Neighbor table plus one flow tick on a `World` in `sim`, with unit tests (conservation, non-negative, void blocks).
- Determinism and symmetry tests.
- Closed-basin equilibrium test.
- Fixed-timestep loop in `core` with pause and single-step.
- Speed controls (1x/2x/4x).
- Render water changing over time; sim ms per tick in the HUD.
- Settings: 4-neighbor vs 8-neighbor switch.
- Sources and drains in the map format and the tick pipeline, with markers.
- Terraforming: click to remove or place a chunk, all-or-nothing timed action.
- Edge shading and textured tilesets, active-cell optimization, multithreading, terraformer unit, erosion.

## Parking lot (ideas, not yet stories)
- Settings persistence between runs.
- Print "passed" tests in CI log or publish a test summary on the PR.
- Decide whether story names keep the `M1.x` style or switch to descriptive names.
- Cap on how far the camera can zoom out (for example a 10 px/tile floor). Today the minimum zoom is "whole map fits on screen", so a hard floor would stop large maps being seen whole in a small window. The grid already hides below 4 px/tile. Decide after living with it.

## Done
- M0 scaffold
- M0.5 CI and quality gates
- M1 land render (loader and tests, tileset, camera, HUD, test maps)
- CI readability (Gradle `lifecycle` logging, each test printed as passed/failed/skipped, per-module summary line)
- CI failure demo (`./gradlew build --continue` so independent failures all show in one run)
- M1.1 Grid wireframe (always-on grid, hidden below 4 px/tile)
