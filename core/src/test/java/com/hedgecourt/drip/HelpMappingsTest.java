package com.hedgecourt.drip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HelpMappingsTest {
  @Test
  void entriesHaveKeysAndDescriptions() {
    assertFalse(HelpMappings.entries().isEmpty());
    for (HelpMappings.Entry e : HelpMappings.entries()) {
      assertFalse(e.keys().isBlank());
      assertFalse(e.description().isBlank());
    }
  }

  @Test
  void keysAreUnique() {
    Set<String> seen = new HashSet<>();
    for (HelpMappings.Entry e : HelpMappings.entries()) {
      seen.add(e.keys());
    }
    assertEquals(HelpMappings.entries().size(), seen.size());
  }
}
