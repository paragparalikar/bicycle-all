package com.bicycle.client.yahoo.adapter.mapper;

import com.bicycle.core.symbol.Symbol;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class YahooSymbolMapper {

    public String toYahooSymbol(Symbol symbol) {
        return symbol.code();
    }
    
}
