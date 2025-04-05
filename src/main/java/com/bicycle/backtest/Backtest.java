package com.bicycle.backtest;

import com.bicycle.backtest.executor.BacktestExecutor;
import com.bicycle.backtest.executor.SerialBacktestExecutor;
import com.bicycle.backtest.report.BaseReport;
import com.bicycle.backtest.report.ReportBuilder;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.strategy.positionSizing.PercentageInitialMarginPositionSizingStrategy;
import com.bicycle.backtest.strategy.positionSizing.PositionSizingStrategy;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.backtest.strategy.trading.builder.TradingStrategyBuilder;
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
import com.bicycle.util.Dates;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true, fluent = false)
public class Backtest {

    private ReportCache reportCache;
    private Collection<Symbol> symbols;
    private BarRepository barRepository;
    private IndicatorCache indicatorCache;
    private SymbolRepository symbolRepository;
    private BacktestExecutor backtestExecutor;
    private SymbolDataProvider symbolDataProvider;
    private TradingStrategyBuilder tradingStrategyBuilder;
    private PositionSizingStrategy positionSizingStrategy;
    private List<MockTradingStrategy> tradingStrategies;
    private float initialMargin = 100000f;
    private float slippagePercentage = 0.5f;
    private float percentagePositionSize = 2.0f;
    private boolean limitPositionSizeToAvailableMargin = false;
    private long startDate = Dates.toEpochMillis(2010, 1, 1);
    private long endDate = Dates.toEpochMillis(2019, 12, 31);
    private Exchange exchange = Exchange.NSE;
    private ReportBuilder reportBuilder = BaseReport::new;
    private int reportCacheOptions = ReportCache.SINGLETON;
    private Set<Timeframe> timeframes = Set.of(Timeframe.D);

    private void printInfo(){
        System.out.printf("""
                Running backtest with below config
                Exchange            : %s
                Timeframes          : %s
                Symbols             : %d
                Trading Strategies  : %d
                Start Date          : %s
                End Date            : %s
                Initial Margin      : %8.2f
                Slippage(%%)         : %3.2f
                Position Size(%%)    : %3.2f
                Limit Margin        : %b
                %n""", exchange.name(), timeframes.stream().map(Enum::name).collect(Collectors.joining(",")),
                symbols.size(), tradingStrategies.size(), Dates.format(startDate), Dates.format(endDate),
                initialMargin, slippagePercentage, percentagePositionSize, limitPositionSizeToAvailableMargin);
    }

    public ReportCache run(){
        if(null == symbolDataProvider) symbolDataProvider = new KiteSymbolDataProvider();
        if(null == symbolRepository) symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        if(null == barRepository) barRepository = new FileSystemBarRepository(symbolRepository);
        if(null == symbols) this.symbols = symbolRepository.findByExchange(exchange);
        if(null == indicatorCache) indicatorCache = new IndicatorCache(symbols.size(), timeframes.size());
        if(null == backtestExecutor) backtestExecutor = new SerialBacktestExecutor(barRepository, indicatorCache);
        if(null == positionSizingStrategy) positionSizingStrategy = new PercentageInitialMarginPositionSizingStrategy(percentagePositionSize, limitPositionSizeToAvailableMargin);
        if(null == reportCache) reportCache = ReportCache.of(initialMargin, startDate, endDate, reportBuilder, reportCacheOptions);
        if(null == tradingStrategies) tradingStrategies = tradingStrategyBuilder.build(slippagePercentage, indicatorCache, reportCache, positionSizingStrategy);
        printInfo();
        backtestExecutor.execute(this, startDate, endDate, reportCache);
        return reportCache;
    }

}
