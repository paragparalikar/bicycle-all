package com.bicycle.backtest.workflow;

import com.bicycle.backtest.workflow.stage.*;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.LiquidityRule;
import com.bicycle.core.rule.StopGainRule;
import com.bicycle.core.rule.StopLossRule;
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
        dataFrame = new FeatureDiscretizationStage().execute(dataFrame, targetVariableName);
        dataFrame = new FeatureSelectionStage().execute(36, formula, dataFrame);
        RandomForest.Options options = new HyperParameterOptimizationStage().execute(10, formula, dataFrame, Precision::of);
    }

    private static RuleBuilder createEntryRuleBuilder(){
        return new SingletonRuleBuilder(cache -> new LiquidityRule(cache)
                .and(cache.rsi(cache.close(), 2).crossAbove(cache.constant(10), cache))
        );
    }

    private static RuleBuilder createExitRuleBuilder(){
        return new SingletonRuleBuilder(cache -> new WaitForBarCountRule(10, cache)
                //.or(new StopLossRule(false, 1.5f, cache.atr(14)))
                //.or(new StopGainRule(2, cache.atr(14)))
        );
    }

}
