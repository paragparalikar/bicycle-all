package com.bicycle.backtest.strategy.trading;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.strategy.positionSizing.PositionSizingStrategy;
import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.BarListener;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.tick.Tick;
import com.bicycle.core.tick.TickListener;
import lombok.Builder;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

public class MockTradingStrategy implements BarListener, TickListener {
    private static final AtomicInteger ID = new AtomicInteger(0);

    private String text;
    @Getter private final int id;
    private final float slippagePercentage;
    private final ReportCache reportCache;
    private Rule entryRule, exitRule;
    @Getter private final OrderType entryOrderType;
    private final PositionSizingStrategy positionSizingStrategy;

    @Builder
    public MockTradingStrategy(float slippagePercentage,
            Rule entryRule, Rule exitRule, OrderType entryOrderType,
            ReportCache reportCache,
            PositionSizingStrategy positionSizingStrategy) {
        this.entryRule = entryRule;
        this.exitRule = exitRule;
        this.entryOrderType = entryOrderType;
        this.reportCache = reportCache;
        this.id = ID.getAndIncrement();
        this.slippagePercentage = slippagePercentage;
        this.positionSizingStrategy = positionSizingStrategy;
    }
    
    @Override
    public void onBar(Bar bar) {
        Report report = reportCache.get(bar.symbol(), this);
        MockPosition openPosition = report.getOpenPosition(bar.symbol());
        onTick(bar.open(), bar.date(), bar.volume(), bar.symbol(), bar.timeframe(), openPosition, report);
        if(bar.close() > bar.open()){
            onTick(bar.low(), bar.date(), bar.volume(), bar.symbol(), bar.timeframe(), openPosition, report);
            onTick(bar.high(), bar.date(), bar.volume(), bar.symbol(), bar.timeframe(), openPosition, report);
        } else {
            onTick(bar.high(), bar.date(), bar.volume(), bar.symbol(), bar.timeframe(), openPosition, report);
            onTick(bar.low(), bar.date(), bar.volume(), bar.symbol(), bar.timeframe(), openPosition, report);
        }
        onTick(bar.close(), bar.date(), bar.volume(), bar.symbol(), bar.timeframe(), openPosition, report);
    }

    @Override
    public void onTick(Tick tick) {
        Report report = reportCache.get(tick.symbol(), this);
        MockPosition openPosition = report.getOpenPosition(tick.symbol());
        onTick(tick.ltp(), tick.date(), tick.volume(), tick.symbol(), null, openPosition, report);
    }

    public void onTick(float price, long date, int volume, Symbol symbol, Timeframe timeframe){
        Report report = reportCache.get(symbol, this);
        MockPosition openPosition = report.getOpenPosition(symbol);
        onTick(price, date, volume, symbol, timeframe, openPosition, report);
    }

    private void onTick(float price, long date, int volume, Symbol symbol, Timeframe timeframe,
                       MockPosition openPosition, Report report) {
        if(null != openPosition ){
            openPosition.onPrice(price);
            if(tryExit(date, openPosition)) {
                report.close(openPosition);
                openPosition = null;
            }
        }
        if(null == openPosition && null != (openPosition = tryEnter(price, date, symbol, timeframe))) {
            report.open(openPosition);
        }
    }

    public MockPosition tryEnter(float price,long date, Symbol symbol, Timeframe timeframe) {
        if(entryRule.isSatisfied(symbol, timeframe, null)) {
            final MockPosition position = new MockPosition(symbol, timeframe, entryOrderType);
            final float entryPrice = price * (100 + entryOrderType.multiplier() * slippagePercentage) / 100;
            final Report report = reportCache.get(symbol, this);
            final int entryQuantity = positionSizingStrategy.size(entryPrice, report.getInitialMargin(), report.getAvailableMargin(), position);
            if(0 < entryQuantity) {
                position.enter(date, entryQuantity, entryPrice);
                return position;
            }
        }
        return null;
    }
    
    public boolean tryExit(long date, MockPosition position) {
        if(exitRule.isSatisfied(position.getSymbol(), position.getTimeframe(), position)) {
            if(0 == position.getExitPrice()) position.setExitPrice(position.getLtp());
            final float exitPrice = position.getExitPrice() * (100 + entryOrderType.complement().multiplier() * slippagePercentage) / 100;
            position.exit(date, position.getEntryQuantity(), exitPrice);
            return true;
        }
        return false;
    }
    
    public void addEntryRule(Rule rule) {
        entryRule = entryRule.and(rule);
    }
    
    public void addExitRule(Rule rule) {
        exitRule = exitRule.or(rule);
    }
    
    @Override
    public String toString() {
        return null == text ? text = entryOrderType + " " + entryRule + " : " +
                entryOrderType.complement() + " " + exitRule : text;
    }
}
