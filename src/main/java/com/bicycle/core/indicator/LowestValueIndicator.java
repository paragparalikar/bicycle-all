package com.bicycle.core.indicator;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.cache.series.SeriesCache;
import com.bicycle.core.indicator.cache.series.SmartSeriesCache;
import com.bicycle.core.indicator.cache.value.SmartValueCache;
import com.bicycle.core.indicator.cache.value.ValueCache;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.util.FloatSeries;

public class LowestValueIndicator implements Indicator {
    
    private final int barCount;
    private final ValueCache cache;
    private final Indicator indicator;
    private final SeriesCache seriesCache;
    
    public LowestValueIndicator(int symbolCount, int timeframeCount, Indicator indicator, int barCount) {
        this.barCount = barCount;
        this.indicator = indicator;
        this.cache = new SmartValueCache(symbolCount, timeframeCount);
        this.seriesCache = new SmartSeriesCache(barCount + 1, symbolCount, timeframeCount);
    }
    
    @Override
    public void onBar(Bar bar) {
        final float indicatorValue = indicator.getValue(bar.symbol(), bar.timeframe());
        final FloatSeries series = seriesCache.get(bar.symbol(), bar.timeframe());
        series.add(indicatorValue);
        if(!Float.isNaN(indicatorValue)) {
            float lowestValue = Float.MAX_VALUE;
            if(barCount > series.size()) {
                lowestValue = Float.NaN;
            } else if(barCount <= series.size()) {
                for(int index = 0; index < barCount; index++) {
                    lowestValue = Math.min(lowestValue, series.get(index));
                }
            }
            cache.set(bar.symbol(), bar.timeframe(), lowestValue);
        }
    }

    @Override
    public float getValue(Symbol symbol, Timeframe timeframe) {
        return cache.get(symbol, timeframe);
    }

    @Override
    public void clear() {
        cache.clear();
        seriesCache.clear();
    }
    
    @Override
    public String toString() {
        return toText(indicator, barCount);
    }
    
    public static String toText(Indicator source, int barCount) {
        return "lowest(" + source + "," + barCount + ")";
    }

}
