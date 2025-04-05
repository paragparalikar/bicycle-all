package com.bicycle.backtest.feature;

import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.rule.builder.RuleBuilder;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FeatureWriterRuleBuilder implements RuleBuilder {

    private final List<Float> values;
    private final List<String> headers;
    private final RuleBuilder delegate;
    private final FeatureWriter featureWriter;

    @Override
    public Rule build(IndicatorCache indicatorCache) {
        final Rule rule = delegate.build(indicatorCache);
        featureWriter.writeHeaders(headers);
        return new FeatureWriterRule(rule, values, featureWriter);
    }

    @Override
    public Rule buildDefault(IndicatorCache indicatorCache) {
        final Rule rule = delegate.buildDefault(indicatorCache);
        featureWriter.writeHeaders(headers);
        return new FeatureWriterRule(rule, values, featureWriter);
    }
}
