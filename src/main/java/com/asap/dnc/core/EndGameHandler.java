package com.asap.dnc.core;

import java.util.Map;

public interface EndGameHandler {
    public void onGameEnd(Map<PenColor, Integer> cellMap);
}
