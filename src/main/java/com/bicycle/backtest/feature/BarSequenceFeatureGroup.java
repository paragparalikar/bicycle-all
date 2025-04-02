package com.bicycle.backtest.feature;

import com.bicycle.backtest.MockPosition;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;

import java.util.ArrayList;
import java.util.List;

public class BarSequenceFeatureGroup implements FeatureGroup {

    private final List<Indicator> indicators = new ArrayList<>();

    public BarSequenceFeatureGroup(IndicatorCache cache, int... barCounts){
        for(int barCount : barCounts){
            indicators.add(cache.risingStrength(cache.high(), barCount));
            indicators.add(cache.risingStrength(cache.low(), barCount));
            indicators.add(cache.risingStrength(cache.close(), barCount));
            indicators.add(cache.risingStrength(cache.typicalPrice(), barCount));
            indicators.add(cache.risingStrength(cache.trueRange(), barCount));
            indicators.add(cache.ruleSatisfiedStrength(cache.open().lesserThanOrEquals(cache.close()), barCount)); // Green bar
            indicators.add(cache.ruleSatisfiedStrength(cache.typicalPrice().lesserThanOrEquals(cache.close()), barCount)); // Bullish bar at close
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
