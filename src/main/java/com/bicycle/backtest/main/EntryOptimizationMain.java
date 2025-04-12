package com.bicycle.backtest.main;

import com.bicycle.backtest.Backtest;
import com.bicycle.backtest.feature.writer.DelimitedFileFeatureWriter;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.backtest.report.BaseReport;
import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.report.cache.TradingStrategyReportCache;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.backtest.strategy.trading.builder.RuleTradingStrategyBuilder;
import com.bicycle.core.indicator.builder.ClosePriceIndicatorBuilder;
import com.bicycle.core.indicator.builder.EMAIndicatorBuilder;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.LiquidityRule;
import com.bicycle.core.rule.WaitForBarCountRule;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.core.rule.builder.SingletonRuleBuilder;
import com.bicycle.util.Dates;
import com.bicycle.util.IntegerIterator;
import com.bicycle.util.ResetableIterator;

import java.util.ArrayList;
import java.util.List;

public class EntryOptimizationMain {

    public static void main(String[] args) throws Exception {
        final List<String> headers = new ArrayList<>();
        final RuleTradingStrategyBuilder tradingStrategyBuilder = createTradingStrategyBuilder();
        tradingStrategyBuilder.getIterators().stream().map(ResetableIterator::name).forEach(headers::add);
        headers.add("AVERAGE_MFE");
        try(final FeatureWriter featureWriter = new DelimitedFileFeatureWriter("features1.tsv", "\t")){
            featureWriter.writeHeaders(headers);
            final Backtest backtest = new Backtest()
                    .setStartDate(Dates.toEpochMillis(2010, 1, 1))
                    .setEndDate(Dates.toEpochMillis(2010, 12, 31))
                    .setPercentagePositionSize(2)
                    .setReportCacheOptions(ReportCache.TRADING_STRATEGY)
                    .setLimitPositionSizeToAvailableMargin(false)
                    .setTradingStrategyBuilder(tradingStrategyBuilder);
            final TradingStrategyReportCache reportCache = (TradingStrategyReportCache) backtest.run();
            final List<MockTradingStrategy> tradingStrategies = backtest.getTradingStrategies();
            final List<List<Float>> parameters = ResetableIterator.toList(tradingStrategyBuilder.getIterators());
            for(int index = 0; index < tradingStrategies.size(); index++){
                final List<Float> features = parameters.get(index);
                final Report report = reportCache.get(null, tradingStrategies.get(index));
                features.add(report.unwrap(BaseReport.class).getAverageMfe());
                featureWriter.writeValues(features);
            }
        }
    }


    private static RuleTradingStrategyBuilder createTradingStrategyBuilder(){
        final IntegerIterator longEMABarCount = new IntegerIterator("long-ema-bar-count", 20, 20, 100, 10);
        final IntegerIterator shortEMABarCount = new IntegerIterator("short-ema-bar-count", 5, 5, 20, 5);
        final ClosePriceIndicatorBuilder closePriceIndicatorBuilder = new ClosePriceIndicatorBuilder();
        final RuleBuilder entryRuleBuilder = new SingletonRuleBuilder(LiquidityRule::new).and(
                new EMAIndicatorBuilder(closePriceIndicatorBuilder, shortEMABarCount).crossedAbove(
                        new EMAIndicatorBuilder(closePriceIndicatorBuilder, longEMABarCount)));
        final RuleBuilder exitRuleBuilder = new SingletonRuleBuilder(cache -> new WaitForBarCountRule(50, cache));
        return new RuleTradingStrategyBuilder(entryRuleBuilder, exitRuleBuilder, OrderType.BUY, List.of(longEMABarCount, shortEMABarCount));
    }

}
