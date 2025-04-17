package com.bicycle.backtest.feature;

import com.bicycle.backtest.feature.captor.FeatureCaptor;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.backtest.report.cache.InterceptableReportCache;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.strategy.positionSizing.PositionSizingStrategy;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.builder.RuleBuilder;
import lombok.Builder;

import java.util.List;

public class FeatureAwareTradingStrategyBuilder implements TradingStrategyBuilder {


    private final OrderType entryOrderType;
    private final FeatureWriter featureWriter;
    private final RuleBuilder entryRuleBuilder, exitRuleBuilder;
    private final FeatureCaptor.Builder entryFeatureCaptorBuilder, exitFeatureCaptorBuilder;

    @Builder
    public FeatureAwareTradingStrategyBuilder(OrderType entryOrderType,
                                              RuleBuilder entryRuleBuilder,
                                              RuleBuilder exitRuleBuilder,
                                              FeatureCaptor.Builder entryFeatureCaptorBuilder,
                                              FeatureCaptor.Builder exitFeatureCaptorBuilder,
                                              FeatureWriter featureWriter){
        this.featureWriter = featureWriter;
        this.entryOrderType = entryOrderType;
        this.entryRuleBuilder = entryRuleBuilder;
        this.exitRuleBuilder = exitRuleBuilder;
        this.entryFeatureCaptorBuilder = entryFeatureCaptorBuilder;
        this.exitFeatureCaptorBuilder = exitFeatureCaptorBuilder;
    }

    @Override
    public List<MockTradingStrategy> build(float slippagePercentage, IndicatorCache indicatorCache, ReportCache reportCache, PositionSizingStrategy positionSizingStrategy) {
        return List.of(buildDefault(slippagePercentage, indicatorCache, reportCache, positionSizingStrategy));
    }

    @Override
    public MockTradingStrategy buildDefault(float slippagePercentage, IndicatorCache indicatorCache, ReportCache reportCache, PositionSizingStrategy positionSizingStrategy) {
        final FeatureReportObserver featureReportObserver = new FeatureReportObserver(
                entryFeatureCaptorBuilder.build(indicatorCache),
                exitFeatureCaptorBuilder.build(indicatorCache),
                featureWriter);
        final InterceptableReportCache.Interceptor featureReportCacheInterceptor =
                new FeatureReportCacheInterceptor(featureReportObserver);
        reportCache = new InterceptableReportCache(reportCache, featureReportCacheInterceptor);
        return new MockTradingStrategy(slippagePercentage,
                entryRuleBuilder.buildDefault(indicatorCache),
                exitRuleBuilder.buildDefault(indicatorCache),
                entryOrderType, reportCache, indicatorCache.atr(14), positionSizingStrategy);
    }
}
