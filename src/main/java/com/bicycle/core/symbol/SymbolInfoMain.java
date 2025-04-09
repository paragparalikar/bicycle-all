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
import com.bicycle.core.symbol.repository.SymbolRepository;
import com.bicycle.util.Dates;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SymbolInfoMain {

    public static void main(String[] args) throws Exception {
        final int barCount = 14;
        final Exchange exchange = Exchange.NSE;
        final Timeframe timeframe = Timeframe.D;
        final long from = Dates.toEpochMillis(LocalDateTime.now().minusYears(1));
        final long to = Dates.toEpochMillis(LocalDateTime.now());

        final SymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider();
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);
        final Collection<Symbol> symbols = symbolRepository.findByExchange(exchange);
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

        int counter = 0;
        final Bar bar = new Bar();
        final Set<Integer> symbolCache = symbols.stream().map(Symbol::token).collect(Collectors.toSet());
        try(Cursor<Bar> cursor = barRepository.get(exchange, timeframe, from, to)){
            long previousBarDate = 0;
            for(int index = 0; index < cursor.size(); index++) {
                cursor.advance(bar);
                final Symbol symbol = bar.symbol();
                if(null != symbol && symbolCache.contains(symbol.token())) {
                    indicatorCache.onBar(bar);
                    putValue(symbol, timeframe, efficiencyValues, efficiencyIndicator);
                    putValue(symbol, timeframe, standardDeviationValues, standardDeviationIndicator);
                    putValue(symbol, timeframe, volumeValues, volumeIndicator);
                    putValue(symbol, timeframe, turnoverValues, turnoverIndicator);
                    putValue(symbol, timeframe, spreadValues, spreadIndicator);
                    if(previousBarDate != bar.date()) {
                        if(0 != previousBarDate) {

                            counter++;
                        }
                        previousBarDate = bar.date();
                    }
                }
            }
        }
        final Map<Symbol, Map.Entry<Integer, Level>> efficiencies = rank(efficiencyValues);
        final Map<Symbol, Map.Entry<Integer, Level>> stdDevs = rank(standardDeviationValues);
        final Map<Symbol, Map.Entry<Integer, Level>> volumes = rank(volumeValues);
        final Map<Symbol, Map.Entry<Integer, Level>> turnovers = rank(turnoverValues);
        final Map<Symbol, Map.Entry<Integer, Level>> spreads = rank(spreadValues);

        final List<SymbolInfo> symbolInfos = new ArrayList<>(symbols.size());


    }

    private static Map<Symbol, Map.Entry<Integer, Level>> rank(Map<Symbol, Double> map){
        final List<Symbol> symbols = map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
        final Map<Symbol, Map.Entry<Integer, Level>> results = new HashMap<>();
        for(int index = 1; index <= symbols.size(); index++){
            final Level level = index <= symbols.size() / 3 ? Level.LOW :
                    (index >= symbols.size() * 2 / 3 ? Level.HIGH : Level.MEDIUM);
            results.put(symbols.get(index), new AbstractMap.SimpleEntry<>(index, level));
        }
        return results;
    }

    private static void putValue(Symbol symbol, Timeframe timeframe, Map<Symbol, Double> map, Indicator indicator){
        final double lastValue = map.getOrDefault(symbol, 0d);
        final double currentValue = indicator.getValue(symbol, timeframe);
        map.put(symbol, lastValue + currentValue);
    }

}
