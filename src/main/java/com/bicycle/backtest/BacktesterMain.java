package com.bicycle.backtest;

import com.bicycle.backtest.feature.captor.CompositeFeatureCaptor;
import com.bicycle.backtest.feature.captor.FeatureCaptor;
import com.bicycle.backtest.feature.captor.PositionFeatureCaptor;
import com.bicycle.backtest.feature.captor.VolatilityFeatureCaptor;
import com.bicycle.backtest.feature.writer.DelimitedFileFeatureWriter;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.backtest.report.AccumulatorReport;
import com.bicycle.backtest.report.FullReport;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.report.cache.SingletonReportCache;
import com.bicycle.backtest.strategy.trading.builder.RuleTradingStrategyBuilder;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.LiquidityRule;
import com.bicycle.core.rule.StopLossRule;
import com.bicycle.core.rule.WaitForBarCountRule;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.core.rule.builder.SingletonRuleBuilder;
import com.bicycle.util.Dates;

import java.util.List;

public class BacktesterMain {

    public static void main(String[] args) throws Exception {
        try(final FeatureWriter featureWriter = new DelimitedFileFeatureWriter("features.tsv", "\t")){
            final TradingStrategyBuilder tradingStrategyBuilder = createTradingStrategyBuilder(featureWriter);
            final Backtest backtest = new Backtest()
                    .setStartDate(Dates.toEpochMillis(2010, 1, 1))
                    .setEndDate(Dates.toEpochMillis(2024, 12, 31))
                    .setReportBuilder(AccumulatorReport.builder(FullReport.builder()))
                    .setPercentagePositionSize(2)
                    .setLimitPositionSizeToAvailableMargin(false)
                    .setTradingStrategyBuilder(tradingStrategyBuilder);
            final ReportCache reportCache = backtest.run();
            System.out.println(SingletonReportCache.class.cast(reportCache).getReport());
        }
    }

    private static TradingStrategyBuilder createTradingStrategyBuilder(FeatureWriter featureWriter){
        final FeatureCaptor.Builder entryFeatureCaptorBuilder = createEntryFeatureCaptorBuilder();
        final FeatureCaptor.Builder exitFeatureCaptorBuidler = cache -> new PositionFeatureCaptor();
        final RuleBuilder entryRuleBuilder = new SingletonRuleBuilder(cache -> {
            return new LiquidityRule(cache)
                    .and(cache.close().greaterThanOrEquals(cache.prev(cache.high(), 1)))
                    //.and(cache.prev(cache.close(), 1).lesserThan(cache.prev(cache.high(), 2)))
                    .and(cache.ibs().lesserThan(0.98f))
                    ;
        });
        final RuleBuilder exitRuleBuilder = new SingletonRuleBuilder(cache -> {
            final Indicator atrIndicator = cache.atr(21);
            return new StopLossRule(false, 2, atrIndicator)
                    .or(new StopLossRule(true, 2, atrIndicator))
                    .or(new WaitForBarCountRule(22, cache));
        });
        /*return FeatureAwareTradingStrategyBuilder.builder()
                .entryOrderType(OrderType.BUY)
                .entryRuleBuilder(entryRuleBuilder)
                .entryFeatureCaptorBuilder(entryFeatureCaptorBuilder)
                .exitRuleBuilder(exitRuleBuilder)
                .exitFeatureCaptorBuilder(exitFeatureCaptorBuidler)
                .featureWriter(featureWriter)
                .build();*/
        return new RuleTradingStrategyBuilder(entryRuleBuilder, exitRuleBuilder, OrderType.BUY, List.of());
    }

    private static FeatureCaptor.Builder createEntryFeatureCaptorBuilder() {
        final int barCount = 5;
        final float multiplier = 4;
        final int[] barCounts = new int[]{5, 10, 15, 20, 25, 30, 40, 50};
        return cache -> new CompositeFeatureCaptor(
                //new SymbolFeatureCaptor(),
                //new BarFeatureCaptor(cache, barCount),
                //new BarSequenceFeatureCaptor(cache, barCounts),
                //new TrendFeatureCaptor(cache, multiplier, barCounts)
                new VolatilityFeatureCaptor(cache, multiplier, barCounts)
                //new VolumeFeatureCaptor(cache, multiplier, barCounts)
                );
    }

}
