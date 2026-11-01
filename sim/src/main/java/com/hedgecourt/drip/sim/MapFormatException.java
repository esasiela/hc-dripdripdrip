package com.hedgecourt.drip.sim;

/** Thrown when a map file is malformed or violates the map format rules. */
public class MapFormatException extends RuntimeException {
  public MapFormatException(String message) {
    super(message);
  }

  public MapFormatException(String message, Throwable cause) {
    super(message, cause);
  }
}
