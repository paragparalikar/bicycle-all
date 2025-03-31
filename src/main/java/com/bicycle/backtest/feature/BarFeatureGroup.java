package com.bicycle.backtest.feature;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.symbol.Symbol;

import java.util.ArrayList;
import java.util.List;

public class BarFeatureGroup implements FeatureGroup {

    private final List<Indicator> indicators = new ArrayList<>();

    public BarFeatureGroup(IndicatorCache cache){
        final Indicator atrIndicator = cache.atr(14);

        // Features for current bar
        indicators.add(cache.ibs());
        indicators.add(cache.body().dividedBy(atrIndicator));
        indicators.add(cache.spread().dividedBy(atrIndicator));
        indicators.add(cache.upperWick().dividedBy(atrIndicator));
        indicators.add(cache.lowerWick().dividedBy(atrIndicator));
        indicators.add(cache.body().dividedBy(cache.spread()));

        // Features for previous bar
        indicators.add(cache.prev(cache.ibs(), 1));
        indicators.add(cache.prev(cache.body(), 1).dividedBy(atrIndicator));
        indicators.add(cache.prev(cache.spread(), 1).dividedBy(atrIndicator));
        indicators.add(cache.prev(cache.upperWick(), 1).dividedBy(atrIndicator));
        indicators.add(cache.prev(cache.lowerWick(), 1).dividedBy(atrIndicator));
        indicators.add(cache.prev(cache.body(), 1).dividedBy(cache.prev(cache.spread(), 1)));

        // Relationship of current bar with previous bar
        indicators.add(cache.trueRange().dividedBy(atrIndicator));
        indicators.add(cache.open().minus(cache.prev(cache.close(), 1)).dividedBy(atrIndicator));
        indicators.add(cache.high().minus(cache.prev(cache.high(), 1)).dividedBy(atrIndicator));
        indicators.add(cache.low().minus(cache.prev(cache.low(), 1)).dividedBy(atrIndicator));
        indicators.add(cache.close().minus(cache.prev(cache.close(), 1)).dividedBy(atrIndicator));
        indicators.add(cache.typicalPrice().minus(cache.prev(cache.typicalPrice(), 1)).dividedBy(atrIndicator));
        indicators.add(cache.ibs().dividedBy(cache.prev(cache.ibs(), 1)));
        indicators.add(cache.spread().dividedBy(cache.prev(cache.spread(), 1)));
        indicators.add(cache.upperWick().dividedBy(cache.prev(cache.upperWick(), 1)));
        indicators.add(cache.lowerWick().dividedBy(cache.prev(cache.lowerWick(), 1)));
        indicators.add(cache.body().dividedBy(cache.prev(cache.body(), 1)));
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
