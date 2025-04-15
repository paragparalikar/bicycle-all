package com.bicycle.core.indicator.cache.value;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Symbol;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

public class TimeframeValueCache implements ValueCache {

    private final Int2FloatOpenHashMap cache = new Int2FloatOpenHashMap(Timeframe.values().length);

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public float get(Symbol symbol, Timeframe timeframe) {
        return cache.getOrDefault(timeframe.ordinal(), Float.NaN);
    }

    @Override
    public void set(Symbol symbol, Timeframe timeframe, float value) {
        cache.put(timeframe.ordinal(), value);
    }
}
