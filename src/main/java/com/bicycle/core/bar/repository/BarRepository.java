package com.bicycle.core.bar.repository;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Cursor;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;

import java.util.List;

public interface BarRepository {

    long getEndDate(Symbol symbol, Timeframe timeframe);

    Cursor<Bar> get(Symbol symbol, Timeframe timeframe);

    Cursor<Bar> get(Symbol symbol, Timeframe timeframe, int limit);

    Cursor<Bar> get(Exchange exchange, Timeframe timeframe);

    Cursor<Bar> get(Exchange exchange, Timeframe timeframe, long fromInclusive, long toInclusive);
    
    void persist(Symbol symbol, Timeframe timeframe, List<Bar> bars);

    int count(Symbol symbol, Timeframe timeframe);

    void delete(Symbol symbol, Timeframe timeframe);

}
