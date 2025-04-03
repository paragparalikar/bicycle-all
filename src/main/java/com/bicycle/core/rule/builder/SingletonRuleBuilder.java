package com.bicycle.core.rule.builder;

import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.rule.Rule;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class SingletonRuleBuilder implements RuleBuilder {
    
    private Rule rule;
    private final Function<IndicatorCache, Rule> builder;

    @Override
    public Rule build(IndicatorCache indicatorCache) {
        return buildDefault(indicatorCache);
    }

    @Override
    public Rule buildDefault(IndicatorCache indicatorCache) {
        return null == rule ? rule = builder.apply(indicatorCache) : rule;
    }
    
    @Override
    public String toString() {
        return String.valueOf(rule);
    }

}
