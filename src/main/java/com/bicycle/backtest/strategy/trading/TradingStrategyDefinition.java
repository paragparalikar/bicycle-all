package com.bicycle.backtest.strategy.trading;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

import java.util.*;

@Data
@Builder
@RequiredArgsConstructor
public class TradingStrategyDefinition implements Cloneable {

    private final Exchange exchange;
    @Singular private final Set<Symbol> symbols = new HashSet<>();
    @Singular private final Set<Timeframe> timeframes = new HashSet<>();
    @Singular private final List<MockTradingStrategy> tradingStrategies = new ArrayList<>();
    
    @Override
    protected TradingStrategyDefinition clone() {
        final TradingStrategyDefinition clone = new TradingStrategyDefinition(exchange);
        clone.getSymbols().addAll(symbols);
        clone.getTimeframes().addAll(timeframes);
        clone.getTradingStrategies().addAll(tradingStrategies);
        return clone;
    }
    
}
