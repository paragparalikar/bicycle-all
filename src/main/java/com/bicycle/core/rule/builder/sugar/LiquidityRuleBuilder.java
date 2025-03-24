package com.bicycle.core.rule.builder.sugar;

import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.util.FloatIterator;
import com.bicycle.util.IntegerIterator;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public class LiquidityRuleBuilder implements RuleBuilder {

    private final FloatIterator minPriceIterator;
    private final IntegerIterator minVolumeIterator;
    private final IntegerIterator smaBarCountIterator;

    @Override
    public Rule build(IndicatorCache indicatorCache) {
        return indicatorCache.sma(indicatorCache.close(), smaBarCountIterator.value()).greaterThan(minPriceIterator.value())
                .and(indicatorCache.sma(indicatorCache.volume(), smaBarCountIterator.value()).greaterThan(minVolumeIterator.value()));
    }

    @Override
    public Rule buildDefault(IndicatorCache indicatorCache) {
        return indicatorCache.sma(indicatorCache.close(), smaBarCountIterator.defaultValue()).greaterThan(minPriceIterator.defaultValue())
                .and(indicatorCache.sma(indicatorCache.volume(), smaBarCountIterator.defaultValue()).greaterThan(minVolumeIterator.defaultValue()));
    }
}
