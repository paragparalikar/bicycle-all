package com.bicycle.backtest.report;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.util.Strings;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;

import java.util.Objects;

@Getter
public class BaseReport implements Report {

    private final long startDate, endDate;
    private int barCount, totalTradeCount;
    private final float initialMargin, years;
    private final MockTradingStrategy tradingStrategy;
    private final Int2ObjectOpenHashMap<MockPosition> openTrades = new Int2ObjectOpenHashMap<>();
    private volatile float availableMargin, equity, maxEquity, minEquity, avgDrawdown, maxDrawdown, exposure;
    private double totalMfe;
    
    public BaseReport(
            float initialMargin, MockTradingStrategy tradingStrategy,
            long startDate, long endDate) {
        this.endDate = endDate;
        this.startDate = startDate;
        this.tradingStrategy = tradingStrategy;
        this.years = (this.endDate - this.startDate) / 31536000000f;
        this.maxEquity = this.initialMargin = this.availableMargin = initialMargin;
    }
    
    @Override
    public void clear() {
        openTrades.clear();
        barCount = totalTradeCount = 0;
        avgDrawdown = maxDrawdown = exposure = 0;
        availableMargin = equity = maxEquity = minEquity = initialMargin;
    }
    
    private float computeOpenEquity() {
        return (float) openTrades.values().stream()
            .filter(Objects::nonNull)
            .mapToDouble(MockPosition::getOpenEquity)
            .sum();
    }
    
    @Override
    public synchronized void compute(long date) {
        barCount++;
        final float openEquity = computeOpenEquity();
        equity = availableMargin + openEquity;
        maxEquity = Math.max(maxEquity, equity);
        minEquity = Math.min(minEquity, equity);
        final float drawdown = maxEquity > equity ? (maxEquity - equity) / maxEquity : 0;
        maxDrawdown = Math.max(maxDrawdown, drawdown);
        avgDrawdown = (avgDrawdown * (barCount - 1) + drawdown) / barCount;
        exposure = (exposure * (barCount - 1) + Math.abs(openEquity / equity)) / barCount;
    }
    
    @Override
    public MockPosition getOpenPosition(Symbol symbol) {
        return openTrades.get(symbol.token());
    }

    @Override
    public synchronized void open(MockPosition trade) {
        totalTradeCount++;
        openTrades.put(trade.getSymbol().token(), trade);
        availableMargin -= trade.getOpenEquity();
    }

    @Override
    public synchronized void close(MockPosition trade) {
        availableMargin += trade.getCloseEquity();
        openTrades.remove(trade.getSymbol().token());
        totalMfe += trade.getMfe();
    }
    
    @Override
    public float getCAGR() {
        double cagr = Math.pow(Math.abs(equity) / initialMargin, 1 / years) - 1;
        cagr = (0 < equity ? cagr : (-1 * cagr -1));
        return (float) cagr;
    }
    
    @Override
    public float getDrawdown() {
        return maxEquity - equity;
    }
    
    @Override
    @SuppressWarnings("unchecked") 
    public <T extends Report> T unwrap(Class<T> type) {
        if(type.isAssignableFrom(getClass())) return (T) this;
        else return null;
    }
    
    public int getOpenPositionCount() {
        return getOpenTrades().size();
    }

    @Override
    public String toString() {
        return "Base backtest report" +
                "\nTrading Strategy    : " + tradingStrategy.toString() +
                "\nInitial Equity      : " + String.format("%8.2f", initialMargin) +
                "\nMaximum Equity      : " + String.format("%8.2f", maxEquity) +
                "\nMinimum Equity      : " + String.format("%8.2f", minEquity) +
                "\nFinal Equity        : " + String.format("%8.2f", equity) +
                "\nDuration            : " + String.format("%4.2f years", years) +
                "\nCAGR                : " + Strings.format(getCAGR()) +
                "\nExposure            : " + Strings.format(exposure) +
                "\nRAR                 : " + Strings.format(getCAGR() / exposure) +
                "\nAvgDD               : " + Strings.format(avgDrawdown) +
                "\nMaxDD               : " + Strings.format(maxDrawdown) +
                "\nTotal MFE           : " + Strings.format(totalMfe) +
                "\nBarCount            : " + Strings.format(barCount) +
                "\nOpen Trades         : " + Strings.format(openTrades.size()) +
                "\nClosed Trades       : " + Strings.format(totalTradeCount - openTrades.size()) +
                "\nTotal Trades        : " + Strings.format(totalTradeCount) + "\n";
    }
    
}
