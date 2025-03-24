package com.bicycle.core.indicator.builder;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GainIndicatorBuilder implements IndicatorBuilder {

    private final IndicatorBuilder sourceIndicatorBuilder;

    @Override
    public Indicator build(IndicatorCache indicatorCache) {
        return indicatorCache.gain(sourceIndicatorBuilder.build(indicatorCache));
    }

    @Override
    public Indicator buildDefault(IndicatorCache indicatorCache) {
        return indicatorCache.gain(sourceIndicatorBuilder.buildDefault(indicatorCache));
    }
    
    @Override
    public String toString() {
        return "gain(" + sourceIndicatorBuilder + ")";
    }

}
