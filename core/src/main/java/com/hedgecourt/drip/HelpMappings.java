package com.hedgecourt.drip;

import java.util.List;

/**
 * The input mappings shown in the help overlay. Hand-maintained: update this list in the same
 * commit as any change to the controls.
 */
public final class HelpMappings {
  /** One row of the help overlay. */
  public record Entry(String keys, String description) {}

  private static final List<Entry> ENTRIES =
      List.of(
          new Entry("W A S D / Arrows", "Pan the map"),
          new Entry("Mouse drag", "Pan the map"),
          new Entry("Scroll wheel", "Zoom at the cursor"),
          new Entry("N / P", "Next / previous map"),
          new Entry(".", "Run one flow tick"),
          new Entry("R", "Reset the map"),
          new Entry("F1", "Toggle settings panel"),
          new Entry("?", "Toggle this help"));

  private HelpMappings() {}

  public static List<Entry> entries() {
    return ENTRIES;
  }
}
