package com.bicycle.backtest.workflow.stage;

import lombok.RequiredArgsConstructor;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.vector.ValueVector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class FeatureSelectionStage {

    public DataFrame execute(int featureCount, Formula formula, DataFrame dataFrame) {
        return execute(featureCount, formula, dataFrame, new RandomForest.Options(100, 2,3,0,50));
    }

    public DataFrame execute(int featureCount, Formula formula, DataFrame dataFrame, RandomForest.Options options) {
        System.out.println("--------------- Initiating feature selection stage ---------------");
        System.out.printf("Selecting %d features\n", featureCount);
        System.out.printf("Using RandomForest.Options : ntrees=%d, mtry=%d, maxDepth=%d, maxNodes=%d, nodeSize=%d\n",
                options.ntrees(), options.mtry(), options.maxDepth(), options.maxNodes(), options.nodeSize());
        final RandomForest model = RandomForest.fit(formula, dataFrame, options);
        final double[] importance = model.importance();
        final Map<Integer, Double> map = new HashMap<>();
        for(int index = 0; index < importance.length; index++) map.put(index, importance[index]);
        final List<Integer> indices = map.entrySet().stream().sorted(Map.Entry.<Integer, Double>comparingByValue().reversed()).map(Map.Entry::getKey).limit(featureCount).toList();
        for(int index : indices) System.out.printf("Selected feature : %8.4f %s\n", importance[index], dataFrame.column(index).name());
        final ValueVector y = formula.y(dataFrame);
        dataFrame = new DataFrame(indices.stream().map(dataFrame::column).toArray(ValueVector[]::new));
        dataFrame.add(y);
        return dataFrame;
    }
}
