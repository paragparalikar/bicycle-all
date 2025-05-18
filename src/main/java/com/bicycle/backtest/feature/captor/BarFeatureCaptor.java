package com.bicycle.backtest.feature.captor;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.position.Position;

import java.util.ArrayList;
import java.util.List;

public class BarFeatureCaptor implements FeatureCaptor {

    private final List<Indicator> indicators = new ArrayList<>();

    public BarFeatureCaptor(IndicatorCache cache, int barCount){

        // Features for current bar
        indicators.add(cache.ibs());
        addAll(barCount, cache, cache.body(), cache.spread(), cache.upperWick(), cache.lowerWick());

        // Features for previous bar
        indicators.addAll(indicators.stream().map(feature -> cache.prev(feature, 1)).toList());

        // Relationship of current bar with previous bar
        addAll(barCount, cache, cache.trueRange(), cache.ibs(),
                cache.ibs().minus(cache.prev(cache.ibs(),  1)),
                cache.typicalPrice().minus(cache.prev(cache.typicalPrice(), 1)),
                cache.trueRange().minus(cache.prev(cache.trueRange(), 1)),
                cache.open().minus(cache.prev(cache.open(), 1)),
                cache.open().minus(cache.prev(cache.close(), 1)),
                cache.open().minus(cache.prev(cache.high(), 1)),
                cache.open().minus(cache.prev(cache.low(), 1)),
                cache.high().minus(cache.prev(cache.open(), 1)),
                cache.high().minus(cache.prev(cache.close(), 1)),
                cache.high().minus(cache.prev(cache.high(), 1)),
                cache.high().minus(cache.prev(cache.low(), 1)),
                cache.low().minus(cache.prev(cache.open(), 1)),
                cache.low().minus(cache.prev(cache.close(), 1)),
                cache.low().minus(cache.prev(cache.high(), 1)),
                cache.low().minus(cache.prev(cache.low(), 1)),
                cache.close().minus(cache.prev(cache.open(), 1)),
                cache.close().minus(cache.prev(cache.close(), 1)),
                cache.close().minus(cache.prev(cache.high(), 1)),
                cache.close().minus(cache.prev(cache.low(), 1)));
    }

    private void addAll(int barCount, IndicatorCache cache, Indicator...indicators){
        for(Indicator indicator : indicators) {
            final Indicator emaIndicator = cache.ema(indicator, barCount);
            final Indicator prevEmaIndicator = cache.prev(emaIndicator, 1);
            final Indicator ratioWithPrevEmaIndicator = indicator.dividedBy(prevEmaIndicator);
            this.indicators.add(ratioWithPrevEmaIndicator);
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
