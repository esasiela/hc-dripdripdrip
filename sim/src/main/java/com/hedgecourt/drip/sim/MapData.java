package com.hedgecourt.drip.sim;

/** A loaded map: file metadata plus the initial {@link Grid}. */
public record MapData(int version, String name, String tileset, Grid grid) {}
