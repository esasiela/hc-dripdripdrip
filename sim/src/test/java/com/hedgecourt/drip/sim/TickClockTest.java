package com.hedgecourt.drip.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TickClockTest {
  private final TickClock clock = new TickClock(60, TickClock.MAX_FRAME_SECONDS);

  @Test
  void startsPausedAndRunsNothing() {
    assertFalse(clock.isPlaying());
    assertEquals(0, clock.advance(1.0));
  }

  @Test
  void wholeSecondsGiveExactTickCounts() {
    clock.play();
    assertEquals(15, clock.advance(0.25));
    assertEquals(6, clock.advance(0.1));
  }

  @Test
  void slowFramesRunSeveralTicksEach() {
    clock.play();
    for (int i = 0; i < 10; i++) {
      assertEquals(2, clock.advance(1.0 / 30));
    }
  }

  @Test
  void leftoverTimeCarriesToNextFrame() {
    clock.play();
    // 0.5 tick per frame: every second frame runs one tick.
    assertEquals(0, clock.advance(1.0 / 120));
    assertEquals(1, clock.advance(1.0 / 120));
    assertEquals(0, clock.advance(1.0 / 120));
    assertEquals(1, clock.advance(1.0 / 120));
  }

  @Test
  void fastFramesNeverLoseTime() {
    clock.play();
    int total = 0;
    for (int i = 0; i < 240; i++) {
      total += clock.advance(1.0 / 240);
    }
    assertEquals(60, total, 1);
  }

  @Test
  void longStallIsCapped() {
    clock.play();
    assertEquals(15, clock.advance(10.0));
    assertEquals(0, clock.advance(0));
  }

  @Test
  void pausedTimeIsNotBanked() {
    clock.play();
    clock.pause();
    assertEquals(0, clock.advance(0.1));
    clock.play();
    assertEquals(0, clock.advance(0));
  }

  @Test
  void pauseDiscardsLeftover() {
    clock.play();
    assertEquals(0, clock.advance(0.9 / 60));
    clock.pause();
    clock.play();
    assertEquals(0, clock.advance(0.9 / 60));
  }

  @Test
  void resetPausesAndDiscardsLeftover() {
    clock.play();
    clock.advance(0.9 / 60);
    clock.reset();
    assertFalse(clock.isPlaying());
    clock.play();
    assertEquals(0, clock.advance(0.9 / 60));
  }

  @Test
  void toggleFlipsPlaying() {
    clock.toggle();
    assertTrue(clock.isPlaying());
    clock.toggle();
    assertFalse(clock.isPlaying());
  }

  @Test
  void negativeDeltaIsIgnored() {
    clock.play();
    assertEquals(0, clock.advance(-1.0));
  }

  @Test
  void rejectsNonPositiveSettings() {
    assertThrows(IllegalArgumentException.class, () -> new TickClock(0, 0.25));
    assertThrows(IllegalArgumentException.class, () -> new TickClock(60, 0));
  }
}
