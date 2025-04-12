package com.bicycle.core.indicator.builder;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;

public class IbsIndicatorBuilder  implements IndicatorBuilder {

    @Override
    public Indicator build(IndicatorCache indicatorCache) {
        return buildDefault(indicatorCache);
    }

    @Override
    public Indicator buildDefault(IndicatorCache indicatorCache) {
        return indicatorCache.ibs();
    }

    @Override
    public String toString() {
        return "ibs";
    }
}

