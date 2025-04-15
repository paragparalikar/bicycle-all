package com.bicycle.backtest.feature.captor;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.position.Position;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.SymbolInfo;

import java.util.Arrays;
import java.util.List;

public class SymbolFeatureCaptor implements FeatureCaptor {

    private final Indicator efficiency, volatility, spread, volume, turnover;

    public SymbolFeatureCaptor(IndicatorCache cache, int barCount){
        this.efficiency = cache.crossNormalDeviation(cache.ema(cache.efficiency(14), barCount));
        this.volatility = cache.crossNormalDeviation(cache.ema(cache.stdDev(cache.close(), 14), barCount));
        this.spread = cache.crossNormalDeviation(cache.ema(cache.spread(), barCount));
        this.volume = cache.crossNormalDeviation(cache.ema(cache.volume(), barCount));
        this.turnover = cache.crossNormalDeviation(cache.ema(cache.volume().dividedBy(1000).multipliedBy(cache.typicalPrice()), barCount));
    }

    @Override
    public void captureHeaders(List<String> headers) {
        headers.addAll(Arrays.asList("SYMBOL_EFFICIENCY", "SYMBOL_VOLATILITY", "SYMBOL_SPREAD", "SYMBOL_VOLUME", "SYMBOL_TURNOVER"));
    }

    @Override
    public void captureValues(Position position, List<Float> values) {
        final Symbol symbol = position.getSymbol();
        final Timeframe timeframe = position.getTimeframe();
        values.add(efficiency.getValue(symbol, timeframe));
        values.add(volatility.getValue(symbol, timeframe));
        values.add(spread.getValue(symbol, timeframe));
        values.add(volume.getValue(symbol, timeframe));
        values.add(turnover.getValue(symbol, timeframe));
    }
}
