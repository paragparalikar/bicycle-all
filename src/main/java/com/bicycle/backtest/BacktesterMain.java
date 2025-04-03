package com.bicycle.backtest;

import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.report.cache.SingletonReportCache;
import com.bicycle.backtest.strategy.trading.builder.RuleTradingStrategyBuilder;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.core.rule.builder.SingletonRuleBuilder;

public class BacktesterMain {

    public static void main(String[] args) {
        final RuleTradingStrategyBuilder tradingStrategyBuilder = createTradingStrategyBuilder();
        final ReportCache reportCache = Backtest.of(tradingStrategyBuilder).run();
        System.out.println(SingletonReportCache.class.cast(reportCache).getReport());
    }

    private static RuleTradingStrategyBuilder createTradingStrategyBuilder(){
        final RuleBuilder entryRuleBuilder = new SingletonRuleBuilder(cache -> {
            return cache.close().greaterThanOrEquals(cache.prev(cache.high(), 1))
                    .and(cache.prev(cache.close(), 1).lesserThan(cache.prev(cache.high(), 2)));
        });
        final RuleBuilder exitRuleBuilder = new SingletonRuleBuilder(cache -> {
            return null;
        });
        return null;
    }

}
