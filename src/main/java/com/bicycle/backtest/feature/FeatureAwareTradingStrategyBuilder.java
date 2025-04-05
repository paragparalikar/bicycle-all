package com.bicycle.backtest.feature;

import com.bicycle.backtest.feature.captor.FeatureCaptor;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.strategy.positionSizing.PositionSizingStrategy;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.builder.RuleBuilder;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

public class FeatureAwareTradingStrategyBuilder implements TradingStrategyBuilder {

    private final OrderType entryOrderType;
    private MockTradingStrategy tradingStrategy;
    private final RuleBuilder entryRuleBuilder, exitRuleBuilder;

    @Builder
    public FeatureAwareTradingStrategyBuilder(OrderType entryOrderType,
                                              RuleBuilder entryRuleBuilder,
                                              RuleBuilder exitRuleBuilder,
                                              FeatureCaptor.Builder entryFeatureCaptorBuilder,
                                              FeatureCaptor.Builder exitFeatureCaptorBuilder,
                                              FeatureWriter featureWriter){
        this.entryOrderType = entryOrderType;
        final List<Float> values = new ArrayList<>();
        final List<String> headers = new ArrayList<>();
        this.entryRuleBuilder = new FeatureCaptorRuleBuilder(values, headers, entryRuleBuilder, entryFeatureCaptorBuilder);
        this.exitRuleBuilder = new FeatureWriterRuleBuilder(values, headers,
                new FeatureCaptorRuleBuilder(values, headers, exitRuleBuilder, exitFeatureCaptorBuilder), featureWriter);
    }

    @Override
    public List<MockTradingStrategy> build(float slippagePercentage, IndicatorCache indicatorCache, ReportCache reportCache, PositionSizingStrategy positionSizingStrategy) {
        return List.of(buildDefault(slippagePercentage, indicatorCache, reportCache, positionSizingStrategy));
    }

    @Override
    public MockTradingStrategy buildDefault(float slippagePercentage, IndicatorCache indicatorCache, ReportCache reportCache, PositionSizingStrategy positionSizingStrategy) {
        return null == tradingStrategy ? tradingStrategy = new MockTradingStrategy(slippagePercentage,
                entryRuleBuilder.buildDefault(indicatorCache),
                exitRuleBuilder.buildDefault(indicatorCache),
                entryOrderType, reportCache, positionSizingStrategy)
                : tradingStrategy;
    }
}
