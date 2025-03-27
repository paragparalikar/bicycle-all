package com.bicycle.client.yahoo.adapter;

import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.provider.SymbolDataProvider;

import java.util.Collection;
import java.util.List;

public class YahooSymbolDataProvider implements SymbolDataProvider {
    
    @Override
    public Collection<Symbol> findByExchange(Exchange exchange) {
       return switch (exchange){
           case CDS -> List.of(
                   createSymbol(0, "INR=X", exchange, "FUT", "CDS-FUT"),
                   createSymbol(1, "EURINR=X", exchange, "FUT", "CDS-FUT"),
                   createSymbol(2, "JPYINR=X", exchange, "FUT", "CDS-FUT"),
                   createSymbol(3, "CNYINR=X", exchange, "FUT", "CDS-FUT"),
                   createSymbol(4, "GBPINR=X", exchange, "FUT", "CDS-FUT"),
                   createSymbol(5, "AUDINR=X", exchange, "FUT", "CDS-FUT"),
                   createSymbol(6, "CHFINR=X", exchange, "FUT", "CDS-FUT")
           );
           case MCX -> List.of(
                   createSymbol(0, "USDINR=X", exchange, "FUT", "MCX-FUT"),
                   createSymbol(1, "EURINR=X", exchange, "FUT", "MCX-FUT"),
                   createSymbol(2, "JPYINR=X", exchange, "FUT", "MCX-FUT"),
                   createSymbol(3, "CNYINR=X", exchange, "FUT", "MCX-FUT")
           );
           case SNP500 -> List.of(
                   createSymbol(0, "^GSPC", exchange, "EQ", "INDICES")
           );
           case NASDAQ -> List.of(
                   createSymbol(0, "^IXIC", exchange, "EQ", "INDICES")
           );
           case DOWJONES -> List.of(
                   createSymbol(0, "^DJI", exchange, "EQ", "INDICES")
           );
           case FTSE -> List.of(
                   createSymbol(0, "^FTSE", exchange, "EQ", "INDICES")
           );
           case NIKKEI -> List.of(
                   createSymbol(0, "^N225", exchange, "EQ", "INDICES")
           );
           case HANGSENG -> List.of(
                   createSymbol(0, "^HSI", exchange, "EQ", "INDICES")
           );
           case SSE -> List.of(
                   createSymbol(0, "000001.SS", exchange, "EQ", "INDICES")
           );
           case DAX -> List.of(
                   createSymbol(0, "^GDAXI", exchange, "EQ", "INDICES")
           );
           case CAC -> List.of(
                   createSymbol(0, "^FCHI", exchange, "EQ", "INDICES")
           );
           default -> List.of();
       };
    }

    private Symbol createSymbol(int index, String code, Exchange exchange, String type, String segment) {
        return Symbol.builder()
                .token(index)
                .code(code)
                .name(code)
                .exchange(exchange)
                .lotSize(1)
                .tickSize(0.05f)
                .segment(segment)
                .type(type)
                .build();
    }
    
}
