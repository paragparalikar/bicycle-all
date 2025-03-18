package com.bicycle.backtest.strategy.trading.evaluator.performance;

import java.util.EnumMap;
import java.util.Map;

import static com.bicycle.backtest.strategy.trading.evaluator.performance.PerformanceEvaluatorType.*;

public class PerformanceEvaluatorFactory {

    private static final class PerformanceEvaluatorFactoryHolder {
        private static final PerformanceEvaluatorFactory INSTANCE = new PerformanceEvaluatorFactory();
    }

    public static PerformanceEvaluatorFactory getInstance(){
        return PerformanceEvaluatorFactoryHolder.INSTANCE;
    }

    private final Map<PerformanceEvaluatorType, PerformanceEvaluator> cache =
            new EnumMap<>(PerformanceEvaluatorType.class);

    private PerformanceEvaluatorFactory(){
        cache.put(AvgDD, new AvgDDPerformanceEvaluator());
        cache.put(CAGR, new CAGRPerformanceEvaluator());
        cache.put(MaxDD, new MaxDDPerformancEvaluator());
        cache.put(RaR, new RaRPerformanceEvaluator());
        cache.put(RaRByAvgDD, new RarByAvgDDPerformanceEvaluator());
        cache.put(RaRByMaxDD, new RaRByMaxDDPerformanceEvaluator());
    }
    
    public PerformanceEvaluator get(PerformanceEvaluatorType type) {
        return cache.get(type);
    }

}
