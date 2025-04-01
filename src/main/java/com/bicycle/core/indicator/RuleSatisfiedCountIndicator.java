package com.bicycle.core.indicator;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.cache.series.SeriesCache;
import com.bicycle.core.indicator.cache.series.SmartSeriesCache;
import com.bicycle.core.indicator.cache.value.SmartValueCache;
import com.bicycle.core.indicator.cache.value.ValueCache;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.util.FloatSeries;

public class RuleSatisfiedCountIndicator implements Indicator {

    private final Rule rule;
    private final int barCount;
    private final ValueCache cache;
    private final ValueCache sumCache;
    private final SeriesCache seriesCache;

    public RuleSatisfiedCountIndicator(int symbolCount, int timeframeCount, Rule rule, int barCount) {
        this.rule = rule;
        this.barCount = barCount;
        this.cache = new SmartValueCache(symbolCount, timeframeCount);
        this.sumCache = new SmartValueCache(symbolCount, timeframeCount);
        this.seriesCache = new SmartSeriesCache(barCount + 1, symbolCount, timeframeCount);
    }

    @Override
    public void clear() {
        cache.clear();
        sumCache.clear();
        seriesCache.clear();
    }

    @Override
    public float getValue(Symbol symbol, Timeframe timeframe) {
        return cache.get(symbol, timeframe);
    }

    @Override
    public void onBar(Bar bar) {
        final float value = rule.isSatisfied(bar.symbol(), bar.timeframe(), null) ? 1 : 0;
        float sum = sumCache.get(bar.symbol(), bar.timeframe());
        sum = Float.isNaN(sum) ? 0 : sum;
        sum += value;

        final FloatSeries series = seriesCache.get(bar.symbol(), bar.timeframe());
        series.add(value);

        float result = Float.NaN;
        if(barCount < series.size()) {
            sum -= series.get(barCount);
        }
        result = sum;
        sumCache.set(bar.symbol(), bar.timeframe(), sum);
        cache.set(bar.symbol(), bar.timeframe(), result);
    }

    @Override
    public String toString() {
        return toText(rule, barCount);
    }

    public static String toText(Rule rule, int barCount) {
        return "ruleSatisfiedCount(" + rule.toString() + "," + barCount + ")";
    }
}
