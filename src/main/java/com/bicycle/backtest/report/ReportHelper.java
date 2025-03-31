package com.bicycle.backtest.report;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.report.accumulator.DrawdownAccumulatorReport;
import com.bicycle.backtest.report.accumulator.EquityAccumulatorReport;
import com.bicycle.backtest.report.accumulator.PositionAccumulatorReport;
import com.bicycle.util.Constant;
import smile.plot.swing.Line;
import smile.plot.swing.LinePlot;
import smile.plot.swing.PlotGrid;
import smile.plot.swing.PlotPanel;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportHelper {

    public void show(Report report) throws InterruptedException, InvocationTargetException {
        System.out.println(report.unwrap(BaseReport.class));
        final PlotGrid plotGrid = new PlotGrid();
        plotGrid.add(createEquityCurvePlot(report));
        plotGrid.add(createDrawdownCurvePlot(report));
        plotGrid.window();
    }

    private PlotPanel createDrawdownCurvePlot(Report report){
        final DrawdownAccumulatorReport drawdownAccumulatorReport = report.unwrap(DrawdownAccumulatorReport.class);
        final double[] drawdowns = drawdownAccumulatorReport.getDrawdowns().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .mapToDouble(Map.Entry::getValue)
                .toArray();
        final PlotPanel panel = LinePlot.of(drawdowns, Line.Style.SOLID, Color.RED, "Drawdown").canvas().panel();
        panel.add(panel.getToolbar(), "North");
        return panel;
    }

    private PlotPanel createEquityCurvePlot(Report report){
        final EquityAccumulatorReport equityAccumulatorReport = report.unwrap(EquityAccumulatorReport.class);
        final double[] equities = equityAccumulatorReport.getEquities().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .mapToDouble(Map.Entry::getValue)
                .toArray();
        final PlotPanel panel = LinePlot.of(equities, Line.Style.SOLID, Color.GREEN, "Equity").canvas().panel();
        panel.add(panel.getToolbar(), "North");
        return panel;
    }

    public void exportPositions(String name, Report report) throws IOException {
        System.out.println("Exporting positions...");
        final PositionAccumulatorReport positionAccumulatorReport = report.unwrap(PositionAccumulatorReport.class);
        final List<MockPosition> positions = positionAccumulatorReport.getPositions();
        final List<String> lines = new ArrayList<>(positions.size() + 1);
        lines.add(MockPosition.getCsvHeaders());
        positions.stream().map(MockPosition::toCSV).forEach(lines::add);
        final Path path = Paths.get(Constant.HOME, "reports", name + ".csv");
        Files.createDirectories(path.getParent());
        Files.write(path, lines);
        System.out.printf("Exported %d positions to %s\n", positions.size(), path.toString());
    }

}
