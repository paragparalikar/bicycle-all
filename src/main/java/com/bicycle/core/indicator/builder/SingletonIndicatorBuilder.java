package com.bicycle.core.indicator.builder;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SingletonIndicatorBuilder implements IndicatorBuilder {

    private final Indicator indicator;
    
    @Override
    public Indicator build(IndicatorCache indicatorCache) {
        return indicator;
    }
    
    @Override
    public Indicator buildDefault(IndicatorCache indicatorCache) {
        return indicator;
    }
    
    @Override
    public String toString() {
        return indicator.toString();
    }

}
