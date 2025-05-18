package com.bicycle.backtest.workflow;

import com.bicycle.backtest.strategy.trading.builder.SingleTradingStrategyBuilder;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.backtest.workflow.stage.strategy.TradingStrategyReportStage;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.LiquidityRule;
import com.bicycle.core.rule.StopLossRule;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.core.rule.builder.SingletonRuleBuilder;

public class TradingStrategyReportWorkflow {

    public static void main(String[] args) throws Exception {
        final RuleBuilder entryRuleBuilder = new SingletonRuleBuilder(cache -> new LiquidityRule(cache)
                .and(cache.close().greaterThan(cache.prev(cache.close(), 1)))
                .and(cache.prev(cache.close(), 1).lesserThan(cache.prev(cache.close(), 2)))
                .and(cache.ema(cache.close(), 21).greaterThan(cache.ema(cache.close(), 55)))
                );
        final RuleBuilder exitRuleBuilder = new SingletonRuleBuilder(cache ->
                new StopLossRule(true, 2f, cache.atr(14))
                );
        final TradingStrategyBuilder tradingStrategyBuilder = new SingleTradingStrategyBuilder(OrderType.BUY, entryRuleBuilder, exitRuleBuilder);
        new TradingStrategyReportStage().execute(tradingStrategyBuilder);
    }

}
