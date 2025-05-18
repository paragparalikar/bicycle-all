package com.bicycle.util;

import lombok.experimental.UtilityClass;
import smile.math.MathEx;
import smile.sort.IQAgent;

@UtilityClass
public class Transforms {

    public double[] robustStandardize(double[] feature){
        final IQAgent agent = new IQAgent();
        for (double v : feature) agent.add(v);
        final double median = agent.quantile(0.5);
        final double iqr = agent.quantile(0.75) - agent.quantile(0.25);
        final double scale = MathEx.isZero(iqr) ? 1.0 : iqr;
        final double[] transform = new double[feature.length];
        for(int index = 0; index < feature.length; index++) transform[index] = (feature[index] - median) / scale;
        return transform;
    }

}
