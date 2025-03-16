package com.bicycle.core.bar.repository;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.BarReader;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BarRepository {

    void append(Collection<Bar> bars);
    
    long getLastDownloadDate(Symbol symbol, Timeframe timeframe);
    
    void append(Symbol symbol, Timeframe timeframe, List<Bar> bars);

    void replace(Symbol symbol, Timeframe timeframe, List<Bar> bars);

    List<Bar> findBySymbolAndTimeframe(Symbol symbol, Timeframe timeframe);
    
    BarReader readBySymbolAndTimeframe(Symbol symbol, Timeframe timeframe);
    
    List<Bar> findBySymbolAndTimeframe(Symbol symbol, Timeframe timeframe, int limit);

    List<Bar> findBySymbolAndTimeframeAfter(Symbol symbol, Timeframe timeframe, long startExclusive);

    void deleteAll(Symbol symbol, Timeframe timeframe);

    // Bhavcopy related methods

    ZonedDateTime getEndDate(Exchange exchange, Timeframe timeframe);

    ZonedDateTime getStartDate(Exchange exchange, Timeframe timeframe);

    BarReader get(Exchange exchange, Timeframe timeframe);

    BarReader get(Exchange exchange, Timeframe timeframe, ZonedDateTime fromInclusive, ZonedDateTime toInclusive);

    void persist(Exchange exchange, Timeframe timeframe, Map<Long, List<Bar>> data);
    
    
}
