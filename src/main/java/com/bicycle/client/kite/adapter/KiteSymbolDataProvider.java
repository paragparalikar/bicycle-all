package com.bicycle.client.kite.adapter;

import com.bicycle.client.kite.adapter.mapper.KiteMapper;
import com.bicycle.client.kite.api.model.KiteExchange;
import com.bicycle.client.kite.api.symbol.KiteFileSystemSymbolDataLoader;
import com.bicycle.client.kite.api.symbol.KiteSymbolDataLoader;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.provider.FilteredSymbolDataProvider;
import com.bicycle.core.symbol.provider.SymbolDataProvider;
import com.bicycle.util.Strings;
import java.util.Collection;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class KiteSymbolDataProvider implements SymbolDataProvider {
    
    private final KiteSymbolDataLoader kiteSymbolDataLoader;
    
    public KiteSymbolDataProvider() {
        this(new KiteFileSystemSymbolDataLoader());
    }

    @Override
    @SneakyThrows
    public Collection<Symbol> findByExchange(Exchange exchange) {
        final KiteMapper kiteMapper = KiteMapper.INSTANCE;
        final KiteExchange kiteExchange = kiteMapper.toKiteExchange(exchange);
        return kiteSymbolDataLoader.loadByKiteExchange(kiteExchange).stream()
                .map(kiteMapper::toSymbol)
                .toList();
    }



    public Predicate<Symbol> excludeEtf(){
        return symbol -> !symbol.code().endsWith("BEES")
                && !symbol.code().contains("ETF")
                && !symbol.name().contains("ETF");
    }

    public Predicate<Symbol> excludeGoldBonds(){
        return symbol -> !symbol.code().endsWith("-GB");
    }

    public Predicate<Symbol> excludeTreasuryBills(){
        return symbol -> !symbol.code().equals("-TB");
    }

    public Predicate<Symbol> indices(){
        return symbol -> "INDICES".equalsIgnoreCase(symbol.segment());
    }

    public Predicate<Symbol> equities(){
        return symbol -> "EQ".equals(symbol.type());
    }

    public Predicate<Symbol> equitiesAndIndices(){
        return symbol -> !symbol.code().contains("BEES")
                && !symbol.name().contains("BEES")
                && !symbol.code().contains("ETF")
                && !symbol.name().contains("ETF")
                && !symbol.code().contains("-")
                && !symbol.name().contains("-");
    }

}
