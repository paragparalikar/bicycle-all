package com.bicycle.core.indicator.builder;

import com.bicycle.core.indicator.CombinedIndicator;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.indicator.IndicatorOperator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CombinedIndicatorBuilder implements IndicatorBuilder {

    private final IndicatorBuilder left, right;
    private final IndicatorOperator operator;
    
    @Override
    public Indicator build(IndicatorCache indicatorCache) {
        return new CombinedIndicator(left.build(indicatorCache), right.build(indicatorCache), operator);
    }

    @Override
    public Indicator buildDefault(IndicatorCache indicatorCache) {
        return new CombinedIndicator(left.buildDefault(indicatorCache), right.buildDefault(indicatorCache), operator);
    }

    @Override
    public String toString() {
        return left + " " + operator + " " + right;
    }
    
}
