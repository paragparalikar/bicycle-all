package com.bicycle.core.rule.builder;

import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.rule.WaitForBarCountRule;
import com.bicycle.util.IntegerIterator;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public class WaitForBarCountRuleBuilder implements RuleBuilder {

    @Builder.Default private final IntegerIterator integerIterator = new IntegerIterator("barCount", 15, 15, 15, 1);

    @Override
    public Rule build(IndicatorCache indicatorCache) {
        return new WaitForBarCountRule(integerIterator.value(), indicatorCache);
    }

    @Override
    public Rule buildDefault(IndicatorCache indicatorCache) {
        return new WaitForBarCountRule(integerIterator.defaultValue(), indicatorCache);
    }
    
    @Override
    public String toString() {
        return "after " + integerIterator.toValueString() + " bars";
    }

}
