package com.bicycle.backtest.main;

import com.bicycle.backtest.feature.writer.DelimitedFileFeatureWriter;
import com.bicycle.backtest.feature.writer.FeatureWriter;
import com.bicycle.util.Constant;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.io.Read;
import smile.util.Index;
import smile.validation.Bag;
import smile.validation.CrossValidation;
import smile.validation.metric.FScore;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class C_EntryFilterOptimizationMain {

    public static void main(String[] args) throws Exception {
        final String targetColumn = "MFE";
        final Formula formula = Formula.lhs(targetColumn);
        final Path path = Paths.get(Constant.HOME, "reports", "entry-features.tab");
        DataFrame dataFrame = Read.csv(path.toString(),"delimiter=\\t,header=true,comment=#,escape=\\,quote=\"").dropna();
        //dataFrame = RobustStandardizer.fit(dataFrame).apply(dataFrame);
        //final int[] y = discretizeByMedian(dataFrame.column(targetColumn).stre);
        //dataFrame.drop(targetColumn);
        //dataFrame.add(ValueVector.of(targetColumn, y));

        try(FeatureWriter featureWriter = new DelimitedFileFeatureWriter("entry-hyper-parameters.tsv","\t")){
            final List<String> headers = List.of("mtry","maxDepth","nodeSize","score");
            featureWriter.writeHeaders(headers);
            System.out.println(String.join(",", headers));
            for(int mtry = 1; mtry < dataFrame.ncol()/3; mtry++){
                for(int maxDepth = 2; maxDepth <= 20; maxDepth += 2){
                    for(int nodeSize = 5; nodeSize <= 50; nodeSize += 5){
                        final RandomForest.Options options = new RandomForest.Options(500, mtry, maxDepth, 0, nodeSize);
                        final double score = compute(10, formula, dataFrame, options);
                        System.out.printf("%d,%d,%d,%f\n", mtry, maxDepth, nodeSize, score);
                        featureWriter.writeValues(List.of((float)mtry, (float)maxDepth, (float)nodeSize, (float)score));
                    }
                }
            }
        }
    }

    public static double compute(int k, Formula formula, DataFrame dataFrame, RandomForest.Options options){
        double sum = 0;
        final Bag[] bags = CrossValidation.of(dataFrame.size(), k);
        for(int bagIndex = 0; bagIndex < k; bagIndex++){
            final Bag bag = bags[bagIndex];
            final DataFrame train = dataFrame.get(Index.of(bag.samples()));
            final RandomForest model = RandomForest.fit(formula, train, options);
            final DataFrame test = dataFrame.get(Index.of(bag.oob()));
            final int[] truth = formula.y(train).toIntArray();
            final double trainF1Score = f1(formula, train, model);
            final double testF1Score = f1(formula, test, model);
            sum += (testF1Score / Math.abs(trainF1Score - testF1Score));
        }
        return sum / k;
    }

    public static double f1(Formula formula, DataFrame dataFrame, RandomForest model){
        final int n = dataFrame.size();
        final int[] prediction = new int[n];
        final int[] truth = formula.y(dataFrame).toIntArray();
        for (int i = 0; i < n; i++) prediction[i] = model.predict(dataFrame.get(i));
        return FScore.F1.score(truth, prediction);
    }

    public static int[] discretizeByMedian(double[] target) {
        final double median = new DescriptiveStatistics(target).getPercentile(50);
        final int[] classes = new int[target.length];
        for (int i = 0; i < target.length; i++) classes[i] = target[i] <= median ? 0 : 1;
        return classes;
    }

}

