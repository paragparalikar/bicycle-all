package com.bicycle.backtest.feature;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.symbol.Symbol;

import java.util.ArrayList;
import java.util.List;

public class TrendFeatureGroup  implements FeatureGroup {

    private final List<Indicator> indicators = new ArrayList<>();

    public TrendFeatureGroup(IndicatorCache cache, int... barCounts){
        for(int barCount : barCounts){

        }
    }

    @Override
    public void captureHeaders(List<String> headers) {
        for(Indicator indicator : indicators) headers.add(indicator.toString());
    }

    @Override
    public void captureValues(Symbol symbol, Timeframe timeframe, List<Float> values) {
        for(Indicator indicator : indicators) values.add(indicator.getValue(symbol, timeframe));
    }
}
