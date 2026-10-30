# Drip Drip Drip: Design

A hobby top-down sim/game. The map is a grid of tiles; each tile has an integer land height and a float water depth. Water flows over the terrain. The player reshapes the land (terraforming) to steer the water. No victory conditions, no combat. The fun is in making water go where you want. Inspired by Creeper World 3, without the combat.

## Scope and non-goals
- In scope: terrain, water flow, fixed water sources, drains, discrete terraforming, simple top-down rendering, hand-authored maps.
- Non-goals for now: combat, enemies, victory/defeat, erosion, multiplayer, 3D or angled views, save games.
- Far future: erosion, textured tilesets, shading, larger maps and performance work.

## Architecture
Gradle multi-module project (liftoff defaults plus one added module):

| Module | Contents | Depends on |
|---|---|---|
| `sim` | Pure Java. Grid, flow, sources/drains, terraform rules, map data model and loader. **No libGDX imports. Ever.** | JSON lib only |
| `core` | libGDX game: rendering, camera, input, tilesets, HUD | `sim`, libGDX |
| `lwjgl3` | Desktop launcher | `core` |

The module boundary is the enforcement mechanism: if `sim` doesn't declare libGDX, an import fails to compile. This lets the sim be unit-tested headlessly, which is how we verify flow behavior without eyes.

## Data model
- `int[] land`, indexed `y * width + x`. `0` = **void**, `1..7` = land height.
- `float[] water`, same indexing. Depth of water on the tile, `>= 0`. Void tiles always hold `0`.
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

Fixed timestep, decoupled from render framerate (accumulator in the game loop). Sim speed is a multiplier on ticks per second; pause and single-step are first-class.

### Flow (v1 algorithm)
Brute force over the whole grid with a double buffer: read from the current `water` array, write into `next`, then swap. Never read values written during the same tick.

For each non-void tile `i` with `water[i] > 0`, surface `s_i = land[i] + water[i]`. For each neighbor `j` in the neighbor table that is non-void with `s_j < s_i`:
- `diff = s_i - s_j`
- `want_j = k * weight_j * diff`

If `sum(want) > water[i]`, scale every `want_j` down proportionally so the sum equals `water[i]`. Then `next[i] -= want_j; next[j] += want_j`.

- **Neighbor table:** a list of `(dx, dy, weight)`. Default is 8 neighbors, orthogonal weight 1.0, diagonal weight about 0.7 (1/sqrt 2). A config switch selects the 4-neighbor table. Comparing the two is a config change.
- **`k`:** a tunable flow fraction. Too high causes oscillation (two tiles swapping levels each tick). It should be low enough to be stable and high enough to look lively. Tune with tests and visual checks.
- **Minimum flow threshold (epsilon):** flows below it are dropped so water settles and doesn't jitter forever.
- **Corner leaking:** two walls touching only at a diagonal leave a gap water can squeeze through. Accepted in v1; revisit if it looks bad.

### Invariants (must be tests)
- **Mass conservation:** total water is unchanged by a flow tick (within float tolerance). It only changes via sources and drains.
- Water is never negative; void tiles never hold water.
- Determinism: same input produces identical output.
- Equilibrium: water in a closed basin settles to a level surface within epsilon.
- Symmetry: a symmetric map and initial state stays symmetric (catches update-order bias).

### Performance
Add a perf HUD (FPS and sim ms per tick) early. Brute force is expected to be fine into the hundreds of thousands of tiles. Later options: active-cell tracking (skip settled tiles), multithreading by row bands. Do not optimize before measuring.

## Rendering (core)
- Top-down, one square per tile. Layers drawn in order: land, water overlay, (later) shading, HUD.
- **Tileset abstraction:** `Tileset` maps land level (0..7) to a drawable and provides the water color. First implementation is `RoygbivTileset`, generated in code with solid colors, no asset files. The map's `meta.tileset` selects it by name. Textured sets are later implementations of the same interface.
- Void rendered black.
- Water: blue with alpha proportional to depth, capped. A debug toggle shows water depth as a grayscale or numeric overlay (blue over rainbow can look muddy).
- Draw only visible tiles (camera culling).
- Later: shading at height boundaries.

## Camera and controls
- Pan (drag or WASD) and zoom (scroll), with clamping.
- Min zoom: whole map fits on screen. Max zoom: about 64 px per tile. Pan clamps to the map edge.
- From M3: pause, single-step, speed (1x/2x/4x...), debug toggles.

## Terraforming (M5)
- Discrete and all-or-nothing: a chunk is removed or placed whole. The action takes time; if interrupted before completing, nothing happens. The sim applies the change only on completion.
- Land is conserved: removing a chunk takes height 1 from a tile; placing adds 1. Heights are clamped to `1..7`. Void tiles can't be touched.
- Starts as a point-and-click "god mode shovel", then grows (a terraformer unit, carried soil, multiple chunk sizes).

## Open questions
- Placing land on a tile that holds water: push the water to neighbors, or block the placement?
- Does a removed chunk go anywhere (carried by a unit, a global soil stock) or just vanish in god mode?
- Sources and drains: constant rate only, or adjustable by the player?
- Should flow rate depend on the height difference nonlinearly (faster waterfalls)?
- Erosion (far future): likely a separate float layer (soil/sediment), not a change to the integer land array.
