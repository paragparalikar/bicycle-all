package com.bicycle.backtest.report;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.util.Strings;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class FullReport extends BaseReport {
    
    public static ReportBuilder builder() {
        return FullReport::new;
    }
    
    private float profitFactor, payoffRatio, expectancy;
    private float maxProfit, avgProfit, maxLoss, avgLoss;
    private int winningTradeCount, losingTradeCount;
    private long minTradeDuration = Long.MAX_VALUE, avgTradeDuration, maxTradeDuration;
    private long minLosingTradeDuration = Long.MAX_VALUE, avgLosingTradeDuration, maxLosingTradeDuration;
    private long minWinningTradeDuration = Long.MAX_VALUE, avgWinningTradeDuration, maxWinningTradeDuration;
    private int avgWinningStreak, maxWinningStreak, winningStreakCount, avgLosingStreak, maxLosingStreak, losingStreakCount;
    
    @Getter(value = AccessLevel.NONE) private int winningStreak, losingStreak;

    public FullReport(float initialMargin, MockTradingStrategy tradingStrategy, long startDate, long endDate) {
        super(initialMargin, tradingStrategy, startDate, endDate);
    }
    
    @Override
    public void close(MockPosition trade) {
        super.close(trade);
        final long duration = trade.getDuration();
        computeTradeDurations(duration);
        final float profitLoss = trade.getClosePercentProfitLoss();
        if(0 < profitLoss) computeWinningMetrices(profitLoss, duration);
        else computeLosingMetrices(profitLoss, duration);
        computeRatios();
    }
    
    private void computeTradeDurations(long duration) {
        minTradeDuration = Math.min(minTradeDuration, duration);
        maxTradeDuration = Math.max(maxTradeDuration, duration);
        final int closedTradeCount = getTotalTradeCount() - getOpenTrades().size();
        avgTradeDuration = (avgTradeDuration * (closedTradeCount - 1) + duration) / closedTradeCount;
    }
    
    private void computeWinningMetrices(float profitLoss, long duration) {
        winningTradeCount++;
        maxProfit = Math.max(maxProfit, profitLoss);
        avgProfit = (avgProfit * (winningTradeCount - 1) + profitLoss) / winningTradeCount;  
        
        minWinningTradeDuration = Math.min(minWinningTradeDuration, duration);
        maxWinningTradeDuration = Math.max(maxWinningTradeDuration, duration);
        avgWinningTradeDuration = (avgWinningTradeDuration * (winningTradeCount - 1) + duration) / winningTradeCount;
        
        winningStreak++;
        avgLosingStreak = (avgLosingStreak * losingStreakCount + losingStreak) / ++losingStreakCount;
        maxLosingStreak = Math.max(maxLosingStreak, losingStreak);
    }
    
    private void computeLosingMetrices(float profitLoss, long duration) {
        losingTradeCount++;
        maxLoss = Math.min(maxLoss, profitLoss);
        avgLoss = (avgLoss * (losingTradeCount - 1) + profitLoss) / losingTradeCount;
        
        minLosingTradeDuration = Math.min(minLosingTradeDuration, duration);
        maxLosingTradeDuration = Math.max(maxLosingTradeDuration, duration);
        avgLosingTradeDuration = (avgLosingTradeDuration * (losingTradeCount - 1) + duration) / losingTradeCount;
        
        losingStreak++;
        avgWinningStreak = (int)((double)(avgWinningStreak * winningStreakCount + winningStreak)) / ++winningStreakCount;
        maxWinningStreak = Math.max(maxWinningStreak, winningStreak);
        winningStreak = 0;
    }
    
    private void computeRatios() {
        payoffRatio = avgProfit / avgLoss;
        final float dinominator = avgLoss * losingTradeCount;
        profitFactor = avgProfit * winningTradeCount / (0 == dinominator ? 1 : dinominator);
        expectancy = (avgProfit * winningTradeCount + avgLoss * losingTradeCount) / getTotalTradeCount();
    }

    @Override
    public String toString() {
        return super.toString() +
                Strings.format("Expectancy", expectancy) +
                Strings.format("Profit Factor", profitFactor) +
                Strings.format("Win Ratio", ((float) winningTradeCount) / ((float)getTotalTradeCount())) +
                Strings.format("Payoff Ratio", payoffRatio);
    }
}
