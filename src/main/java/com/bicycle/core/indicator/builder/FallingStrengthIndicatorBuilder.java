package com.bicycle.core.indicator.builder;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.util.IntegerIterator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FallingStrengthIndicatorBuilder implements IndicatorBuilder {
    
    private final IndicatorBuilder sourceIndicatorBuilder;
    private final IntegerIterator integerIterator;

    @Override
    public Indicator build(IndicatorCache indicatorCache) {
        return indicatorCache.fallingStrength(sourceIndicatorBuilder.build(indicatorCache), integerIterator.value());
    }

    @Override
    public Indicator buildDefault(IndicatorCache indicatorCache) {
        return indicatorCache.fallingStrength(sourceIndicatorBuilder.buildDefault(indicatorCache), integerIterator.defaultValue());
    }
    
    @Override
    public String toString() {
        return "fallingStrength(" + sourceIndicatorBuilder + "," + integerIterator.toValueString() + ")";
    }

}
