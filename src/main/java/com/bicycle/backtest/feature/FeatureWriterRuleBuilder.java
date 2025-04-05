package com.bicycle.backtest.feature;

import com.bicycle.backtest.feature.captor.FeatureCaptor;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.rule.builder.RuleBuilder;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FeatureWriterRuleBuilder implements RuleBuilder {

    private final List<Float> values;
    private final RuleBuilder delegate;
    private final FeatureWriter featureWriter;

    @Override
    public Rule build(IndicatorCache indicatorCache) {
        return new FeatureWriterRule(delegate.build(indicatorCache), values, featureWriter);
    }

    @Override
    public Rule buildDefault(IndicatorCache indicatorCache) {
        return new FeatureWriterRule(delegate.buildDefault(indicatorCache), values, featureWriter);
    }
}
