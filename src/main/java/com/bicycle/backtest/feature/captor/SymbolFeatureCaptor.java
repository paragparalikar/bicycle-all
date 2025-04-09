package com.bicycle.backtest.feature.captor;

import com.bicycle.core.position.Position;
import com.bicycle.core.symbol.SymbolInfo;

import java.util.Arrays;
import java.util.List;

public class SymbolFeatureCaptor implements FeatureCaptor {

    @Override
    public void captureHeaders(List<String> headers) {
        headers.addAll(Arrays.asList("SYMBOL_EFFICIENCY", "SYMBOL_VOLATILITY", "SYMBOL_SPREAD", "SYMBOL_VOLUME", "SYMBOL_TURNOVER"));
    }

    @Override
    public void captureValues(Position position, List<Float> values) {
        final SymbolInfo info = position.getSymbol().info();
        if(null != info){
            values.add((float) info.efficiency().level().ordinal());
            values.add((float) info.volatility().level().ordinal());
            values.add((float) info.spread().level().ordinal());
            values.add((float) info.volume().level().ordinal());
            values.add((float) info.turnover().level().ordinal());
        }
    }
}
