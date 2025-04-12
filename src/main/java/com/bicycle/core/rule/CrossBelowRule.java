package com.bicycle.core.rule;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.position.Position;
import com.bicycle.core.symbol.Symbol;

public class CrossBelowRule implements Rule {

    private final Rule delegate;

    public CrossBelowRule(Indicator first, Indicator second, IndicatorCache cache){
        this.delegate = first.lesserThanOrEquals(second)
                .and(first.prev(cache, 1).greaterThan(second.prev(cache, 1)));
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