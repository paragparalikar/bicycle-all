package com.bicycle.core.indicator.builder;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;

public class LowPriceIndicatorBuilder  implements IndicatorBuilder {

    @Override
    public Indicator build(IndicatorCache indicatorCache) {
        return buildDefault(indicatorCache);
    }

    @Override
    public Indicator buildDefault(IndicatorCache indicatorCache) {
        return indicatorCache.low();
    }

    @Override
    public String toString() {
        return "low";
    }
}

