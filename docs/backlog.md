# Backlog

A story is one feature or change delivered as **one small PR**: a one-line goal plus a few acceptance bullets. The roadmap (`docs/roadmap.md`) holds the big themes; this file holds the stories.

- There is no "Now" section. A story is in progress when its prompt is running in Claude Code.
- Order within a section is priority. The top item of the first Next section is the likely next story.
- When a story is merged, it moves to Done by name (no PR number; `git log` has it) in the same PR that delivers it. Planning changes (new stories, reordering) go in their own small docs PR.
- Story prompts live in `docs/prompts/`, named `M<theme>.<seq>-<slug>.md` with two-digit numbers assigned by hand so they sort in order (for example `M02.01-static-water-rendering.md`). Branches stay descriptive (`feat/static-water-rendering`). Earlier prompts (M0 to M1.4) use an older numbering and are left as they are. A prompt that isn't next in line yet may stay unnumbered until its order is settled. Stories without a prompt yet are kept short here and expanded when they come up.

## Next (water display and scenario maps)
1. **Richer cursor inspector.** HUD shows land, water depth and surface height for the tile under the cursor (land height is already shown).
2. **Flow scenario maps** (`prompts/flow-scenario-maps.md`). Tiny, deterministic maps, each with a documented expected outcome, plus a doc and a test that keeps map and doc in agreement. These become the acceptance targets for flow.

## Next (flow)
Rough order, each its own PR. Prompts are written when the previous story merges, so they match what was actually built.
- Flow tick in `sim`: `World`, neighbor table, one double-buffered tick, with invariant tests (conservation, non-negative, void blocks, determinism).
- Scenario tests: each scenario map reaches its documented equilibrium within tolerance; tune `k` and epsilon. Symmetry test.
- Fixed-timestep loop in `core` with pause and single-step.
- Speed controls (1x/2x/4x).
- Render water changing over time; sim ms per tick in the HUD.
- Settings: 4-neighbor vs 8-neighbor switch.

## Next (more viewing aids, lower priority)
- **Map border and camera padding.** A bright border around the whole map, with the camera clamped so the border is fully visible at the edges.
- **Edge shading (cheap version).** Darken a thin strip along a tile edge where the neighbor to the north or west is higher, for a drop-shadow effect. No wang/blob tilesets yet.
- **Water debug overlay toggle** (depth as grayscale or numbers), as a settings checkbox.

## Later
- Sources and drains in the map format and the tick pipeline, with markers.
- Terraforming: click to remove or place a chunk, all-or-nothing timed action.
- Textured tilesets, active-cell optimization, multithreading, terraformer unit, erosion.

## Parking lot (ideas, not yet stories)
- Settings persistence between runs.
- Upload the HTML test report as a CI artifact, or publish a test summary on the PR.
- A single table of key bindings that both the input handling and the help overlay read from, so they can't drift apart. Revisit when the help overlay grows into a tabbed overlay.
- Cap on how far the camera can zoom out (for example a 10 px/tile floor). Today the minimum zoom is "whole map fits on screen", so a hard floor would stop large maps being seen whole in a small window. The grid already hides below 4 px/tile. Decide after living with it.
- GitHub Issues or Projects as the story tracker, if this file gets unwieldy. It would give stories real IDs, and PRs could say "Closes #n". Plan: try it with one story after a couple more file-based ones (stories M02.01 onward), then decide at a retro. Hand-numbered prompt files are fine until the two-digit numbers run out.

## Done
- M0 scaffold
- M0.5 CI and quality gates
- M1 land render (loader and tests, tileset, camera, HUD, test maps)
- CI readability (Gradle `lifecycle` logging, each test printed as passed/failed/skipped, per-module summary line)
- CI failure demo (`./gradlew build --continue` so independent failures all show in one run)
- M1.1 Grid wireframe (always-on grid, hidden below 4 px/tile)
- M1.2 Settings screen (F1 panel, Scene2D, `ViewSettings`, Show grid checkbox)
- M1.3 Help overlay (`?` toggles a non-modal list of input mappings, `HelpMappings`)
- M1.4 Hover tile highlight (two-tone 3 px outline on the hovered tile, "Highlight hovered tile" checkbox)
- M02.01 Static water rendering (water layer drawn under grid and highlight, alpha by depth, "Show water" checkbox, `water-depths` showcase map)
