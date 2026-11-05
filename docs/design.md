# Drip Drip Drip: Design

A hobby top-down sim/game. The map is a grid of tiles; each tile has an integer land height and a float water depth. Water flows over the terrain. The player reshapes the land (terraforming) to steer the water. No victory conditions, no combat. The fun is in making water go where you want. Inspired by Creeper World 3, without the combat.

The simulation is not a physics engine. It aims for game-feel: water that looks and behaves in a satisfying, predictable way. Real fluid dynamics (momentum, pressure, compressibility) are non-goals. Water has no momentum — it flows toward lower surfaces and stops when it gets there, with no water hammer or sloshing back and forth. The goal is water that is fun to watch and looks plausible, and cheap to compute.

## Scope and non-goals
- In scope: terrain, water flow, fixed water sources, drains, discrete terraforming, simple top-down rendering, hand-authored maps.
- Non-goals for now: combat, enemies, victory/defeat, erosion, multiplayer, 3D or angled views, save games.
- Far future: erosion, textured tilesets, shading, larger maps and performance work.

## Architecture
Gradle multi-module project (liftoff defaults plus one added module):

| Module | Contents | Depends on |
|---|---|---|
| `sim` | Pure Java. Grid, flow, sources/drains, terraform rules, map data model and loader. **No libGDX imports. Ever.** | JSON lib only |
| `core` | libGDX game: rendering, camera, input, tilesets, inspector, HUD | `sim`, libGDX |
| `lwjgl3` | Desktop launcher | `core` |

The module boundary is the enforcement mechanism: if `sim` doesn't declare libGDX, an import fails to compile. This lets the sim be unit-tested headlessly, which is how we verify flow behavior without eyes.

## Data model
- `int[] land`, indexed `y * width + x`. `0` = **void**, `1..7` = land height.
- `float[] water`, same indexing. Depth of water on the tile, `>= 0`, in the same units as land height. Void tiles always hold `0`.
- Water surface = `land + water`.
- Map size is configurable per map (no hard-coded dimensions anywhere).
- Access goes through a small `Grid`/`World` class so storage can change later.

### Void tiles
Void (`land == 0`) behaves like an infinitely tall wall: water never flows in or out, units can't be placed, terraforming refuses. Rendered black or a distinct dark color. Other immovable things (buildings, rocks) go in the reserved `obstacles` list later, not in the land array.

## Map file format (v1)
JSON. One row per string, whitespace-delimited values, same convention for land and water. Unknown keys are ignored (forward compatibility). `sources`, `drains`, `obstacles` are reserved and empty in v1.

```json
{
  "version": 1,
  "meta": { "name": "Simple Bowl", "tileset": "roygbiv" },
  "size": { "width": 8, "height": 6 },
  "land": [
    "7 7 7 7 7 7 7 7",
    "7 2 2 2 2 2 2 7",
    "7 2 1 1 1 1 2 7",
    "7 2 1 1 1 1 2 7",
    "7 2 2 2 2 2 2 7",
    "7 7 7 7 7 7 7 7"
  ],
  "water": [
    "0 0 0 0 0 0 0 0",
    "0 0 0 0 0 0 0 0",
    "0 0 0 2 2 0 0 0",
    "0 0 0 2 2 0 0 0",
    "0 0 0 0 0 0 0 0",
    "0 0 0 0 0 0 0 0"
  ],
  "sources": [],
  "drains": [],
  "obstacles": []
}
```

Loader rules: `water` optional (defaults to dry). Row and column counts must match `size`, with clear errors that include row/column. Land values outside `0..7` are errors. Water on a void tile is an error. Negative water is an error.

Later additions (not v1): `sources: [{x, y, rate}]`, `drains: [{x, y, rate}]`, `obstacles`, extra meta (author, notes).

## Simulation
### Tick pipeline
Each tick is an ordered list of passes over the world, so new mechanics (and eventually erosion) are new passes rather than rewrites:
1. Sources add water.
2. Flow moves water.
3. Drains remove water.
4. (Later) erosion.

Fixed timestep of 1/60 s (`TICKS_PER_SECOND = 60`), decoupled from render framerate (accumulator in the game loop). Sim speed is how many ticks run per second of real time (2x runs two ticks per frame); the tick itself never changes size, so tuning and stability do not depend on speed. Pause and single-step are first-class. Rates in the design are written in water per second and converted to per-tick amounts once, by dividing by `TICKS_PER_SECOND`.

### Flow
Brute force over the whole grid with a double buffer: read from the current `water` array, write into `next`, then swap. Never read values written during the same tick. "Downhill" always means by surface height (`land + water`), not land height alone.

The flow logic lives in its own class so the algorithm can be swapped later without touching the tick pipeline or the rest of the sim.

**Constants:**
- **`MAX_OUTFLOW`** — the most water a single tile can send out, in water per second. Per tick that is `MAX_OUTFLOW / TICKS_PER_SECOND`. It sets how fast water moves. Starting value: **1.0** water/sec. Example: two adjacent tiles with a surface differential of 2 level out in roughly 60 ticks (one second), because leveling moves 1.0 unit.
- **`MIN_DIFFERENTIAL`** — the smallest surface difference that makes water move. This is a depth, not a rate, so it is not scaled by tick rate or speed. It acts as surface tension and cohesion: a small puddle on flat land stops spreading once its surface is within this much of its neighbors, and a droplet shallower than this on flat ground stays put. Higher values keep puddles compact; lower values let water spread thin. Starting value: **0.1** (tunable).

Both are configurable on the flow class (constructor parameters with these defaults) so tests can use other values.

**Per-tick algorithm:**

For each non-void tile `src` with `water[src] > 0`:
1. `surface_src = land[src] + water[src]`.
2. For each neighbor `nb` in the neighbor table that is inside the map and non-void: `diff = surface_src - surface_nb`. If `diff >= MIN_DIFFERENTIAL`, `nb` is a **wanting neighbor**; remember its `diff` and add it to `total_diff`.
3. If there are no wanting neighbors, move on.
4. Let `n` be the number of wanting neighbors. The amount `src` sends this tick is
   `flow = min(total_diff / (n + 1), water[src], MAX_OUTFLOW / TICKS_PER_SECOND)`.
   The first term is the amount that would level `src` with `n` equally low neighbors. It is what stops two tiles from swapping heights and ping-ponging forever. The second term keeps water non-negative, and the third is the speed limit.
5. Split `flow` among the wanting neighbors in proportion to `weight * diff` (all weights are 1.0 with the 4-neighbor table). Subtract `flow` from `next[src]` once and add each neighbor's share to its `next[nb]`.

Because every tile reads only the current buffer and a tile never sends more than it holds, mass is conserved and water cannot go negative regardless of the order tiles are visited.

**No momentum.** There is no velocity or direction memory between ticks. Each tick looks only at current surface heights. Water hitting a wall simply has nowhere to go and stops.

**Known quirks (accepted):**
- When two source tiles push into a common neighbor in the same tick, the neighbor can end slightly above the level it would settle at, then send a little back. These wobbles are small and die out.
- With unequal neighbors, a tile can end a hair below its higher wanting neighbor after a tick. The next tick corrects it.
- A film of water shallower than `MIN_DIFFERENTIAL` on tiles with equal-height neighbors stays put. That is the intended cohesion.

**Neighbor table:** a list of `(dx, dy, weight)`. The first implementation uses the 4 orthogonal neighbors, all weight 1.0. Later, an 8-neighbor table adds the diagonals with weight approximately 0.707 (1/√2), which makes spread look round instead of square. Comparing the two is a config change, not a code change.

**Corner leaking** (8-neighbor only): two walls touching only at a diagonal leave a gap water can squeeze through. Accept it when diagonals arrive; revisit if it looks bad.

### Invariants (must be tests)
- **Mass conservation:** total water is unchanged by a flow tick (within float tolerance). It only changes via sources and drains.
- Water is never negative; void tiles never hold water.
- Determinism: same input produces identical output.
- Two-tile leveling: with one wanting neighbor and no speed limit in the way, a tile sends exactly half the differential, so the pair ends level and does not swap order.
- Settling: water in a closed basin reaches a state where no water moves, with every pair of adjacent tiles within `MIN_DIFFERENTIAL` of level surface.
- Symmetry: a symmetric map and initial state stays symmetric (catches update-order bias).

### Performance
Add a perf readout in the inspector (FPS and sim ms per tick) early. Brute force is expected to be fine into the hundreds of thousands of tiles. Later options: active-cell tracking (skip settled tiles), multithreading by row bands. Do not optimize before measuring.

## Rendering (core)
- Top-down, one square per tile. Layers drawn in order: land, water overlay, grid, hover highlight, tile HUD, inspector overlay.
- **Tileset abstraction:** `Tileset` maps land level (0..7) to a drawable and provides the water color (RGB; alpha is computed from depth by the renderer). First implementation is `RoygbivTileset`, generated in code with solid colors, no asset files. The map's `meta.tileset` selects it by name. Textured sets are later implementations of the same interface.
- Void rendered black.
- **Vocabulary:** the *Inspector* is the fixed upper-left data panel. *HUD* is the tile-attached overlay drawn on the map near the hovered tile.
- Water: drawn using the tileset's water RGB. Alpha scales with depth (linearly from a small minimum floor to a cap), so the land under deep water is still faintly visible. Constants are tunable.
- Draw only visible tiles (camera culling).
- Later: shading at height boundaries.

## UI overlays

### Inspector
A fixed panel in the upper left of the screen. Shows global stats (FPS, map name and size, camera zoom) and per-tile data for the hovered tile (coordinates, land height, water depth, surface height). Utilitarian appearance — its purpose is to put every useful number in one place. Has a title line rendered in a larger font size.

### HUD (tile-attached)
An overlay drawn on the map, positioned relative to the hovered tile (not the mouse cursor). Shows contextual tile data so the player can read it without looking away from the map.

**MVP (M02.03, built):** colored text `land + water = surface`: land green, water blue, surface white, separators light gray, with a 1 px dark shadow for legibility. Always shows the full label on land tiles (dry tiles show `0.00` water); void tiles show nothing. Fixed screen size at every zoom, with no zoom threshold. Positioned above the hovered tile, centered horizontally, 6 px gap, then clamped to the visible part of the map (the viewport intersected with the map's on-screen rectangle, inset by 6 px so the label stays just inside the edge), so tiles near an edge or corner share the same label position. Toggled by the "Show tile HUD" setting (default on); hidden while the cursor is over the settings panel or help overlay.

**Vision:** a transparent donut ring around the hovered tile. The inner radius sits at the tile's diagonal corners (or slightly outside them with padding), so the entire HUD is drawn outside the tile. The donut carries the tile data in a more attractive visual form, with subtle shading and drop shadows. As the game grows, the donut gains richer content: status icons (terraforming in progress, tile reserved for a unit), and potentially action buttons. Action buttons would need a way to keep the HUD snapped to the tile while the mouse moves into the HUD area (for example, holding a modifier key). The donut and its interactions are later stories; the MVP just gets the data on screen.

## Camera and controls
- Pan (drag or WASD) and zoom (scroll), with clamping.
- F1 opens the settings panel (Scene2D, mouse-operable, not persisted). Options live in `ViewSettings`; the panel swallows clicks and scrolls so the camera ignores them.
- Min zoom: whole map fits on screen. Max zoom: about 64 px per tile. Pan clamps to the map edge.
- With the flow stories: pause, single-step, speed (1x/2x/4x...), debug toggles.

## Terraforming
- Discrete and all-or-nothing: a chunk is removed or placed whole. The action takes time; if interrupted before completing, nothing happens. The sim applies the change only on completion.
- Land is conserved: removing a chunk takes height 1 from a tile; placing adds 1. Heights are clamped to `1..7`. Void tiles can't be touched.
- Starts as a point-and-click "god mode shovel", then grows (a terraformer unit, carried soil, multiple chunk sizes).

## Open questions
- Placing land on a tile that holds water: push the water to neighbors, or block the placement?
- Does a removed chunk go anywhere (carried by a unit, a global soil stock) or just vanish in god mode?
- Sources and drains: constant rate only, or adjustable by the player?
- Erosion (far future): likely a separate float layer (soil/sediment), not a change to the integer land array.
