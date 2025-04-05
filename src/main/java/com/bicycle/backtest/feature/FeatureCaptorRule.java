package com.bicycle.backtest.feature;

import com.bicycle.backtest.feature.captor.FeatureCaptor;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.position.Position;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.symbol.Symbol;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FeatureCaptorRule implements Rule {

    private final Rule delegate;
    private final List<Float> values;
    private final FeatureCaptor featureCaptor;

    @Override
    public boolean isSatisfied(Symbol symbol, Timeframe timeframe, Position trade) {
        if(delegate.isSatisfied(symbol, timeframe, trade)){
            featureCaptor.captureValues(symbol, timeframe, trade, values);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
