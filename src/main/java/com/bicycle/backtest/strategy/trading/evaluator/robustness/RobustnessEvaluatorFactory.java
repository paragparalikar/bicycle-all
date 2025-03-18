package com.bicycle.backtest.strategy.trading.evaluator.robustness;

import java.util.EnumMap;
import java.util.Map;

import static com.bicycle.backtest.strategy.trading.evaluator.robustness.RobustnessEvaluatorType.*;

public class RobustnessEvaluatorFactory {

    private static class RobustnessEvaluatorFactoryHolder {
        private static final RobustnessEvaluatorFactory INSTANCE = new RobustnessEvaluatorFactory();
    }

    public static RobustnessEvaluatorFactory getInstance(){
        return RobustnessEvaluatorFactoryHolder.INSTANCE;
    }

    private final Map<RobustnessEvaluatorType, RobustnessEvaluator> cache =
            new EnumMap<>(RobustnessEvaluatorType.class);

    private RobustnessEvaluatorFactory(){
        cache.put(AVERAGE, new AverageRobustnessEvaluator());
        cache.put(AVERAGE_BY_DEVIATION, new AverageByDeviationRobustnessEvaluator());
        cache.put(DEVIATION, new DeviationRobustnessEvaluator());
        cache.put(MAXIMUM, new MaximumRobustnessEvaluator());
    }


    public RobustnessEvaluator get(RobustnessEvaluatorType type) {
        return cache.get(type);
    }

}
