package com.bicycle.backtest.feature.captor;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.position.Position;

import java.util.ArrayList;
import java.util.List;

public class BarFeatureCaptor implements FeatureCaptor {

    private final List<Indicator> indicators = new ArrayList<>();

    public BarFeatureCaptor(IndicatorCache cache, int barCount){
        final Indicator atrIndicator = cache.prev(cache.atr(barCount), 1);

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

        // Relationship of current bar with last N bars
        final Indicator tpEMA = cache.prev(cache.ema(cache.typicalPrice(), barCount), 1);
        final Indicator openEMA = cache.prev(cache.ema(cache.open(), barCount), 1);
        final Indicator highEMA = cache.prev(cache.ema(cache.high(), barCount), 1);
        final Indicator lowEMA = cache.prev(cache.ema(cache.low(), barCount), 1);
        final Indicator closeEMA = cache.prev(cache.ema(cache.close(), barCount), 1);
        final Indicator ibsEMA = cache.prev(cache.ema(cache.ibs(), barCount), 1);
        final Indicator spreadEMA = cache.prev(cache.ema(cache.spread(), barCount), 1);
        final Indicator upperWickEMA = cache.prev(cache.ema(cache.upperWick(), barCount), 1);
        final Indicator lowerWickEMA = cache.prev(cache.ema(cache.lowerWick(), barCount), 1);
        final Indicator bodyEMA = cache.prev(cache.ema(cache.body(), barCount), 1);

        indicators.add(cache.typicalPrice().minus(tpEMA).dividedBy(atrIndicator));
        indicators.add(cache.typicalPrice().minus(highEMA).dividedBy(atrIndicator));
        indicators.add(cache.typicalPrice().minus(lowEMA).dividedBy(atrIndicator));
        indicators.add(cache.open().minus(openEMA).dividedBy(atrIndicator));
        indicators.add(cache.high().minus(highEMA).dividedBy(atrIndicator));
        indicators.add(cache.high().minus(tpEMA).dividedBy(atrIndicator));
        indicators.add(cache.high().minus(lowEMA).dividedBy(atrIndicator));
        indicators.add(cache.low().minus(lowEMA).dividedBy(atrIndicator));
        indicators.add(cache.low().minus(tpEMA).dividedBy(atrIndicator));
        indicators.add(cache.low().minus(highEMA).dividedBy(atrIndicator));
        indicators.add(cache.close().minus(closeEMA).dividedBy(atrIndicator));
        indicators.add(cache.close().minus(highEMA).dividedBy(atrIndicator));
        indicators.add(cache.close().minus(lowEMA).dividedBy(atrIndicator));
        indicators.add(cache.close().minus(tpEMA).dividedBy(atrIndicator));
        indicators.add(cache.spread().dividedBy(spreadEMA));
        indicators.add(cache.upperWick().dividedBy(upperWickEMA));
        indicators.add(cache.lowerWick().dividedBy(lowerWickEMA));
        indicators.add(cache.body().dividedBy(bodyEMA));
        indicators.add(cache.ibs().dividedBy(ibsEMA));
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
