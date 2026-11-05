package com.hedgecourt.drip.sim;

import java.util.List;

/**
 * One flow tick over a {@link Grid}. Reads only the current water buffer and writes a next buffer,
 * so visit order does not matter. See "Flow" in docs/design.md.
 */
public class Flow {
  public static final int TICKS_PER_SECOND = 60;
  public static final float MAX_OUTFLOW = 1.0f;
  public static final float MIN_DIFFERENTIAL = 0.1f;

  /** A neighbor offset and its weight in the flow split. */
  public record Neighbor(int dx, int dy, float weight) {}

  public static final List<Neighbor> NEIGHBORS_4 =
      List.of(
          new Neighbor(0, -1, 1.0f),
          new Neighbor(1, 0, 1.0f),
          new Neighbor(0, 1, 1.0f),
          new Neighbor(-1, 0, 1.0f));

  private final int ticksPerSecond;
  private final float maxOutflow;
  private final float minDifferential;
  private final List<Neighbor> neighbors;

  public Flow() {
    this(TICKS_PER_SECOND, MAX_OUTFLOW, MIN_DIFFERENTIAL);
  }

  public Flow(float maxOutflow, float minDifferential) {
    this(TICKS_PER_SECOND, maxOutflow, minDifferential);
  }

  public Flow(int ticksPerSecond, float maxOutflow, float minDifferential) {
    this.ticksPerSecond = ticksPerSecond;
    this.maxOutflow = maxOutflow;
    this.minDifferential = minDifferential;
    this.neighbors = NEIGHBORS_4;
  }

  /** Runs one tick, updating {@code grid.water} in place. */
  public void tick(Grid grid) {
    float[] water = grid.water;
    float[] next = water.clone();
    int count = neighbors.size();
    int[] wantIdx = new int[count];
    float[] wantShare = new float[count];
    float perTickCap = maxOutflow / ticksPerSecond;

    for (int y = 0; y < grid.height; y++) {
      for (int x = 0; x < grid.width; x++) {
        int src = y * grid.width + x;
        if (grid.land[src] == 0 || water[src] <= 0f) {
          continue;
        }
        float surfaceSrc = grid.land[src] + water[src];
        int n = 0;
        float totalDiff = 0f;
        float totalWeighted = 0f;
        for (Neighbor nb : neighbors) {
          int nx = x + nb.dx();
          int ny = y + nb.dy();
          if (!grid.inBounds(nx, ny)) {
            continue;
          }
          int idx = ny * grid.width + nx;
          if (grid.land[idx] == 0) {
            continue;
          }
          float diff = surfaceSrc - (grid.land[idx] + water[idx]);
          if (diff >= minDifferential) {
            float weighted = nb.weight() * diff;
            wantIdx[n] = idx;
            wantShare[n] = weighted;
            n++;
            totalDiff += diff;
            totalWeighted += weighted;
          }
        }
        if (n == 0) {
          continue;
        }
        float flow = Math.min(Math.min(totalDiff / (n + 1), water[src]), perTickCap);
        next[src] -= flow;
        for (int i = 0; i < n; i++) {
          next[wantIdx[i]] += flow * (wantShare[i] / totalWeighted);
        }
      }
    }

    for (int i = 0; i < next.length; i++) {
      water[i] = Math.max(0f, next[i]);
    }
  }
}
