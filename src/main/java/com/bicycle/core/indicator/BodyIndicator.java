package com.bicycle.core.indicator;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.cache.value.SmartValueCache;
import com.bicycle.core.indicator.cache.value.ValueCache;
import com.bicycle.core.symbol.Symbol;

public class BodyIndicator implements Indicator {

    private final ValueCache cache;

    public BodyIndicator(int symbolCount, int timeframeCount) {
        cache = new SmartValueCache(symbolCount, timeframeCount);
    }

    @Override
    public void onBar(Bar bar) {
        cache.set(bar.symbol(), bar.timeframe(), Math.abs(bar.close() - bar.open()));
    }

    @Override
    public float getValue(Symbol symbol, Timeframe timeframe) {
        return cache.get(symbol, timeframe);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public String toString() {
        return toText();
    }

    public static String toText() {
        return "body";
    }
}
