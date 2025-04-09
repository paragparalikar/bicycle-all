package com.bicycle.core.indicator;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.cache.series.SeriesCache;
import com.bicycle.core.indicator.cache.series.SmartSeriesCache;
import com.bicycle.core.indicator.cache.value.SmartValueCache;
import com.bicycle.core.indicator.cache.value.ValueCache;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.util.FloatSeries;


public class EfficiencyIndicator implements Indicator {

    private final int barCount;
    private final ValueCache cache;
    private final ValueCache spreadSumCache;
    private final SeriesCache openSeriesCache;
    private final SeriesCache spreadSeriesCache;


    public EfficiencyIndicator(int symbolCount, int timeframeCount, int barCount) {
        this.barCount = barCount;
        this.cache = new SmartValueCache(symbolCount, timeframeCount);
        this.spreadSumCache = new SmartValueCache(symbolCount, timeframeCount);
        this.openSeriesCache = new SmartSeriesCache(barCount + 1, symbolCount, timeframeCount);
        this.spreadSeriesCache = new SmartSeriesCache(barCount + 1, symbolCount, timeframeCount);
    }

    @Override
    public void onBar(Bar bar) {
        final float spread = bar.high() - bar.low();
        final FloatSeries spreadSeries = spreadSeriesCache.get(bar.symbol(), bar.timeframe());
        spreadSeries.add(spread);

        float sum = spreadSumCache.get(bar.symbol(), bar.timeframe());
        sum = Float.isNaN(sum) ? 0 : sum;
        sum += spread;

        final FloatSeries openSeries = openSeriesCache.get(bar.symbol(), bar.timeframe());
        openSeries.add(bar.open());

        float value = Float.NaN;
        if(barCount >= openSeries.size()) {
            value = Float.NaN;
        } else {
            sum -= spreadSeries.get(barCount);
            value = (bar.close() - openSeries.get(barCount)) / sum;
        }

        spreadSumCache.set(bar.symbol(), bar.timeframe(), sum);
        cache.set(bar.symbol(), bar.timeframe(), value);
    }

    @Override
    public float getValue(Symbol symbol, Timeframe timeframe) {
        return cache.get(symbol, timeframe);
    }

    @Override
    public void clear() {
        cache.clear();
        spreadSumCache.clear();
        openSeriesCache.clear();
        spreadSeriesCache.clear();
    }

    @Override
    public String toString() {
        return toText(barCount);
    }

    public static String toText(int barCount) {
        return "efficiency(" + barCount + ")";
    }
}
