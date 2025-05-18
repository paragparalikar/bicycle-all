package com.bicycle.backtest.workflow.stage.model;

import com.bicycle.backtest.feature.writer.DelimitedFileFeatureWriter;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.util.Index;
import smile.validation.Bag;
import smile.validation.ClassificationMetrics;
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
                    for(int nodeSize = 20; nodeSize <= 40; nodeSize += 10){
                        for(int ntrees = 500; ntrees <= 1000; ntrees += 100){
                            final RandomForest.Options options = new RandomForest.Options(ntrees, mtry, maxDepth, 0, nodeSize);
                            final double score = compute(folds, formula, dataFrame, options, evaluator, false);
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
        compute(folds, formula, dataFrame, maxOptions, evaluator, true);
        return maxOptions;
    }

    private double compute(int k, Formula formula, DataFrame dataFrame, RandomForest.Options options, BiFunction<int[], int[], Double> evaluator, boolean printInfo){
        double sum = 0;
        final Bag[] bags = CrossValidation.of(dataFrame.size(), k);
        if(printInfo) System.out.printf("%15s%15s%15s%15s%15s%15s%15s%15s%15s%15s%15s%15s%15s\n", "train", "test", "score", "error", "accuracy", "sensitivity",
                "specificity", "precision", "f1", "mcc", "auc", "logLoss", "crossEntropy");
        for(int bagIndex = 0; bagIndex < k; bagIndex++){
            final Bag bag = bags[bagIndex];
            final DataFrame train = dataFrame.get(Index.of(bag.samples()));
            final RandomForest model = RandomForest.fit(formula, train, options);

            final DataFrame test = dataFrame.get(Index.of(bag.oob()));
            final double trainResult = evaluate(formula, train, model, evaluator);
            final double testResult = evaluate(formula, test, model, evaluator);
            final double score = ((trainResult + testResult) / Math.pow(1 + Math.abs(trainResult - testResult), 2));
            sum += score;
            if(printInfo) printInfo(model, trainResult, testResult, score);
        }
        return sum / k;
    }

    private void printInfo(RandomForest model, double trainResult, double testResult, double score){
        final ClassificationMetrics metrics = model.metrics();
        System.out.printf("%15.2f%15.2f%15.2f%15d%15.2f%15.2f%15.2f%15.2f%15.2f%15.2f%15.2f%15.2f%15.2f\n",
                trainResult, testResult, score, metrics.error(), metrics.accuracy(),
                metrics.sensitivity(), metrics.specificity(), metrics.precision(), metrics.f1(), metrics.mcc(), metrics.auc(),
                metrics.logloss(), metrics.crossEntropy());
    }

    private double evaluate(Formula formula, DataFrame dataFrame, RandomForest model, BiFunction<int[], int[], Double> evaluator){
        final int n = dataFrame.size();
        final int[] prediction = new int[n];
        final int[] truth = formula.y(dataFrame).toIntArray();
        for (int i = 0; i < n; i++) prediction[i] = model.predict(dataFrame.get(i));
        return evaluator.apply(truth, prediction);
    }


}
