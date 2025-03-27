package com.bicycle.backtest;

import com.bicycle.backtest.report.BaseReport;
import com.bicycle.backtest.report.Report;
import com.bicycle.backtest.report.cache.ReportCache;
import com.bicycle.backtest.report.cache.TradingStrategyReportCache;
import com.bicycle.backtest.strategy.positionSizing.PercentageInitialMarginPositionSizingStrategy;
import com.bicycle.backtest.strategy.positionSizing.PositionSizingStrategy;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.backtest.strategy.trading.TradingStrategyDefinition;
import com.bicycle.backtest.strategy.trading.builder.RuleTradingStrategyBuilder;
import com.bicycle.backtest.strategy.trading.executor.ParallelTradingStrategyExecutor;
import com.bicycle.backtest.strategy.trading.executor.TradingStrategyExecutor;
import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.bar.repository.FileSystemBarRepository;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.order.OrderType;
import com.bicycle.core.rule.builder.RuleBuilder;
import com.bicycle.core.rule.builder.WaitForBarCountRuleBuilder;
import com.bicycle.core.rule.builder.sugar.LiquidityRuleBuilder;
import com.bicycle.core.rule.builder.sugar.LongBarCountCloseBreakoutRuleBuilder;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.provider.SymbolDataProvider;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;
import com.bicycle.util.Dates;
import com.bicycle.util.FloatIterator;
import com.bicycle.util.IntegerIterator;
import com.bicycle.util.ResetableIterator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BacktesterMain {

    public static void main(String[] args) {
        final float initialMargin = 100000f;
        final Exchange exchange = Exchange.NSE;
        final Timeframe timeframe = Timeframe.D;
        final float percentagePositionSize = 2.0f;
        final float slippagePercentage = 0.5f;
        final boolean limitPositionSizeToAvailableMargin = false;
        final long startDate = Dates.toEpochMillis(2014, 1, 1);
        final long endDate = Dates.toEpochMillis(2023, 12, 31);
        final SymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider();
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);
        final RuleTradingStrategyBuilder tradingStrategyBuilder = createTradingStrategyBuilder();

        final Collection<Symbol> symbols = symbolRepository.findByExchange(exchange);
        final TradingStrategyReportCache reportCache = new TradingStrategyReportCache(initialMargin, startDate, endDate, BaseReport.builder(symbols.size()));
        final IndicatorCache cache = new IndicatorCache(symbols.size(), 1);
        final PositionSizingStrategy positionSizingStrategy = new PercentageInitialMarginPositionSizingStrategy(percentagePositionSize, limitPositionSizeToAvailableMargin);
        final List<MockTradingStrategy> tradingStrategies = tradingStrategyBuilder.build(slippagePercentage, cache, reportCache, positionSizingStrategy);
        final TradingStrategyDefinition tradingStrategyDefinition = new TradingStrategyDefinition(exchange);
        tradingStrategyDefinition.getSymbols().addAll(symbols);
        tradingStrategyDefinition.getTimeframes().add(timeframe);
        tradingStrategyDefinition.getTradingStrategies().addAll(tradingStrategies);
        final TradingStrategyExecutor tradingStrategyExecutor = new ParallelTradingStrategyExecutor(barRepository, cache);
        tradingStrategyExecutor.execute(tradingStrategyDefinition, startDate, endDate, reportCache);

        final List<Report> reports = getSortedReports(reportCache, tradingStrategies);
        final Map<String, List<Double>> parameters = ResetableIterator.toMap(tradingStrategyBuilder.getIterators());


    }

    private static List<Report> getSortedReports(ReportCache reportCache, List<MockTradingStrategy> tradingStrategies){
        final TradingStrategyReportCache tradingStrategyReportCache = TradingStrategyReportCache.class.cast(reportCache);
        final Map<String, Report> reportMap = tradingStrategyReportCache.findAll().stream()
                .collect(Collectors.toMap(report -> report.getTradingStrategy().toString(), Function.identity()));
        return tradingStrategies.stream().map(Object::toString).map(reportMap::get).toList();
    }

    private static RuleTradingStrategyBuilder createTradingStrategyBuilder(){
        final IntegerIterator minVolumeIterator = new IntegerIterator("minVolume", 100000, 100000, 100000,  1);
        final FloatIterator minPriceIterator = new FloatIterator("minPrice", 5, 5, 5, 1);
        final IntegerIterator smaBarCountIterator = new IntegerIterator("smaBarCount", 10, 10, 10, 1);
        final IntegerIterator barCountIterator = new IntegerIterator("barCount", 5, 2, 15, 1);
        final IntegerIterator waitForBarCountIterator = new IntegerIterator("waitForBarCount", 15, 15, 15, 1);
        final List<ResetableIterator> iterators = Arrays.asList(minVolumeIterator, minPriceIterator, smaBarCountIterator, barCountIterator, waitForBarCountIterator);
        final RuleBuilder entryRuleBuilder = LiquidityRuleBuilder.builder()
                .minVolumeIterator(minVolumeIterator)
                .minPriceIterator(minPriceIterator)
                .smaBarCountIterator(smaBarCountIterator)
                .build().and(LongBarCountCloseBreakoutRuleBuilder.builder()
                        .barCountIterator(barCountIterator)
                        .build());
        final RuleBuilder exitRuleBuilder = WaitForBarCountRuleBuilder.builder()
                .integerIterator(waitForBarCountIterator)
                .build();
        return new RuleTradingStrategyBuilder(entryRuleBuilder, exitRuleBuilder,
                OrderType.BUY, iterators);
    }

}
