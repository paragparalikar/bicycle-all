package com.bicycle.backtest.feature;

import com.bicycle.backtest.feature.captor.FeatureCaptor;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.rule.builder.RuleBuilder;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class FeatureCaptorRuleBuilder implements RuleBuilder {

    private final Map<String, List<Float>> values;
    private final List<String> headers;
    private final RuleBuilder delegate;
    private final FeatureCaptor.Builder featureCaptorBuilder;

    @Override
    public Rule build(IndicatorCache indicatorCache) {
        final FeatureCaptor featureCaptor = featureCaptorBuilder.build(indicatorCache);
        featureCaptor.captureHeaders(headers);
        return new FeatureCaptorRule(delegate.build(indicatorCache), values, featureCaptor);
    }

    @Override
    public Rule buildDefault(IndicatorCache indicatorCache) {
        final FeatureCaptor featureCaptor = featureCaptorBuilder.build(indicatorCache);
        featureCaptor.captureHeaders(headers);
        return new FeatureCaptorRule(delegate.buildDefault(indicatorCache), values, featureCaptor);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
