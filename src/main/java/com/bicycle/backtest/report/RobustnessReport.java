package com.bicycle.backtest.report;

import com.bicycle.backtest.MockPosition;
import com.bicycle.util.Strings;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class RobustnessReport implements Report {

    public static ReportBuilder builder(ReportBuilder delegateBuilder, long step){
        return (initialMargin, tradingStrategy, startDate, endDate) -> new RobustnessReport(
                delegateBuilder.build(initialMargin, tradingStrategy, startDate, endDate), startDate, step);
    }

    @Delegate
    private final Report delegate;
    private final long start, step;
    private final Map<Integer, List<Float>> allScores = new HashMap<>();
    private final SecondMoment secondMoment = new SecondMoment();
    private final Mean mean = new Mean(secondMoment);
    private final Variance variance = new Variance(secondMoment);

    @Override
    public void clear(){
        delegate.clear();
        allScores.clear();
        secondMoment.clear();
        mean.clear();
        variance.clear();
    }

    @Override
    public void close(MockPosition position) {
        final int index = (int)((position.getEntryDate() - start) / step);
        final List<Float> scores = allScores.computeIfAbsent(index, key -> new ArrayList<>());
        scores.add(position.getMfe());
    }

    public float compute(){
        final double[] values = allScores.values().stream()
                .map(this::mean)
                .mapToDouble(Double::doubleValue)
                .toArray();
        return (float) (mean.evaluate(values) / variance.evaluate(values));
    }

    private double mean(List<Float> scores){
        final double[] values = scores.stream().mapToDouble(Float::doubleValue).toArray();
        return mean.evaluate(values);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Report> T unwrap(Class<T> type) {
        if(type.isAssignableFrom(getClass())) return (T) this;
        else return delegate.unwrap(type);
    }

    @Override
    public String toString() {
        return super.toString()
                + Strings.format("Robustness", (float) compute());
    }
}
