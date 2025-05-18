package com.bicycle.backtest.workflow.stage.feature;

import com.bicycle.backtest.Backtest;
import com.bicycle.backtest.feature.FeatureAwareTradingStrategyBuilder;
import com.bicycle.backtest.feature.captor.*;
import com.bicycle.backtest.feature.writer.CompositeFeatureWriter;
import com.bicycle.backtest.feature.writer.DataFrameFeatureWriter;
import com.bicycle.backtest.feature.writer.DelimitedFileFeatureWriter;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.backtest.report.AccumulatorReport;
import com.bicycle.backtest.report.FullReport;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.report.cache.SingletonReportCache;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.util.Dates;
import smile.data.DataFrame;

public class FeatureGenerationStage {

    public DataFrame execute(OrderType orderType, RuleBuilder entryRuleBuilder, RuleBuilder exitRuleBuilder) throws Exception {
        final long startDate = Dates.toEpochMillis(2020, 1, 1);
        final long endDate = Dates.toEpochMillis(2020, 12, 31);
        final float percentagePositionSize = 2;
        final boolean limitPositionSizeToAvailableMargin = false;
        return execute(orderType, entryRuleBuilder, exitRuleBuilder, startDate, endDate, percentagePositionSize, limitPositionSizeToAvailableMargin);
    }

    public DataFrame execute(OrderType orderType, RuleBuilder entryRuleBuilder, RuleBuilder exitRuleBuilder,
                             long startDate, long endDate, float percentagePositionSize, boolean limitPositionSizeToAvailableMargin) throws Exception {
        System.out.println("\n--------------- Initiating feature selection stage ---------------");
        System.out.printf("Using below configuration for feature generation:\n" +
                "Start Date          : %s\n" +
                "End Date            : %s\n" +
                "Position Size       : %4.2f\n" +
                "Limit position size : %b\n", Dates.format(startDate), Dates.format(endDate), percentagePositionSize, limitPositionSizeToAvailableMargin);
        try(DataFrameFeatureWriter dataFrameFeatureWriter = new DataFrameFeatureWriter();
            FeatureWriter fileFeatureWriter = new DelimitedFileFeatureWriter("features.tsv","\t");
            FeatureWriter featureWriter = new CompositeFeatureWriter(dataFrameFeatureWriter, fileFeatureWriter)){
            final TradingStrategyBuilder tradingStrategyBuilder = createTradingStrategyBuilder(orderType, entryRuleBuilder, exitRuleBuilder, featureWriter);
            final Backtest backtest = new Backtest()
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .setReportBuilder(AccumulatorReport.builder(FullReport.builder()))
                    .setPercentagePositionSize(percentagePositionSize)
                    .setLimitPositionSizeToAvailableMargin(limitPositionSizeToAvailableMargin)
                    .setTradingStrategyBuilder(tradingStrategyBuilder);
            final ReportCache reportCache = backtest.run();
            System.out.println(((SingletonReportCache) reportCache).getReport());
            final DataFrame dataFrame = dataFrameFeatureWriter.getDataFrame();
            System.out.printf("Generated %d features and %d rows\n", dataFrame.ncol(), dataFrame.nrow());
            return dataFrame;
        }
    }

    private TradingStrategyBuilder createTradingStrategyBuilder(OrderType orderType, RuleBuilder entryRuleBuilder,
                                                                RuleBuilder exitRuleBuilder, FeatureWriter featureWriter){
        final FeatureCaptor.Builder entryFeatureCaptorBuilder = createEntryFeatureCaptorBuilder();
        final FeatureCaptor.Builder exitFeatureCaptorBuilder = cache -> new PositionFeatureCaptor();
        return FeatureAwareTradingStrategyBuilder.builder()
                .entryOrderType(orderType)
                .entryRuleBuilder(entryRuleBuilder)
                .entryFeatureCaptorBuilder(entryFeatureCaptorBuilder)
                .exitRuleBuilder(exitRuleBuilder)
                .exitFeatureCaptorBuilder(exitFeatureCaptorBuilder)
                .featureWriter(featureWriter)
                .build();
    }

    private FeatureCaptor.Builder createEntryFeatureCaptorBuilder() {
        final int barCount = 5;
        final float multiplier = 4;
        final int[] barCounts = new int[]{5, 10, 15, 20, 25, 30, 40, 50};
        return cache -> new CompositeFeatureCaptor(
                new SymbolFeatureCaptor(cache, 50),
                new BarFeatureCaptor(cache, barCount),
                new BarSequenceFeatureCaptor(cache, barCounts),
                new EfficiencyFeatureCaptor(cache, barCounts),
                new TrendFeatureCaptor(cache, multiplier, barCounts),
                new VolatilityFeatureCaptor(cache, multiplier, barCounts),
                new VolumeFeatureCaptor(cache, multiplier, barCounts)
        );
    }

}
