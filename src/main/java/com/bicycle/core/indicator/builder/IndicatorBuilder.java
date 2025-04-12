package com.bicycle.core.indicator.builder;

import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.indicator.IndicatorOperator;
import com.bicycle.core.rule.builder.*;

public interface IndicatorBuilder {

    Indicator build(IndicatorCache indicatorCache);
    
    Indicator buildDefault(IndicatorCache indicatorCache);
    
    default IndicatorBuilder plus(IndicatorBuilder other) {
        return new CombinedIndicatorBuilder(this, other, IndicatorOperator.PLUS);
    }
    
    default IndicatorBuilder minus(IndicatorBuilder other) {
        return new CombinedIndicatorBuilder(this, other, IndicatorOperator.MINUS);
    }
    
    default IndicatorBuilder multipliedBy(IndicatorBuilder other) {
        return new CombinedIndicatorBuilder(this, other, IndicatorOperator.MULTIPLIED_BY);
    }
    
    default IndicatorBuilder dividedBy(IndicatorBuilder other) {
        return new CombinedIndicatorBuilder(this, other, IndicatorOperator.DIVIDED_BY);
    }
    
    default IndicatorBuilder pow(IndicatorBuilder other) {
        return new CombinedIndicatorBuilder(this, other, IndicatorOperator.POWER_OF);
    }

    default RuleBuilder equals(IndicatorBuilder other){
        return new EqualsRuleBuilder(this, other);
    }

    default RuleBuilder greaterThan(IndicatorBuilder other){
        return new GreaterThanRuleBuilder(this, other);
    }

    default RuleBuilder greaterThanOrEquals(IndicatorBuilder other){
        return greaterThan(other).or(equals(other));
    }

    default RuleBuilder lesserThan(IndicatorBuilder other){
        return new LesserThanRuleBuilder(this, other);
    }

    default RuleBuilder lesserThanOrEquals(IndicatorBuilder other){
        return lesserThan(other).or(equals(other));
    }

    default RuleBuilder crossedAbove(IndicatorBuilder other){
        return new CrossAboveRuleBuilder(this, other);
    }

    default  RuleBuilder crossedBelow(IndicatorBuilder other){
        return new CrossBelowRuleBuilder(this, other);
    }

}
