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
        onTick(bar.open(), bar.date(), bar.volume(), bar.symbol(), bar.timeframe(), report, true);
        if(bar.close() > bar.open()){
            onTick(bar.low(), bar.date(), bar.volume(), bar.symbol(), bar.timeframe(), report, false);
            onTick(bar.high(), bar.date(), bar.volume(), bar.symbol(), bar.timeframe(), report, false);
        } else {
            onTick(bar.high(), bar.date(), bar.volume(), bar.symbol(), bar.timeframe(), report, false);
            onTick(bar.low(), bar.date(), bar.volume(), bar.symbol(), bar.timeframe(), report, false);
        }
        onTick(bar.close(), bar.date(), bar.volume(), bar.symbol(), bar.timeframe(), report, false);
    }

    @Override
    public void onTick(Tick tick) {
        Report report = reportCache.get(tick.symbol(), this);
        onTick(tick.ltp(), tick.date(), tick.volume(), tick.symbol(), null, report, true);
    }

    public void onTick(float price, long date, int volume, Symbol symbol, Timeframe timeframe, boolean forceLtp){
        Report report = reportCache.get(symbol, this);
        onTick(price, date, volume, symbol, timeframe, report, forceLtp);
    }

    private void onTick(float price, long date, int volume, Symbol symbol, Timeframe timeframe, Report report, boolean forceLtp) {
        MockPosition openPosition = report.getOpenPosition(symbol);
        if(null != openPosition ){
            openPosition.onPrice(price);
            if(tryExit(date, openPosition, forceLtp)) {
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
    
    public boolean tryExit(long date, MockPosition position, boolean forceLtp) {
        if(exitRule.isSatisfied(position.getSymbol(), position.getTimeframe(), position)) {
            if(forceLtp || 0 == position.getExitPrice()) position.setExitPrice(position.getLtp());
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
