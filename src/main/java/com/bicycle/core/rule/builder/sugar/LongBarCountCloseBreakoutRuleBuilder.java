package com.bicycle.core.rule.builder.sugar;

import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.util.IntegerIterator;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public class LongBarCountCloseBreakoutRuleBuilder implements RuleBuilder {

    private final IntegerIterator barCountIterator;

    @Override
    public Rule build(IndicatorCache indicatorCache) {
        return indicatorCache.close().greaterThan(indicatorCache.prev(indicatorCache.highest(indicatorCache.close(), barCountIterator.value()), 1));
    }

    @Override
    public Rule buildDefault(IndicatorCache indicatorCache) {
        return indicatorCache.close().greaterThan(indicatorCache.prev(indicatorCache.highest(indicatorCache.close(), barCountIterator.defaultValue()), 1));
    }
}
