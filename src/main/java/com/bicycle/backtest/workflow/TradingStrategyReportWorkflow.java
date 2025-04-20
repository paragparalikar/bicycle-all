package com.bicycle.backtest.workflow;

import com.bicycle.backtest.strategy.trading.builder.SingleTradingStrategyBuilder;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.backtest.workflow.stage.TradingStrategyReportStage;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.LiquidityRule;
import com.bicycle.core.rule.StopGainRule;
import com.bicycle.core.rule.StopLossRule;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.core.rule.builder.SingletonRuleBuilder;

public class TradingStrategyReportWorkflow {

    public static void main(String[] args) throws Exception {
        final RuleBuilder entryRuleBuilder = new SingletonRuleBuilder(cache -> new LiquidityRule(cache)
                .and(cache.rsi(cache.close(), 2).crossAbove(cache.constant(55f), cache))
                .and(cache.close().greaterThan(cache.ema(cache.close(), 200)))
                );
        final RuleBuilder exitRuleBuilder = new SingletonRuleBuilder(cache -> new StopGainRule(2f, cache.atr(14))
                .or(new StopLossRule(false, 1.5f, cache.atr(14))));
        final TradingStrategyBuilder tradingStrategyBuilder = new SingleTradingStrategyBuilder(OrderType.BUY, entryRuleBuilder, exitRuleBuilder);
        new TradingStrategyReportStage().execute(tradingStrategyBuilder);
    }

}
