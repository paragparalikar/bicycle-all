package com.bicycle.core.rule.builder;

import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.rule.Rule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SingletonRuleBuilder implements RuleBuilder {
    
    private final Rule rule;

    @Override
    public Rule build(IndicatorCache indicatorCache) {
        return rule;
    }

    @Override
    public Rule buildDefault(IndicatorCache indicatorCache) {
        return rule;
    }
    
    @Override
    public String toString() {
        return rule.toString();
    }

}
