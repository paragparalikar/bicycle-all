package com.bicycle.backtest.feature;

import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.position.Position;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.symbol.Symbol;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FeatureWriterRule implements Rule {

    private final Rule delegate;
    private final List<Float> values;
    private final FeatureWriter featureWriter;

    @Override
    public boolean isSatisfied(Symbol symbol, Timeframe timeframe, Position trade) {
        if(delegate.isSatisfied(symbol, timeframe, trade)){
            featureWriter.writeValues(values);
            values.clear();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
