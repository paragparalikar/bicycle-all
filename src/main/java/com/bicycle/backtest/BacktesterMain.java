package com.bicycle.backtest;

import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.report.cache.SingletonReportCache;
import com.bicycle.backtest.report.cache.TradingStrategyReportCache;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.backtest.strategy.trading.builder.RuleTradingStrategyBuilder;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.core.rule.builder.WaitForBarCountRuleBuilder;
import com.bicycle.core.rule.builder.sugar.LiquidityRuleBuilder;
import com.bicycle.core.rule.builder.sugar.LongBarCountCloseBreakoutRuleBuilder;
import com.bicycle.util.FloatIterator;
import com.bicycle.util.IntegerIterator;
import com.bicycle.util.ResetableIterator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BacktesterMain {

    public static void main(String[] args) {
        final RuleTradingStrategyBuilder tradingStrategyBuilder = createTradingStrategyBuilder();
        final ReportCache reportCache = Backtest.of(tradingStrategyBuilder).run();
        System.out.println(SingletonReportCache.class.cast(reportCache).getReport());
    }

    private static List<Report> getSortedReports(ReportCache reportCache, List<MockTradingStrategy> tradingStrategies){
        final TradingStrategyReportCache tradingStrategyReportCache = TradingStrategyReportCache.class.cast(reportCache);
        final Map<String, Report> reportMap = tradingStrategyReportCache.findAll().stream()
                .collect(Collectors.toMap(report -> report.getTradingStrategy().toString(), Function.identity()));
        return tradingStrategies.stream().map(Object::toString).map(reportMap::get).toList();
    }

    private static RuleTradingStrategyBuilder createTradingStrategyBuilder(){
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
        return new RuleTradingStrategyBuilder(entryRuleBuilder, exitRuleBuilder,
                OrderType.BUY, iterators);
    }

}
