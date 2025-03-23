package com.bicycle.backtest;

import com.bicycle.backtest.report.BaseReport;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.report.cache.SingletonReportCache;
import com.bicycle.backtest.strategy.positionSizing.PositionSizingStrategy;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.rule.WaitForBarCountRule;
import com.bicycle.util.Dates;

import java.util.List;

public class BacktesterMain {

    public static void main(String[] args) {
        final Backtester backtester = new Backtester();
        final long startDate = Dates.toEpochMillis(2014, 1, 1);
        final long endDate = Dates.toEpochMillis(2023, 12, 31);
        final TradingStrategyBuilder tradingStrategyBuilder = createTradingStrategyBuilder();

        final ReportCache reportCache = Backtester.builder()
                .reportBuilderCustomizer(BaseReport::builder)
                .reportCacheCustomizer(SingletonReportCache::new)
                .build()
                .run(tradingStrategyBuilder, startDate, endDate);

    }

    private static TradingStrategyBuilder createTradingStrategyBuilder(){
        return new TradingStrategyBuilder() {
            @Override
            public List<MockTradingStrategy> build(float slippagePercentage, IndicatorCache indicatorCache, ReportCache reportCache, PositionSizingStrategy positionSizingStrategy) {
                return List.of(buildDefault(slippagePercentage, indicatorCache, reportCache, positionSizingStrategy));
            }

            @Override
            public MockTradingStrategy buildDefault(float slippagePercentage, IndicatorCache cache, ReportCache reportCache, PositionSizingStrategy positionSizingStrategy) {
                final OrderType entryOrderType = OrderType.BUY;
                final Rule liquidityRule = cache.close().multipliedBy(cache.volume()).greaterThan(10000000)
                        .and(cache.close().greaterThan(10.0f));
                final Rule entryRule = liquidityRule.and(cache.close().greaterThanOrEquals(cache.highest(cache.high(), 5)));
                final Rule exitRule = new WaitForBarCountRule(15, cache);
                return MockTradingStrategy.builder()
                        .slippagePercentage(slippagePercentage)
                        .entryRule(entryRule)
                        .exitRule(exitRule)
                        .entryOrderType(entryOrderType)
                        .reportCache(reportCache)
                        .atrIndicator(cache.atr(10))
                        .positionSizingStrategy(positionSizingStrategy)
                        .build();
            }
        };
    }
}
