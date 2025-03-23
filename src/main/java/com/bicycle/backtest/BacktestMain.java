package com.bicycle.backtest;

import com.bicycle.backtest.report.BaseReport;
import com.bicycle.backtest.report.ReportBuilder;
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
import com.bicycle.core.rule.Rule;
import com.bicycle.core.rule.WaitForBarCountRule;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.provider.SymbolDataProvider;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;
import com.bicycle.util.Dates;

public class BacktestMain {

    public static void main1(String[] args) {
        final SymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider().equitiesOnly();
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        final FileSystemBarRepository barRepository = new FileSystemBarRepository(symbolRepository);
        barRepository.transpose(Exchange.NSE, Timeframe.D);
    }

    public static void main(String[] args) {
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
        final ReportBuilder reportBuilder = BaseReport.builder(symbolCount);
        final SingletonReportCache reportCache = new SingletonReportCache(reportBuilder, initialMargin, startDate, endDate);
        final TradingStrategyDefinition definition = new TradingStrategyDefinition(exchange);
        final PositionSizingStrategy positionSizingStrategy = new PercentageInitialMarginPositionSizingStrategy(percentagePositionSize, limitPositionSizeToAvailableMargin);

        final OrderType entryOrderType = OrderType.BUY;
        final Rule entryRule = cache.close().greaterThanOrEquals(cache.highest(cache.high(), 5));
        final Rule exitRule = new WaitForBarCountRule(15, cache);
        final MockTradingStrategy tradingStrategy = MockTradingStrategy.builder()
                .slippagePercentage(slippagePercentage)
                .entryRule(entryRule)
                .exitRule(exitRule)
                .entryOrderType(entryOrderType)
                .reportCache(reportCache)
                .atrIndicator(cache.atr(10))
                .positionSizingStrategy(positionSizingStrategy)
                .build();

        definition.getTimeframes().add(timeframe);
        definition.getSymbols().addAll(symbolRepository.findByExchange(exchange));
        definition.getTradingStrategies().add(tradingStrategy);

        final TradingStrategyExecutor executor = new SerialTradingStrategyExecutor(barRepository, cache);

        executor.execute(definition, startDate, endDate, reportCache);

        System.out.println(reportCache.getReport().toString());

    }

}
