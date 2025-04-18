package com.bicycle.backtest.feature.captor;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.position.Position;

import java.util.ArrayList;
import java.util.List;

public class EfficiencyFeatureCaptor implements FeatureCaptor {

    private final List<Indicator> indicators = new ArrayList<>();

    public EfficiencyFeatureCaptor(IndicatorCache cache, int... barCounts){
        for(int shortBarCount : barCounts) {
            indicators.add(cache.efficiency(shortBarCount));
            indicators.add(cache.risingStrength(cache.efficiency(shortBarCount), shortBarCount));
            indicators.add(cache.fallingStrength(cache.efficiency(shortBarCount), shortBarCount));
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
