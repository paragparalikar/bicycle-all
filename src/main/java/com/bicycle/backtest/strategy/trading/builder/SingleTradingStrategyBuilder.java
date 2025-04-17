package com.bicycle.backtest.strategy.trading.builder;

import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.strategy.positionSizing.PositionSizingStrategy;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.builder.RuleBuilder;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class SingleTradingStrategyBuilder implements TradingStrategyBuilder {

    private final OrderType entryOrderType;
    private MockTradingStrategy tradingStrategy;
    private final RuleBuilder entryRuleBuilder, exitRuleBuilder;

    @Override
    public List<MockTradingStrategy> build(float slippagePercentage, 
            IndicatorCache cache, ReportCache reportCache, PositionSizingStrategy positionSizingStrategy) {
        return Collections.singletonList(buildDefault(slippagePercentage, cache, reportCache, positionSizingStrategy));
    }

    @Override
    public MockTradingStrategy buildDefault(float slippagePercentage, IndicatorCache cache, 
            ReportCache reportCache, PositionSizingStrategy positionSizingStrategy) {
        return null == tradingStrategy ? tradingStrategy = new MockTradingStrategy(slippagePercentage,
                        entryRuleBuilder.buildDefault(cache),
                        exitRuleBuilder.buildDefault(cache),
                        entryOrderType, reportCache, cache.atr(14), positionSizingStrategy)
                : tradingStrategy;
    }

    @Override
    public String toString() {
        return String.valueOf(tradingStrategy);
    }
}
