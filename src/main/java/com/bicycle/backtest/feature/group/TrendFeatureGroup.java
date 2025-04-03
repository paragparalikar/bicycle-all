package com.bicycle.backtest.feature.group;

import com.bicycle.backtest.MockPosition;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;

import java.util.ArrayList;
import java.util.List;

public class TrendFeatureGroup  implements FeatureGroup {

    private final List<Indicator> indicators = new ArrayList<>();

    public TrendFeatureGroup(IndicatorCache cache, float multiplier, int... barCounts){
        for(int shortBarCount : barCounts){
            final int longBarCount = (int) (shortBarCount * multiplier);
            final Indicator shortEMA = cache.prev(cache.ema(cache.typicalPrice(), shortBarCount), 1);
            final Indicator longEMA = cache.prev(cache.ema(cache.typicalPrice(), longBarCount), 1);
            indicators.add(cache.risingStrength(shortEMA, shortBarCount));
            indicators.add(cache.risingStrength(longEMA, shortBarCount));
            indicators.add(cache.typicalPrice().dividedBy(shortEMA));
            indicators.add(cache.typicalPrice().dividedBy(longEMA));
            indicators.add(cache.close().dividedBy(shortEMA));
            indicators.add(cache.close().dividedBy(longEMA));
            indicators.add(shortEMA.dividedBy(longEMA));
            indicators.add(cache.rsi(cache.close(), shortBarCount));
            indicators.add(cache.risingStrength(cache.rsi(cache.close(), shortBarCount), shortBarCount));
            indicators.add(cache.prev(cache.rsi(cache.close(), shortBarCount), 1));
            indicators.add(cache.cci(shortBarCount).dividedBy(shortEMA));
            indicators.add(cache.risingStrength(cache.cci(shortBarCount), shortBarCount));
            indicators.add(cache.prev(cache.cci(shortBarCount), 1).dividedBy(shortEMA));
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
