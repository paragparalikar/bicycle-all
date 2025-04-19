package com.bicycle.backtest.feature.captor;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.position.Position;

import java.util.ArrayList;
import java.util.List;

public class VolumeFeatureCaptor implements FeatureCaptor {

    private final List<Indicator> indicators = new ArrayList<>();

    public VolumeFeatureCaptor(IndicatorCache cache, float multiplier, int... barCounts){
        indicators.add(cache.volume().dividedBy(cache.prev(cache.volume(), 1)));
        for(int shortBarCount : barCounts) {
            final int longBarCount = (int) (shortBarCount * multiplier);
            indicators.add(cache.risingStrength(cache.volume(), shortBarCount));
            indicators.add(cache.volume().dividedBy(cache.prev(cache.ema(cache.volume(), shortBarCount), 1)));
            indicators.add(cache.ema(cache.volume(), shortBarCount).dividedBy(cache.ema(cache.volume(), longBarCount)));
        }
    }

    @Override
    public void captureHeaders(List<String> headers) {
        for(Indicator indicator : indicators) headers.add(indicator.toString());
    }

    @Override
    public void captureValues(Position position, List<Object> values) {
        for(Indicator indicator : indicators) values.add(indicator.getValue(position.getSymbol(), position.getTimeframe()));
    }
}
