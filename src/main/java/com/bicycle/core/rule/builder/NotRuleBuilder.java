package com.bicycle.core.rule.builder;

import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.rule.NotRule;
import com.bicycle.core.rule.Rule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotRuleBuilder implements RuleBuilder {
    
    private final RuleBuilder delegate;

    @Override
    public Rule build(IndicatorCache indicatorCache) {
        return new NotRule(delegate.build(indicatorCache));
    }

    @Override
    public Rule buildDefault(IndicatorCache indicatorCache) {
        return new NotRule(delegate.buildDefault(indicatorCache));
    }
    
    @Override
    public String toString() {
        return "not " + delegate;
    }

}
