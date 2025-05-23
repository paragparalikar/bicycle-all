package com.bicycle.core.rule.builder;

import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.indicator.builder.IndicatorBuilder;
import com.bicycle.core.rule.CrossBelowRule;
import com.bicycle.core.rule.Rule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CrossBelowRuleBuilder  implements RuleBuilder {

    private final IndicatorBuilder left, right;

    @Override
    public Rule build(IndicatorCache indicatorCache) {
        return new CrossBelowRule(left.build(indicatorCache), right.build(indicatorCache), indicatorCache);
    }

    @Override
    public Rule buildDefault(IndicatorCache indicatorCache) {
        return new CrossBelowRule(left.buildDefault(indicatorCache), right.buildDefault(indicatorCache), indicatorCache);
    }

    @Override
    public String toString() {
        return left + " x< " + right;
    }

}
