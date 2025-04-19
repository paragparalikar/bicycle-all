package com.bicycle.backtest.workflow.stage;

import com.bicycle.backtest.feature.writer.DelimitedFileFeatureWriter;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.util.Index;
import smile.validation.Bag;
import smile.validation.CrossValidation;

import java.util.List;
import java.util.function.BiFunction;

public class HyperParameterOptimizationStage {

    public RandomForest.Options execute(int folds, Formula formula, DataFrame dataFrame, BiFunction<int[], int[], Double> evaluator) throws Exception {
        System.out.println("\n--------------- Initiating hyper-parameter optimization stage ---------------");
        double maxScore = Double.MIN_VALUE;
        RandomForest.Options maxOptions = null;

        try(FeatureWriter featureWriter = new DelimitedFileFeatureWriter("entry-hyper-parameters.tsv","\t")){
            final List<String> headers = List.of("ntrees","mtry","maxDepth","nodeSize","score");
            featureWriter.writeHeaders(headers);
            System.out.printf("%10s%10s%10s%10s%10s\n", "ntrees","mtry","maxDepth","nodeSize","score");

            for(int mtry = 1; mtry <= 3; mtry++){
                for(int maxDepth = 2; maxDepth <= 4; maxDepth += 2){
                    for(int nodeSize = 10; nodeSize <= 50; nodeSize += 10){
                        for(int ntrees = 50; ntrees <= 1000; ntrees += 50){
                            final RandomForest.Options options = new RandomForest.Options(ntrees, mtry, maxDepth, 0, nodeSize);
                            final double score = compute(folds, formula, dataFrame, options, evaluator);
                            System.out.printf("%10d%10d%10d%10d%10.4f\n", ntrees, mtry, maxDepth, nodeSize, score);
                            featureWriter.writeValues(List.of((float)ntrees, (float)mtry, (float)maxDepth, (float)nodeSize, (float)score));
                            if(score > maxScore){
                                maxOptions = options;
                                maxScore = score;
                            }
                        }
                    }
                }
            }
        }
        System.out.printf("Max score : %f\n", maxScore);
        System.out.printf("RandomForest.Options : ntrees=%d, mtry=%d, maxDepth=%d, nodeSize=%d\n",
                maxOptions.ntrees(), maxOptions.mtry(), maxOptions.maxDepth(), maxOptions.nodeSize());
        return maxOptions;
    }

    private double compute(int k, Formula formula, DataFrame dataFrame, RandomForest.Options options, BiFunction<int[], int[], Double> evaluator){
        double sum = 0;
        final Bag[] bags = CrossValidation.of(dataFrame.size(), k);
        for(int bagIndex = 0; bagIndex < k; bagIndex++){
            final Bag bag = bags[bagIndex];
            final DataFrame train = dataFrame.get(Index.of(bag.samples()));
            final RandomForest model = RandomForest.fit(formula, train, options);
            final DataFrame test = dataFrame.get(Index.of(bag.oob()));
            final double trainResult = evaluate(formula, train, model, evaluator);
            final double testResult = evaluate(formula, test, model, evaluator);
            sum += ((trainResult + testResult) / Math.pow(1 + Math.abs(trainResult - testResult), 2));
        }
        return sum / k;
    }

    private double evaluate(Formula formula, DataFrame dataFrame, RandomForest model, BiFunction<int[], int[], Double> evaluator){
        final int n = dataFrame.size();
        final int[] prediction = new int[n];
        final int[] truth = formula.y(dataFrame).toIntArray();
        for (int i = 0; i < n; i++) prediction[i] = model.predict(dataFrame.get(i));
        return evaluator.apply(truth, prediction);
    }


}
