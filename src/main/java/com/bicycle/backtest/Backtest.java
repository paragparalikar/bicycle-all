package com.bicycle.backtest;

import com.bicycle.backtest.executor.BacktestExecutor;
import com.bicycle.backtest.executor.SerialBacktestExecutor;
import com.bicycle.backtest.report.FeatureReport;
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
import lombok.*;
import lombok.experimental.Accessors;

import java.util.*;

@Data
@Accessors(chain = true, fluent = false)
public class Backtest {

    public static Backtest of(Set<Symbol> symbols, TradingStrategyBuilder tradingStrategyBuilder){
        return new Backtest().setTradingStrategyBuilder(tradingStrategyBuilder).setSymbols(symbols);
    }

    private Collection<Symbol> symbols;
    private ReportCache reportCache;
    private ReportBuilder reportBuilder;
    private BarRepository barRepository;
    private IndicatorCache indicatorCache;
    private SymbolRepository symbolRepository;
    private BacktestExecutor backtestExecutor;
    private SymbolDataProvider symbolDataProvider;
    private FeatureReport.FeatureCaptor featureCaptor;
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
    private int reportBuilderOptions = ReportBuilder.BASE;
    private int reportCacheOptions = ReportCache.SINGLETON;
    private Set<Timeframe> timeframes = Set.of(Timeframe.D);

    public ReportCache run(){
        if(null == symbolDataProvider) symbolDataProvider = new KiteSymbolDataProvider();
        if(null == symbolRepository) symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        if(null == barRepository) barRepository = new FileSystemBarRepository(symbolRepository);
        if(null == symbols) setSymbols(symbolRepository.findByExchange(exchange));
        if(null == backtestExecutor) backtestExecutor = new SerialBacktestExecutor(barRepository, indicatorCache);
        if(null == positionSizingStrategy) positionSizingStrategy = new PercentageInitialMarginPositionSizingStrategy(percentagePositionSize, limitPositionSizeToAvailableMargin);
        reportCache = ReportCache.of(initialMargin, startDate, endDate, reportBuilder, reportCacheOptions);
        tradingStrategies = tradingStrategyBuilder.build(slippagePercentage, indicatorCache, reportCache, positionSizingStrategy);
        backtestExecutor.execute(this, startDate, endDate, reportCache);
        return reportCache;
    }

    public Backtest setSymbols(Collection<Symbol> symbols){
        this.symbols = symbols;
        indicatorCache = new IndicatorCache(symbols.size(), 1);
        reportBuilder = ReportBuilder.of(symbols.size(), featureCaptor, reportBuilderOptions);
        return this;
    }

}
