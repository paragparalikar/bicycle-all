package com.bicycle.backtest.workflow;

import com.bicycle.backtest.strategy.trading.builder.RuleTradingStrategyBuilder;
import com.bicycle.backtest.workflow.stage.strategy.TradingStrategyOptimizationStage;
import com.bicycle.core.indicator.builder.ClosePriceIndicatorBuilder;
import com.bicycle.core.indicator.builder.ConstantIndicatorBuilder;
import com.bicycle.core.indicator.builder.EMAIndicatorBuilder;
import com.bicycle.core.indicator.builder.RSIIndicatorBuilder;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.LiquidityRule;
import com.bicycle.core.rule.builder.*;
import com.bicycle.util.FloatIterator;
import com.bicycle.util.IntegerIterator;

import java.util.List;

public class TradingStrategyOptimizationWorkflow {

    public static void main(String[] args) throws Exception {
        final IntegerIterator rsiBarCountIterator = new IntegerIterator("RSI-BAR-COUNT", 2, 2, 3, 1);
        final FloatIterator rsiLevelIterator = new FloatIterator("RSI-LEVEL", 40, 30, 70, 5);
        final IntegerIterator emaBarCountIterator = new IntegerIterator("EMA-BAR-COUNT", 200, 200, 200, 200);
        final FloatIterator trailingStopIterator = new FloatIterator("STOP-LOSS", 1.6f, 1, 3, 0.5f);
        final FloatIterator trailingGainIterator = new FloatIterator("STOP-GAIN", 2f, 1, 3, 0.5f);
        final IntegerIterator atrBarCountIterator = new IntegerIterator("ATR-BAR-COUNT", 14, 14, 14, 14);

        final ClosePriceIndicatorBuilder closePriceIndicatorBuilder = new ClosePriceIndicatorBuilder();
        final RuleBuilder entryRuleBuilder = new SingletonRuleBuilder(LiquidityRule::new)
                .and(new CrossAboveRuleBuilder(new RSIIndicatorBuilder(closePriceIndicatorBuilder, rsiBarCountIterator), new ConstantIndicatorBuilder(rsiLevelIterator)))
                .and(closePriceIndicatorBuilder.greaterThan(new EMAIndicatorBuilder(closePriceIndicatorBuilder, emaBarCountIterator)));
        final RuleBuilder exitRuleBuilder = new StopLossRuleBuilder(false, trailingStopIterator, atrBarCountIterator)
                .or(new StopGainRuleBuilder(trailingGainIterator, atrBarCountIterator));
        final RuleTradingStrategyBuilder tradingStrategyBuilder = new RuleTradingStrategyBuilder(entryRuleBuilder, exitRuleBuilder, OrderType.BUY,
                List.of(rsiBarCountIterator, rsiLevelIterator, emaBarCountIterator, trailingStopIterator, trailingGainIterator));

        new TradingStrategyOptimizationStage().execute(tradingStrategyBuilder);
    }

}
