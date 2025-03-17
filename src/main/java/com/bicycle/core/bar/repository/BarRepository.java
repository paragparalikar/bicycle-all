package com.bicycle.core.bar.repository;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Cursor;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface BarRepository {

    long getEndDate(Symbol symbol, Timeframe timeframe);

    long getEndDate(Exchange exchange, Timeframe timeframe);

    Cursor<Bar> get(Symbol symbol, Timeframe timeframe);

    Cursor<Bar> get(Symbol symbol, Timeframe timeframe, int limit);

    Cursor<Bar> get(Exchange exchange, Timeframe timeframe);

    Cursor<Bar> get(Exchange exchange, Timeframe timeframe, ZonedDateTime fromInclusive, ZonedDateTime toInclusive);
    
    void persist(Symbol symbol, Timeframe timeframe, List<Bar> bars);

    void persist(Exchange exchange, Timeframe timeframe, Map<Long, List<Bar>> data);

    int count(Symbol symbol, Timeframe timeframe);

    void delete(Symbol symbol, Timeframe timeframe);

}
