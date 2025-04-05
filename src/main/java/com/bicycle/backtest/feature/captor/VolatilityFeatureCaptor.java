package com.bicycle.backtest.feature.captor;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.position.Position;
import com.bicycle.core.symbol.Symbol;

import java.util.ArrayList;
import java.util.List;

public class VolatilityFeatureCaptor implements FeatureCaptor {

    private final List<Indicator> indicators = new ArrayList<>();

    public VolatilityFeatureCaptor(IndicatorCache cache, float multiplier, int... barCounts) {
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
    public void captureValues(Symbol symbol, Timeframe timeframe, Position position, List<Float> values) {
        for(Indicator indicator : indicators) values.add(indicator.getValue(symbol, timeframe));
    }
}
