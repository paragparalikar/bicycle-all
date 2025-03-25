package com.bicycle.backtest;

import com.bicycle.backtest.report.BaseReport;
import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.report.cache.SingletonReportCache;
import com.bicycle.backtest.strategy.trading.builder.RuleTradingStrategyBuilder;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.core.rule.builder.WaitForBarCountRuleBuilder;
import com.bicycle.core.rule.builder.sugar.LiquidityRuleBuilder;
import com.bicycle.core.rule.builder.sugar.LongBarCountCloseBreakoutRuleBuilder;
import com.bicycle.util.Dates;
import com.bicycle.util.FloatIterator;
import com.bicycle.util.IntegerIterator;
import com.bicycle.util.ResetableIterator;

import java.util.Arrays;
import java.util.List;

public class BacktesterMain {

    public static void main(String[] args) {
        final Backtester backtester = new Backtester();
        final long startDate = Dates.toEpochMillis(2014, 1, 1);
        final long endDate = Dates.toEpochMillis(2023, 12, 31);
        final IntegerIterator minVolumeIterator = new IntegerIterator("minVolume", 100000, 100000, 100000,  1);
        final FloatIterator minPriceIterator = new FloatIterator("minPrice", 5, 5, 5, 1);
        final IntegerIterator smaBarCountIterator = new IntegerIterator("smaBarCount", 10, 10, 10, 1);
        final IntegerIterator barCountIterator = new IntegerIterator("barCount", 5, 2, 15, 1);
        final IntegerIterator waitForBarCountIterator = new IntegerIterator("waitForBarCount", 15, 15, 15, 1);
        final List<ResetableIterator> iterators = Arrays.asList(minVolumeIterator, minPriceIterator, smaBarCountIterator, barCountIterator, waitForBarCountIterator);
        final RuleBuilder entryRuleBuilder = LiquidityRuleBuilder.builder()
                .minVolumeIterator(minVolumeIterator)
                .minPriceIterator(minPriceIterator)
                .smaBarCountIterator(smaBarCountIterator)
                .build().and(LongBarCountCloseBreakoutRuleBuilder.builder()
                        .barCountIterator(barCountIterator)
                        .build());
        final RuleBuilder exitRuleBuilder = WaitForBarCountRuleBuilder.builder()
                .integerIterator(waitForBarCountIterator)
                .build();
        final TradingStrategyBuilder tradingStrategyBuilder = new RuleTradingStrategyBuilder(entryRuleBuilder, exitRuleBuilder,
                OrderType.BUY, iterators);
        final ReportCache reportCache = Backtester.builder()
                .reportBuilderCustomizer(BaseReport::builder)
                .reportCacheCustomizer(SingletonReportCache::new)
                .build()
                .run(tradingStrategyBuilder, startDate, endDate);
        final SingletonReportCache singletonReportCache = SingletonReportCache.class.cast(reportCache);
        final Report report = singletonReportCache.getReport();
        final BaseReport baseReport = report.unwrap(BaseReport.class);
        System.out.println(baseReport);
    }
}
