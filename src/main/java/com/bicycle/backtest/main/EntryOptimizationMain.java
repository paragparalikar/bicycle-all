package com.bicycle.backtest.main;

import com.bicycle.backtest.Backtest;
import com.bicycle.backtest.feature.writer.DelimitedFileFeatureWriter;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.strategy.trading.builder.RuleTradingStrategyBuilder;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.core.indicator.builder.ClosePriceIndicatorBuilder;
import com.bicycle.core.indicator.builder.EMAIndicatorBuilder;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.LiquidityRule;
import com.bicycle.core.rule.WaitForBarCountRule;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.core.rule.builder.SingletonRuleBuilder;
import com.bicycle.util.Dates;
import com.bicycle.util.IntegerIterator;

import java.util.List;

public class EntryOptimizationMain {

    public static void main(String[] args) throws Exception {
        try(final FeatureWriter featureWriter = new DelimitedFileFeatureWriter("features.tsv", "\t")){
            final TradingStrategyBuilder tradingStrategyBuilder = createTradingStrategyBuilder(featureWriter);
            final Backtest backtest = new Backtest()
                    .setStartDate(Dates.toEpochMillis(2010, 1, 1))
                    .setEndDate(Dates.toEpochMillis(2020, 12, 31))
                    .setPercentagePositionSize(2)
                    .setLimitPositionSizeToAvailableMargin(false)
                    .setTradingStrategyBuilder(tradingStrategyBuilder);
            final ReportCache reportCache = backtest.run();
        }
    }


    private static TradingStrategyBuilder createTradingStrategyBuilder(FeatureWriter featureWriter){
        final IntegerIterator longEMABarCount = new IntegerIterator("long-ema-bar-count", 20, 20, 100, 10);
        final IntegerIterator shortEMABarCount = new IntegerIterator("short-ema-bar-count", 5, 5, 20, 5);
        final ClosePriceIndicatorBuilder closePriceIndicatorBuilder = new ClosePriceIndicatorBuilder();
        final RuleBuilder entryRuleBuilder = new SingletonRuleBuilder(LiquidityRule::new).and(
                new EMAIndicatorBuilder(closePriceIndicatorBuilder, shortEMABarCount).crossedAbove(
                        new EMAIndicatorBuilder(closePriceIndicatorBuilder, longEMABarCount)));
        final RuleBuilder exitRuleBuilder = new SingletonRuleBuilder(cache -> new WaitForBarCountRule(50, cache));
        return new RuleTradingStrategyBuilder(entryRuleBuilder, exitRuleBuilder, OrderType.BUY, List.of());
    }

}
