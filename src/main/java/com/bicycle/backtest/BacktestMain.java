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
import com.bicycle.backtest.strategy.trading.executor.SerialTradingStrategyExecutor;
import com.bicycle.backtest.strategy.trading.executor.TradingStrategyExecutor;
import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.bar.repository.FileSystemBarRepository;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.position.Position;
import com.bicycle.core.rule.Rule;
import com.bicycle.core.rule.WaitForBarCountRule;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.provider.SymbolDataProvider;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;
import com.bicycle.util.Dates;
import smile.plot.swing.Histogram;
import smile.plot.swing.LinePlot;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class BacktestMain {

    public static void main1(String[] args) {
        final SymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider().equitiesOnly();
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        final FileSystemBarRepository barRepository = new FileSystemBarRepository(symbolRepository);
        barRepository.transpose(Exchange.NSE, Timeframe.D);
    }

    public static void main(String[] args) throws InterruptedException, InvocationTargetException, IOException {
        final float initialMargin = 100000;
        final Exchange exchange = Exchange.NSE;
        final Timeframe timeframe = Timeframe.D;
        final float percentagePositionSize = 2.0f;
        final float slippagePercentage = 0.5f;
        final boolean limitPositionSizeToAvailableMargin = true;
        final long startDate = Dates.toEpochMillis(2014, 1, 1);
        final long endDate = Dates.toEpochMillis(2023, 12, 31);

        final SymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider().equitiesOnly();
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);

        final int symbolCount = symbolRepository.findByExchange(exchange).size();
        final IndicatorCache cache = new IndicatorCache(symbolCount, 1);
        final ReportBuilder reportBuilder = DrawdownAccumulatorReport.builder(EquityAccumulatorReport.builder(
                PositionAccumulatorReport.builder(BaseReport.builder(symbolCount))));
        final SingletonReportCache reportCache = new SingletonReportCache(reportBuilder, initialMargin, startDate, endDate);
        final TradingStrategyDefinition definition = new TradingStrategyDefinition(exchange);



        definition.getTimeframes().add(timeframe);
        definition.getSymbols().addAll(symbolRepository.findByExchange(exchange));
        definition.getTradingStrategies().add(createTradingStrategy(slippagePercentage, percentagePositionSize, cache, reportCache));

        final TradingStrategyExecutor executor = new SerialTradingStrategyExecutor(barRepository, cache);

        executor.execute(definition, startDate, endDate, reportCache);

        final Report report = reportCache.getReport();
        final EquityAccumulatorReport equityAccumulatorReport = report.unwrap(EquityAccumulatorReport.class);
        final DrawdownAccumulatorReport drawdownAccumulatorReport = report.unwrap(DrawdownAccumulatorReport.class);
        final PositionAccumulatorReport positionAccumulatorReport = report.unwrap(PositionAccumulatorReport.class);

        final double[] equities = equityAccumulatorReport.getEquities().values().stream().mapToDouble(Float::doubleValue).toArray();
        System.out.println(report.unwrap(BaseReport.class).toString());
        //showHistogram(report, Position::getClosePercentProfitLoss);
        showEquityCurve(report);

        writeCSV(report);

    }

    private static MockTradingStrategy createTradingStrategy(float slippagePercentage,
                                                             float percentagePositionSize,
                                                             IndicatorCache cache,
                                                             ReportCache reportCache){
        final OrderType entryOrderType = OrderType.BUY;
        final Rule liquidityRule = cache.close().multipliedBy(cache.volume()).greaterThan(100000000)
                .and(cache.close().greaterThan(3.0f));
        final Rule entryRule = liquidityRule.and(cache.close().greaterThanOrEquals(cache.highest(cache.high(), 5)));
        final Rule exitRule = new WaitForBarCountRule(15, cache);
        final PositionSizingStrategy positionSizingStrategy = new PercentageInitialMarginPositionSizingStrategy(percentagePositionSize, false);
        return MockTradingStrategy.builder()
                .slippagePercentage(slippagePercentage)
                .entryRule(entryRule)
                .exitRule(exitRule)
                .entryOrderType(entryOrderType)
                .reportCache(reportCache)
                .atrIndicator(cache.atr(10))
                .positionSizingStrategy(positionSizingStrategy)
                .build();
    }

    private static void showHistogram(Report report, ToDoubleFunction<MockPosition> function) throws InterruptedException, InvocationTargetException {
        final double[] values = report.unwrap(PositionAccumulatorReport.class).getPositions().stream()
                .mapToDouble(function).toArray();
        Histogram.of(values).canvas().window();
    }

    private static void showEquityCurve(Report report) throws IOException, InterruptedException, InvocationTargetException {
        final EquityAccumulatorReport equityAccumulatorReport = report.unwrap(EquityAccumulatorReport.class);
        final double[] equities = equityAccumulatorReport.getEquities().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .mapToDouble(Float::doubleValue)
                .toArray();
        LinePlot.of(equities).canvas().window();
    }


    private static void writeCSV(Report report) throws IOException {
        final PositionAccumulatorReport positionAccumulatorReport = report.unwrap(PositionAccumulatorReport.class);
        final List<String> lines = new ArrayList<>();
        lines.add(MockPosition.getCsvHeaders());
        positionAccumulatorReport.getPositions().stream().map(position -> position.toCSV()).forEach(lines::add);
        Files.write(Paths.get("/home/parag/test.csv"), lines);
    }

}
