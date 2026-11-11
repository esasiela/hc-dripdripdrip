package com.hedgecourt.drip.sim;

/**
 * Fixed-timestep accumulator. Each rendered frame reports its elapsed time to {@link #advance} and
 * runs the returned number of whole ticks. Leftover time carries to the next frame. Starts paused.
 */
public final class TickClock {
  /** Longest frame time counted, so a stall (window drag, debugger) cannot cause a tick burst. */
  public static final double MAX_FRAME_SECONDS = 0.25;

  private final int ticksPerSecond;
  private final double maxFrameSeconds;
  private boolean playing;

  /** Elapsed time not yet turned into ticks, in ticks (always below 1). */
  private double leftoverTicks;

  public TickClock(int ticksPerSecond, double maxFrameSeconds) {
    if (ticksPerSecond <= 0 || maxFrameSeconds <= 0) {
      throw new IllegalArgumentException("ticksPerSecond and maxFrameSeconds must be positive");
    }
    this.ticksPerSecond = ticksPerSecond;
    this.maxFrameSeconds = maxFrameSeconds;
  }

  public boolean isPlaying() {
    return playing;
  }

  public void play() {
    playing = true;
  }

  /** Pauses and discards leftover time. */
  public void pause() {
    playing = false;
    leftoverTicks = 0;
  }

  public void toggle() {
    if (playing) {
      pause();
    } else {
      play();
    }
  }

  /** Pauses and discards leftover time. */
  public void reset() {
    pause();
  }

  /** Adds the frame's elapsed time and returns how many whole ticks to run now (0 if paused). */
  public int advance(double deltaSeconds) {
    if (!playing) {
      return 0;
    }
    leftoverTicks += Math.min(Math.max(deltaSeconds, 0), maxFrameSeconds) * ticksPerSecond;
    int ticks = (int) Math.floor(leftoverTicks);
    leftoverTicks -= ticks;
    return ticks;
  }
}
