package com.bicycle.backtest;

import com.bicycle.backtest.report.BaseReport;
import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.ReportBuilder;
import com.bicycle.backtest.report.accumulator.DrawdownAccumulatorReport;
import com.bicycle.backtest.report.accumulator.EquityAccumulatorReport;
import com.bicycle.backtest.report.accumulator.PositionAccumulatorReport;
import com.bicycle.backtest.report.cache.ReportCache;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Backtester {

    @Builder.Default private final float initialMargin = 100000f;
    @Builder.Default private final Exchange exchange = Exchange.NSE;
    @Builder.Default private final Timeframe timeframe = Timeframe.D;
    @Builder.Default private final float percentagePositionSize = 2.0f;
    @Builder.Default private final float slippagePercentage = 0.5f;
    @Builder.Default private final boolean limitPositionSizeToAvailableMargin = false;
    @Builder.Default private final ReportBuilder.Customizer reportBuilderCustomizer = BaseReport::builder;
    @Builder.Default private final ReportCache.Customizer reportCacheCustomizer = SingletonReportCache::new;
    @Builder.Default private final SymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider().equitiesOnly();
    @Builder.Default private final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
    @Builder.Default private final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);

    public ReportCache run(TradingStrategyBuilder tradingStrategyBuilder, long startDate, long endDate){
        final Collection<Symbol> symbols = symbolRepository.findByExchange(exchange);
        final ReportBuilder reportBuilder = reportBuilderCustomizer.customize(symbols.size());
        final ReportCache reportCache = reportCacheCustomizer.customize(initialMargin, startDate, endDate, reportBuilder);
        final IndicatorCache cache = new IndicatorCache(symbols.size(), 1);
        final PositionSizingStrategy positionSizingStrategy = new PercentageInitialMarginPositionSizingStrategy(percentagePositionSize, limitPositionSizeToAvailableMargin);
        final List<MockTradingStrategy> tradingStrategies = tradingStrategyBuilder.build(slippagePercentage, cache, reportCache, positionSizingStrategy);
        final TradingStrategyDefinition tradingStrategyDefinition = createTradingStrategyDefinition(symbols, tradingStrategies);
        final TradingStrategyExecutor tradingStrategyExecutor = new SerialTradingStrategyExecutor(barRepository, cache);
        tradingStrategyExecutor.execute(tradingStrategyDefinition, startDate, endDate, reportCache);
        return reportCache;
    }

    private TradingStrategyDefinition createTradingStrategyDefinition(Collection<Symbol> symbols, List<MockTradingStrategy> tradingStrategies){
        final TradingStrategyDefinition definition = new TradingStrategyDefinition(exchange);
        definition.getSymbols().addAll(symbols);
        definition.getTimeframes().add(timeframe);
        definition.getTradingStrategies().addAll(tradingStrategies);
        return definition;
    }

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
