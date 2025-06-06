package com.bicycle.backtest.feature.captor;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.position.Position;

import java.util.ArrayList;
import java.util.List;

public class BarSequenceFeatureCaptor implements FeatureCaptor {

    private final List<Indicator> indicators = new ArrayList<>();

    public BarSequenceFeatureCaptor(IndicatorCache cache, int... barCounts){
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
    public void captureValues(Position position, List<Object> values) {
        for(Indicator indicator : indicators) {
            final float value = indicator.getValue(position.getSymbol(), position.getTimeframe());
            values.add(Float.isNaN(value) ? null : value);
        }
    }
}
