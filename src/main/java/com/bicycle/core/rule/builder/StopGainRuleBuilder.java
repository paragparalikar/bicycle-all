package com.bicycle.core.rule.builder;

import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.rule.StopGainRule;
import com.bicycle.util.FloatIterator;
import com.bicycle.util.IntegerIterator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StopGainRuleBuilder implements RuleBuilder {
    
    private final FloatIterator atrMultipleIterator;
    private final IntegerIterator barCountIterator;

    @Override
    public Rule build(IndicatorCache indicatorCache) {
        return new StopGainRule(atrMultipleIterator.value(), indicatorCache.atr(barCountIterator.value()));
    }

    @Override
    public Rule buildDefault(IndicatorCache indicatorCache) {
        return new StopGainRule(atrMultipleIterator.defaultValue(), indicatorCache.atr(barCountIterator.defaultValue()));
    }

    @Override
    public String toString() {
        return "stopGain(" + atrMultipleIterator.toValueString() + ")";
    }
    
}
