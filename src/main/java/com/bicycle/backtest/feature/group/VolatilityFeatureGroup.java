package com.bicycle.backtest.feature.group;

import com.bicycle.backtest.MockPosition;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;

import java.util.ArrayList;
import java.util.List;

public class VolatilityFeatureGroup   implements FeatureGroup {

    private final List<Indicator> indicators = new ArrayList<>();

    public VolatilityFeatureGroup(IndicatorCache cache, float multiplier, int... barCounts) {
        for (int shortBarCount : barCounts) {
            final int longBarCount = (int) (shortBarCount * multiplier);
            indicators.add(cache.atr(shortBarCount).dividedBy(cache.close()));
            indicators.add(cache.atr(shortBarCount).dividedBy(cache.atr(longBarCount)));
            indicators.add(cache.stdDev(cache.close(), shortBarCount).dividedBy(cache.close()));
            indicators.add(cache.stdDev(cache.close(), shortBarCount).dividedBy(cache.stdDev(cache.close(), longBarCount)));
            indicators.add(cache.meanDev(cache.close(), shortBarCount).dividedBy(cache.close()));
            indicators.add(cache.meanDev(cache.close(), shortBarCount).dividedBy(cache.meanDev(cache.close(), longBarCount)));
            indicators.add(cache.chop(shortBarCount).dividedBy(cache.chop(longBarCount)));
            indicators.add(cache.variance(cache.close(), shortBarCount).dividedBy(cache.variance(cache.close(), longBarCount)));
        }
        for(int index = 0; index < barCounts.length; index++){
            indicators.add(cache.prev(indicators.get(index), 1));
        }
    }

    @Override
    public void captureHeaders(List<String> headers) {
        for(Indicator indicator : indicators) headers.add(indicator.toString());
    }

    @Override
    public void captureValues(MockPosition position, List<Float> values) {
        for(Indicator indicator : indicators) values.add(indicator.getValue(position.getSymbol(), position.getTimeframe()));
    }
}
