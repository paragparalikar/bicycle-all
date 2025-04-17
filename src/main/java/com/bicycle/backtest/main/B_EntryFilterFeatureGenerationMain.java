package com.bicycle.backtest.main;

import com.bicycle.backtest.Backtest;
import com.bicycle.backtest.feature.FeatureAwareTradingStrategyBuilder;
import com.bicycle.backtest.feature.captor.*;
import com.bicycle.backtest.feature.writer.DelimitedFileFeatureWriter;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.backtest.report.AccumulatorReport;
import com.bicycle.backtest.report.FullReport;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.report.cache.SingletonReportCache;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.LiquidityRule;
import com.bicycle.core.rule.WaitForBarCountRule;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.core.rule.builder.SingletonRuleBuilder;
import com.bicycle.util.Dates;

public class B_EntryFilterFeatureGenerationMain {

    public static void main(String[] args) throws Exception {
        try(final FeatureWriter featureWriter = new DelimitedFileFeatureWriter("entry-features.tsv", "\t")){
            final TradingStrategyBuilder tradingStrategyBuilder = createTradingStrategyBuilder(featureWriter);
            final Backtest backtest = new Backtest()
                    .setStartDate(Dates.toEpochMillis(2010, 1, 1))
                    .setEndDate(Dates.toEpochMillis(2020, 12, 31))
                    .setReportBuilder(AccumulatorReport.builder(FullReport.builder()))
                    .setPercentagePositionSize(2)
                    .setLimitPositionSizeToAvailableMargin(false)
                    .setTradingStrategyBuilder(tradingStrategyBuilder);
            final ReportCache reportCache = backtest.run();
            System.out.println(((SingletonReportCache) reportCache).getReport());
        }
    }

    private static TradingStrategyBuilder createTradingStrategyBuilder(FeatureWriter featureWriter){
        final FeatureCaptor.Builder entryFeatureCaptorBuilder = createEntryFeatureCaptorBuilder();
        final FeatureCaptor.Builder exitFeatureCaptorBuidler = cache -> new PositionFeatureCaptor();
        final RuleBuilder entryRuleBuilder = new SingletonRuleBuilder(cache -> new LiquidityRule(cache)
                .and(cache.close().greaterThanOrEquals(cache.ema(cache.close(), 200)))
                .and(cache.rsi(cache.close(), 3).crossAbove(cache.constant(15), cache))
        );
        final RuleBuilder exitRuleBuilder = new SingletonRuleBuilder(cache -> new WaitForBarCountRule(22, cache));
        return FeatureAwareTradingStrategyBuilder.builder()
                .entryOrderType(OrderType.BUY)
                .entryRuleBuilder(entryRuleBuilder)
                .entryFeatureCaptorBuilder(entryFeatureCaptorBuilder)
                .exitRuleBuilder(exitRuleBuilder)
                .exitFeatureCaptorBuilder(exitFeatureCaptorBuidler)
                .featureWriter(featureWriter)
                .build();
    }

    private static FeatureCaptor.Builder createEntryFeatureCaptorBuilder() {
        final int barCount = 5;
        final float multiplier = 4;
        final int[] barCounts = new int[]{5, 10, 15, 20, 25, 30, 40, 50};
        return cache -> new CompositeFeatureCaptor(
                new SymbolFeatureCaptor(cache, 50),
                new BarFeatureCaptor(cache, barCount),
                new BarSequenceFeatureCaptor(cache, barCounts),
                new TrendFeatureCaptor(cache, multiplier, barCounts),
                new VolatilityFeatureCaptor(cache, multiplier, barCounts),
                new VolumeFeatureCaptor(cache, multiplier, barCounts)
                );
    }

}
