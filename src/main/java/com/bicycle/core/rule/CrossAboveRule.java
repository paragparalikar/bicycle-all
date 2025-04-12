package com.bicycle.core.rule;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.position.Position;
import com.bicycle.core.symbol.Symbol;

public class CrossAboveRule implements Rule {

    private final Rule delegate;

    public CrossAboveRule(Indicator first, Indicator second, IndicatorCache cache) {
        this.delegate = first.greaterThanOrEquals(second)
                .and(first.prev(cache, 1).lesserThan(second.prev(cache, 1)));
    }

    @Override
    public boolean isSatisfied(Symbol symbol, Timeframe timeframe, Position trade) {
        return delegate.isSatisfied(symbol, timeframe, trade);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
