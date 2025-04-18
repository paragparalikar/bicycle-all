package com.bicycle.core.rule.builder;

import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.rule.Rule;

public interface RuleBuilder {

    Rule build(IndicatorCache indicatorCache);
    
    Rule buildDefault(IndicatorCache indicatorCache);
    
    default RuleBuilder and(RuleBuilder other) {
        return new AndRuleBuilder(this, other);
    }
    
    default RuleBuilder or(RuleBuilder other) {
        return new OrRuleBuilder(this, other);
    }
    
    default RuleBuilder not() {
        return new NotRuleBuilder(this);
    }
    
}
