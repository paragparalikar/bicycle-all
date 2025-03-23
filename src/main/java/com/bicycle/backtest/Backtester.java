package com.bicycle.backtest;

import com.bicycle.backtest.report.BaseReport;
import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.ReportBuilder;
import com.bicycle.backtest.report.accumulator.DrawdownAccumulatorReport;
import com.bicycle.backtest.report.accumulator.EquityAccumulatorReport;
import com.bicycle.backtest.report.accumulator.PositionAccumulatorReport;
import com.bicycle.backtest.report.cache.SingletonReportCache;
import com.bicycle.backtest.strategy.positionSizing.PercentageInitialMarginPositionSizingStrategy;
import com.bicycle.backtest.strategy.positionSizing.PositionSizingStrategy;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.backtest.strategy.trading.TradingStrategyDefinition;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
import com.bicycle.backtest.strategy.trading.executor.SerialTradingStrategyExecutor;
import com.bicycle.backtest.strategy.trading.executor.TradingStrategyExecutor;
import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.bar.repository.FileSystemBarRepository;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.provider.SymbolDataProvider;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;
import com.bicycle.util.Constant;
import com.bicycle.util.Dates;
import lombok.*;
import lombok.experimental.Accessors;
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
import java.util.UUID;
import java.util.function.Predicate;

@Builder
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
public class Backtester {

    @Setter
    @Accessors(fluent = true, chain = true)
    public static class Show {
        private boolean equityCurve, drawdownCurve;
        public boolean isShow() { return equityCurve || drawdownCurve; }
    }

    @Setter
    @Accessors(fluent = true, chain = true)
    public static class Export {
        private boolean positions;
    }

    @Getter private final Show show = new Show();
    @Getter private final Export export = new Export();
    private TradingStrategyBuilder tradingStrategyBuilder;
    @Builder.Default private float initialMargin = 100000;
    @Builder.Default private Exchange exchange = Exchange.NSE;
    @Builder.Default private Timeframe timeframe = Timeframe.D;
    @Builder.Default private float percentagePositionSize = 2.0f;
    @Builder.Default private float slippagePercentage = 0.5f;
    @Builder.Default private boolean limitPositionSizeToAvailableMargin = false;
    @Builder.Default private Predicate<Symbol> symbolPredicate = symbol -> true;

    private final SymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider().equitiesOnly();
    private final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
    private final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);

    @SneakyThrows
    public void run(long startDate, long endDate){
        final List<Symbol> symbols = symbolRepository.findByExchange(exchange).stream().filter(symbolPredicate).toList();
        System.out.printf("Backtesting %d symbols from %s to %s\n", symbols.size(), Dates.format(startDate), Dates.format(endDate));
        final IndicatorCache cache = new IndicatorCache(symbols.size(), 1);

        final ReportBuilder reportBuilder = createReportBuilder(symbols.size());
        final SingletonReportCache reportCache = new SingletonReportCache(reportBuilder, initialMargin, startDate, endDate);
        final PositionSizingStrategy positionSizingStrategy = new PercentageInitialMarginPositionSizingStrategy(percentagePositionSize, limitPositionSizeToAvailableMargin);
        final List<MockTradingStrategy> tradingStrategies = tradingStrategyBuilder.build(slippagePercentage, cache, reportCache, positionSizingStrategy);
        final TradingStrategyDefinition tradingStrategyDefinition = createTradingStrategyDefinition(symbols, tradingStrategies);

        final TradingStrategyExecutor tradingStrategyExecutor = new SerialTradingStrategyExecutor(barRepository, cache);
        tradingStrategyExecutor.execute(tradingStrategyDefinition, startDate, endDate, reportCache);
        final Report report = reportCache.getReport();
        if(export.positions) exportPositions(report);
        if(show.isShow()) show(report);
    }

    private void show(Report report) throws InterruptedException, InvocationTargetException {
        final PlotGrid plotGrid = new PlotGrid();
        if(show.equityCurve) plotGrid.add(createEquityCurvePlot(report));
        if(show.drawdownCurve) plotGrid.add(createDrawdownCurvePlot(report));
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



    private void exportPositions(Report report) throws IOException {
        System.out.println("Exporting positions...");
        final PositionAccumulatorReport positionAccumulatorReport = report.unwrap(PositionAccumulatorReport.class);
        final List<MockPosition> positions = positionAccumulatorReport.getPositions();
        final List<String> lines = new ArrayList<>(positions.size() + 1);
        lines.add(MockPosition.getCsvHeaders());
        positions.stream().map(MockPosition::toCSV).forEach(lines::add);
        final Path path = Paths.get(Constant.HOME, "reports", UUID.randomUUID().toString() + ".csv");
        Files.createDirectories(path.getParent());
        Files.write(path, lines);
        System.out.printf("Exported %d positions to %s\n", positions.size(), path.toString());
    }

    private ReportBuilder createReportBuilder(int symbolCount){
        ReportBuilder reportBuilder = BaseReport.builder(symbolCount);
        if(show.equityCurve) reportBuilder = EquityAccumulatorReport.builder(reportBuilder);
        if(show.drawdownCurve) reportBuilder = DrawdownAccumulatorReport.builder(reportBuilder);
        if(export.positions) reportBuilder = PositionAccumulatorReport.builder(reportBuilder);
        return reportBuilder;
    }

    private TradingStrategyDefinition createTradingStrategyDefinition(List<Symbol> symbols, List<MockTradingStrategy> tradingStrategies){
        final TradingStrategyDefinition definition = new TradingStrategyDefinition(exchange);
        definition.getSymbols().addAll(symbols);
        definition.getTimeframes().add(timeframe);
        definition.getTradingStrategies().addAll(tradingStrategies);
        return definition;
    }

}
