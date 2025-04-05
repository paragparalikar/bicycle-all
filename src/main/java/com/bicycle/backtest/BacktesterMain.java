package com.bicycle.backtest;

import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.report.cache.SingletonReportCache;
import com.bicycle.backtest.strategy.trading.builder.RuleTradingStrategyBuilder;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.StopLossRule;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.core.rule.builder.SingletonRuleBuilder;

import java.util.List;

public class BacktesterMain {

    public static void main(String[] args) {
        final TradingStrategyBuilder tradingStrategyBuilder = createTradingStrategyBuilder();
        final ReportCache reportCache = Backtest.of(tradingStrategyBuilder).run();
        System.out.println(SingletonReportCache.class.cast(reportCache).getReport());
    }

    private static TradingStrategyBuilder createTradingStrategyBuilder(){
        final RuleBuilder entryRuleBuilder = new SingletonRuleBuilder(cache -> {
            return cache.close().greaterThanOrEquals(cache.prev(cache.high(), 1))
                    .and(cache.prev(cache.close(), 1).lesserThan(cache.prev(cache.high(), 2)));
        });
        final RuleBuilder exitRuleBuilder = new SingletonRuleBuilder(cache -> {
            final Indicator atrIndicator = cache.atr(21);
            return new StopLossRule(false, 2, atrIndicator)
                    .or(new StopLossRule(true, 2, atrIndicator));
        });
        return new RuleTradingStrategyBuilder(entryRuleBuilder, exitRuleBuilder, OrderType.BUY, List.of());
    }

}
