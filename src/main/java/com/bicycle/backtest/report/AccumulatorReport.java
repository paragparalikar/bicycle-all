package com.bicycle.backtest.report;

import com.bicycle.backtest.report.callback.AccumulatorObserver;
import com.bicycle.backtest.report.callback.CompositeObserver;
import com.bicycle.util.Strings;
import lombok.experimental.Delegate;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Map;

public class AccumulatorReport implements Report {

    public static ReportBuilder builder(ReportBuilder delegateBuilder){
        return ((initialMargin, tradingStrategy, startDate, endDate) ->
                new AccumulatorReport(delegateBuilder.build(initialMargin, tradingStrategy, startDate, endDate)));
    }

    @Delegate private final Report delegate;
    private final AccumulatorObserver<Float> equityAccumulatorObserver = new AccumulatorObserver<>(Report::getEquity);
    private final AccumulatorObserver<Float> drawdownAccumulatorObserver = new AccumulatorObserver<>(Report::getDrawdown);

    public AccumulatorReport(Report delegate){
        this.delegate = new ObservableReport(new CompositeObserver(equityAccumulatorObserver, drawdownAccumulatorObserver), delegate);
    }

    public Map<Long, Float> getEquities(){
        return equityAccumulatorObserver.getValues();
    }

    public Map<Long, Float> getDrawdowns(){
        return drawdownAccumulatorObserver.getValues();
    }

    public float getSharpeRatio(){
        final double[] equities = equityAccumulatorObserver.getValuesAsList().stream().mapToDouble(Float::doubleValue).toArray();
        final DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(equities);
        final float standardDeviation = (float) (descriptiveStatistics.getStandardDeviation() / descriptiveStatistics.getMean());
        return (getCAGR() - 0.065f) / standardDeviation;
    }

    public float getSortinoRatio(){
        final double[] drawdowns = drawdownAccumulatorObserver.getValuesAsList().stream().mapToDouble(Float::doubleValue).toArray();
        final DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(drawdowns);
        final float standardDeviation = (float) (descriptiveStatistics.getStandardDeviation() / descriptiveStatistics.getMean());
        return (getCAGR() - 0.065f) / standardDeviation;
    }

    public float getCalmarRatio(){
        return getCAGR() / getMaxDrawdown();
    }

    @Override
    public String toString() {
        return delegate.toString() +
                Strings.format("Sharpe Ratio", getSharpeRatio()) +
                Strings.format("Sortino Ratio", getSortinoRatio()) +
                Strings.format("Calmar Ratio", getCalmarRatio());
    }
}
