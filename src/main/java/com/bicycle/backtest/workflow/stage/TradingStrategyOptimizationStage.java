package com.bicycle.backtest.workflow.stage;

import com.bicycle.backtest.Backtest;
import com.bicycle.backtest.feature.writer.DelimitedFileFeatureWriter;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.report.cache.TradingStrategyReportCache;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.backtest.strategy.trading.builder.RuleTradingStrategyBuilder;
import com.bicycle.util.Dates;
import com.bicycle.util.ResetableIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TradingStrategyOptimizationStage {

    public void execute(RuleTradingStrategyBuilder tradingStrategyBuilder) throws Exception {
        final String fileName = "trading-strategy-parameters.tsv";
        final String delimiter = "\t";
        final long startDate = Dates.toEpochMillis(2010, 1, 1);
        final long endDate = Dates.toEpochMillis(2020, 12, 31);
        final float percentPositionSize = 2;
        final boolean limitPositionSizeToAvailableMargin = false;
        execute(tradingStrategyBuilder, fileName, delimiter, startDate, endDate, percentPositionSize, limitPositionSizeToAvailableMargin);
    }

    public void execute(RuleTradingStrategyBuilder tradingStrategyBuilder, String fileName, String delimiter,
                        long startDate, long endDate, float percentagePositionSize,
                        boolean limitPositionSizeToAvailableMargin) throws Exception{
        try(final FeatureWriter featureWriter = new DelimitedFileFeatureWriter(fileName, delimiter)){
            featureWriter.writeHeaders(resolveHeaders(tradingStrategyBuilder));
            final Backtest backtest = new Backtest()
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .setPercentagePositionSize(percentagePositionSize)
                    .setReportCacheOptions(ReportCache.TRADING_STRATEGY)
                    .setLimitPositionSizeToAvailableMargin(limitPositionSizeToAvailableMargin)
                    .setTradingStrategyBuilder(tradingStrategyBuilder);
            final TradingStrategyReportCache reportCache = (TradingStrategyReportCache) backtest.run();
            final List<MockTradingStrategy> tradingStrategies = backtest.getTradingStrategies();
            final List<List<Float>> parameters = ResetableIterator.toList(tradingStrategyBuilder.getIterators());
            for(int index = 0; index < tradingStrategies.size(); index++){
                final List<Float> features = parameters.get(index);
                final Report report = reportCache.get(null, tradingStrategies.get(index));
                captureValues(features, report);
                featureWriter.writeValues((List)features);
            }
        }
    }

    private void captureValues(List<Float> values, Report report){
        values.add(report.getAverageMfe());
        values.add(report.getMaxDrawdown());
        values.add(report.getAvgDrawdown());
        values.add(report.getCAGR());
        values.add(report.getExposure());
        values.add(report.getCAGR() / report.getExposure());
        values.add(report.getCAGR() / ((1 + report.getExposure()) * (1 + report.getAvgDrawdown())));
        values.add((float) report.getClosedPositionCount());
    }

    private List<String> resolveHeaders(RuleTradingStrategyBuilder tradingStrategyBuilder){
        final List<String> headers = tradingStrategyBuilder.getIterators().stream()
                .map(ResetableIterator::name)
                .collect(Collectors.toCollection(ArrayList::new));
        headers.addAll(Arrays.asList("AVGMFE", "MAXDD", "AVGDD", "CAGR", "EXPOSURE", "RAR", "RARBADD", "TRADECOUNT"));
        return headers;
    }
}
