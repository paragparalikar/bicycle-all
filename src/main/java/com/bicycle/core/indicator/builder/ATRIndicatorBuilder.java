package com.bicycle.core.indicator.builder;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.util.IntegerIterator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ATRIndicatorBuilder implements IndicatorBuilder {
    
    private final IntegerIterator integerIterator;

    @Override
    public Indicator build(IndicatorCache indicatorCache) {
        return indicatorCache.atr(integerIterator.value());
    }

    @Override
    public Indicator buildDefault(IndicatorCache indicatorCache) {
        return indicatorCache.atr(integerIterator.defaultValue());
    }
    
    @Override
    public String toString() {
        return "atr(" + integerIterator.toValueString() + ")";
    }

}
