package com.bicycle.backtest.workflow;

import com.bicycle.backtest.workflow.stage.feature.*;
import com.bicycle.backtest.workflow.stage.model.HyperParameterOptimizationStage;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.LiquidityRule;
import com.bicycle.core.rule.WaitForBarCountRule;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.core.rule.builder.SingletonRuleBuilder;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.validation.metric.Precision;

public class EntryFilterWorkflow {

    public static void main(String[] args) throws Exception {
        final String targetVariableName = "PNL";
        final Formula formula = Formula.lhs(targetVariableName);
        final RuleBuilder entryRuleBuilder = createEntryRuleBuilder();
        final RuleBuilder exitRuleBuilder = createExitRuleBuilder();
        DataFrame dataFrame = new FeatureGenerationStage().execute(OrderType.BUY, entryRuleBuilder, exitRuleBuilder);
        dataFrame = new FeatureImputationStage().execute(formula, dataFrame);
        dataFrame = new FeatureClusteringStage().toCentroidsDataFrame(dataFrame, targetVariableName);
        dataFrame = new FeatureDiscretizationStage().execute(dataFrame, targetVariableName);
        dataFrame = new FeatureSelectionStage().execute(9, formula, dataFrame);
        RandomForest.Options options = new HyperParameterOptimizationStage().execute(10, formula, dataFrame, Precision::of);
    }

    private static RuleBuilder createEntryRuleBuilder(){
        return new SingletonRuleBuilder(cache -> new LiquidityRule(cache)
                .and(cache.rsi(cache.close(), 3).crossAbove(cache.constant(10), cache))
        );
    }

    private static RuleBuilder createExitRuleBuilder(){
        return new SingletonRuleBuilder(
                cache -> cache.rsi(cache.close(), 3).crossAbove(cache.constant(70), cache)
        );
    }

}
