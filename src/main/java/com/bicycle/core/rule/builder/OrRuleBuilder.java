package com.bicycle.core.rule.builder;

import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.rule.OrRule;
import com.bicycle.core.rule.Rule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrRuleBuilder implements RuleBuilder {
    
    private final RuleBuilder left, right;

    @Override
    public Rule build(IndicatorCache indicatorCache) {
        return new OrRule(left.build(indicatorCache), right.build(indicatorCache));
    }

    @Override
    public Rule buildDefault(IndicatorCache indicatorCache) {
        return new OrRule(left.buildDefault(indicatorCache), right.buildDefault(indicatorCache));
    }
    
    @Override
    public String toString() {
        return left + " or " + right;
    }

}
