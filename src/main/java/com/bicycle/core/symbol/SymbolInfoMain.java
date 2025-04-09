package com.bicycle.core.symbol;

import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Cursor;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.bar.repository.FileSystemBarRepository;
import com.bicycle.core.indicator.Indicator;
import com.bicycle.core.indicator.IndicatorCache;
import com.bicycle.core.symbol.provider.SymbolDataProvider;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.FileSystemSymbolInfoRepository;
import com.bicycle.core.symbol.repository.SymbolInfoRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;
import com.bicycle.util.Dates;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolInfoMain {

    public static void main(String[] args) throws Exception {
        final int barCount = 14;
        final Exchange exchange = Exchange.NSE;
        final Timeframe timeframe = Timeframe.D;
        final long from = Dates.toEpochMillis(LocalDateTime.now().minusYears(1));
        final long to = Dates.toEpochMillis(LocalDateTime.now());
        System.out.printf("Initiating symbol info data extraction with below settings:" +
                "\nExchange            : " + exchange.name() +
                "\nTimeframe           : " + timeframe.name() +
                "\nBarCount            : " + String.valueOf(barCount) +
                "\nFrom                : " + Dates.format(from) +
                "\nTo                  : " + Dates.format(to));

        final SymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider();
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);
        final SymbolInfoRepository symbolInfoRepository = new FileSystemSymbolInfoRepository();
        final Collection<Symbol> symbols = symbolRepository.findByExchange(exchange);
        System.out.printf("\nSymbolCount         : " + symbols.size());
        final IndicatorCache indicatorCache = new IndicatorCache(symbols.size(), 1);

        final Indicator efficiencyIndicator = indicatorCache.efficiency(barCount);
        final Indicator standardDeviationIndicator = indicatorCache.stdDev(indicatorCache.close(), barCount)
                .dividedBy(indicatorCache.sma(indicatorCache.close(), barCount));
        final Indicator volumeIndicator = indicatorCache.volume();
        final Indicator turnoverIndicator = indicatorCache.volume().multipliedBy(indicatorCache.close());
        final Indicator spreadIndicator = indicatorCache.spread().dividedBy(indicatorCache.close());

        final Map<Symbol, Double> efficiencyValues = new HashMap<>();
        final Map<Symbol, Double> standardDeviationValues = new HashMap<>();
        final Map<Symbol, Double> volumeValues = new HashMap<>();
        final Map<Symbol, Double> turnoverValues = new HashMap<>();
        final Map<Symbol, Double> spreadValues = new HashMap<>();

        try(Cursor<Bar> cursor = barRepository.get(exchange, timeframe, from, to)){
            final Bar bar = new Bar();
            for(int index = 0; index < cursor.size(); index++) {
                cursor.advance(bar);
                final Symbol symbol = bar.symbol();
                if(null != symbol) {
                    indicatorCache.onBar(bar);
                    putValue(symbol, timeframe, efficiencyValues, efficiencyIndicator);
                    putValue(symbol, timeframe, standardDeviationValues, standardDeviationIndicator);
                    putValue(symbol, timeframe, volumeValues, volumeIndicator);
                    putValue(symbol, timeframe, turnoverValues, turnoverIndicator);
                    putValue(symbol, timeframe, spreadValues, spreadIndicator);
                }
            }
        }
        final Map<Symbol, SymbolAspect> efficiencies = rank(efficiencyValues);
        final Map<Symbol, SymbolAspect> stdDevs = rank(standardDeviationValues);
        final Map<Symbol, SymbolAspect> volumes = rank(volumeValues);
        final Map<Symbol, SymbolAspect> turnovers = rank(turnoverValues);
        final Map<Symbol, SymbolAspect> spreads = rank(spreadValues);

        final List<SymbolInfo> symbolInfos = symbols.stream()
                .map(symbol -> SymbolInfo.builder()
                        .token(symbol.token())
                        .efficiency(efficiencies.get(symbol))
                        .volatility(stdDevs.get(symbol))
                        .volume(volumes.get(symbol))
                        .turnover(turnovers.get(symbol))
                        .spread(spreads.get(symbol))
                        .build())
                .toList();
        System.out.printf("\nPersisting %d symbolInofs...", symbolInfos.size());
        symbolInfoRepository.saveAll(symbolInfos);
        System.out.println("\tCompleted");

    }

    private static Map<Symbol, SymbolAspect> rank(Map<Symbol, Double> map){
        final List<Symbol> symbols = map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
        final Map<Symbol, SymbolAspect> results = new HashMap<>();
        for(int index = 0; index < symbols.size(); index++){
            final Level level = index <= symbols.size() / 3 ? Level.LOW :
                    (index >= symbols.size() * 2 / 3 ? Level.HIGH : Level.MEDIUM);
            results.put(symbols.get(index), new SymbolAspect(index, level));
        }
        return results;
    }

    private static void putValue(Symbol symbol, Timeframe timeframe, Map<Symbol, Double> map, Indicator indicator){
        final double lastValue = map.getOrDefault(symbol, 0d);
        final double currentValue = indicator.getValue(symbol, timeframe);
        map.put(symbol, lastValue + (Double.isNaN(currentValue) ? 0 : currentValue));
    }

}
