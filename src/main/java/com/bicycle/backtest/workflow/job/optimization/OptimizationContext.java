package com.bicycle.backtest.workflow.job.optimization;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.Backtest;
import com.bicycle.backtest.strategy.trading.evaluator.performance.PerformanceEvaluator;
import com.bicycle.backtest.strategy.trading.evaluator.robustness.RobustnessEvaluator;
import com.bicycle.backtest.executor.BacktestExecutor;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.symbol.Exchange;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OptimizationContext {
    
    private List<MockPosition> positions;
    
    private final float initialMargin;
    private final Exchange exchange;
    private final IndicatorCache indicatorCache;
    private final int maxSymbolSelectionCount;
    private final Backtest definition;
    private final RobustnessEvaluator robustnessEvaluator;
    private final PerformanceEvaluator performanceEvaluator;
    private final BacktestExecutor tradingStrategyExecutor;
    
    public void clear() {
        indicatorCache.clear();
        positions = null;
    }
    
}
