package com.bicycle.backtest.main;

import com.bicycle.util.Constant;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.vector.ValueVector;
import smile.feature.transform.RobustStandardizer;
import smile.io.Read;
import smile.validation.ClassificationValidations;
import smile.validation.CrossValidation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;

public class EntryFilterOptimizationMain {

    public static void main(String[] args) throws IOException, URISyntaxException {
        final String targetColumn = "AVERAGE_MFE";
        final Formula formula = Formula.lhs(targetColumn);
        final Path path = Paths.get(Constant.HOME, "reports", "features.tsv");
        DataFrame dataFrame = Read.csv(path.toString(),"delimiter=\\t,header=true,comment=#,escape=\\,quote=\"").dropna();
        dataFrame = RobustStandardizer.fit(dataFrame).apply(dataFrame);
        final int[] y = discretizeByMedian(dataFrame.column(targetColumn).toDoubleArray());
        dataFrame.drop(targetColumn);
        dataFrame.add(ValueVector.of(targetColumn, y));

        System.out.println("mtry,maxDepth,nodeSize,score");
        for(int mtry = 1; mtry < dataFrame.ncol()/3; mtry++){
            for(int maxDepth = 2; maxDepth <= 20; maxDepth += 2){
                for(int nodeSize = 5; nodeSize <= 50; nodeSize += 5){
                    final RandomForest.Options options = new RandomForest.Options(500, mtry, maxDepth, 0, nodeSize);
                    final double score = compute(10, formula, dataFrame, options);
                    System.out.printf("%d,%d,%d,%f\n", mtry, maxDepth, nodeSize, score);
                }
            }
        }
    }

    public static double compute(int k, Formula formula, DataFrame dataFrame, RandomForest.Options options){
        final BiFunction<Formula, DataFrame, RandomForest> trainer = (f, d) -> RandomForest.fit(f, d, options);
        final ClassificationValidations<RandomForest> validations = CrossValidation.classification(k, formula, dataFrame, trainer);
        return validations.avg().f1() / Math.pow(1 + validations.std().f1(), 2);
    }

    public static int[] discretizeByMedian(double[] target) {
        final double median = new DescriptiveStatistics(target).getPercentile(50);
        final int[] classes = new int[target.length];
        for (int i = 0; i < target.length; i++) classes[i] = target[i] <= median ? 0 : 1;
        return classes;
    }

}

