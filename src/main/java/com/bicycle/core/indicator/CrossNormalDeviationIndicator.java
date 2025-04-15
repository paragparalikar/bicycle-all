package com.bicycle.core.indicator;


import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.cache.value.SmartValueCache;
import com.bicycle.core.indicator.cache.value.ValueCache;
import com.bicycle.core.symbol.Symbol;
import lombok.RequiredArgsConstructor;

/**
 * Cross Sectional - Normalized - Standard Deviation indicator
 */
@RequiredArgsConstructor
public class CrossNormalDeviationIndicator implements Indicator {

    private float sum = 0, mean = Float.NaN, distanceSquaredSum = 0, standardDeviation = Float.NaN;
    private int count;
    private long previousDate;
    private final Indicator delegate;
    private final ValueCache cache;

    public CrossNormalDeviationIndicator(int symbolCount, int timeframeCount, Indicator delegate){
        this.cache = new SmartValueCache(symbolCount, timeframeCount);
        this.delegate = delegate;
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public float getValue(Symbol symbol, Timeframe timeframe) {
        return cache.get(symbol, timeframe);
    }

    @Override
    public void onBar(Bar bar) {
        if(previousDate != bar.date()){
            mean = 0 == count || 0 == sum? Float.NaN : sum / count;
            standardDeviation = 0 == count || 0 == distanceSquaredSum ? Float.NaN : (float) Math.sqrt(distanceSquaredSum/count);
            sum = 0;
            count = 0;
            distanceSquaredSum = 0;
            previousDate = bar.date();
        }
        count++;
        final float value = delegate.getValue(bar.symbol(), bar.timeframe());
        sum += Float.isNaN(value) ? 0 : value;
        final float distance = Float.isNaN(value) || Float.isNaN(mean) ? Float.NaN : value - mean;
        distanceSquaredSum += Float.isNaN(distance) ? 0 : (float) Math.pow(distance, 2);
        final float result = Float.isNaN(distance) || Float.isNaN(standardDeviation) || 0 == standardDeviation ? Float.NaN : distance / standardDeviation;
        cache.set(bar.symbol(), bar.timeframe(), result);
    }

    @Override
    public String toString() {
        return toText(delegate);
    }

    public static String toText(Indicator delegate){
        return "crossNormalDeviation(" + delegate + ")";
    }
}
