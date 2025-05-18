package com.bicycle.backtest.workflow.stage.feature;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import smile.data.DataFrame;
import smile.data.vector.ValueVector;

public class FeatureDiscretizationStage {

    public DataFrame execute(DataFrame dataFrame, String... names) {
        System.out.println("\n--------------- Initiating feature discretization stage ---------------");
        for(String name : names){
            final int[] values = discretizeByMedian(dataFrame.column(name).toDoubleArray());
            dataFrame = dataFrame.drop(name).add(ValueVector.of(name, values));
            System.out.println("Discretized feature by median : " + name);
        }
        return dataFrame;
    }

    public int[] discretizeByMedian(double[] target) {
        final double median = new DescriptiveStatistics(target).getPercentile(50);
        final int[] classes = new int[target.length];
        for (int i = 0; i < target.length; i++) classes[i] = target[i] <= median ? 0 : 1;
        return classes;
    }

}
