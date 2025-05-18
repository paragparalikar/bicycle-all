package com.bicycle.backtest.workflow.stage.strategy;

import com.bicycle.backtest.Backtest;
import com.bicycle.backtest.report.FullReport;
import com.bicycle.backtest.report.cache.SingletonReportCache;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.util.Dates;

public class TradingStrategyReportStage {

    public void execute(TradingStrategyBuilder tradingStrategyBuilder) throws Exception {
        final long startDate = Dates.toEpochMillis(2010, 1, 1);
        final long endDate = Dates.toEpochMillis(2020, 12, 31);
        final float slippaggePercentage = 0.1f;
        final float percentPositionSize = 2;
        final boolean limitPositionSizeToAvailableMargin = false;
        execute(tradingStrategyBuilder, startDate, endDate, slippaggePercentage, percentPositionSize, limitPositionSizeToAvailableMargin);
    }

    public void execute(TradingStrategyBuilder tradingStrategyBuilder,
                        long startDate, long endDate, float slippagePercentage, float percentagePositionSize,
                        boolean limitPositionSizeToAvailableMargin) throws Exception{
        final SingletonReportCache reportCache = (SingletonReportCache) new Backtest()
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setReportBuilder(FullReport::new)
                .setPercentagePositionSize(percentagePositionSize)
                .setLimitPositionSizeToAvailableMargin(limitPositionSizeToAvailableMargin)
                .setTradingStrategyBuilder(tradingStrategyBuilder)
                .run();
        System.out.println(reportCache.getReport());
    }


}
