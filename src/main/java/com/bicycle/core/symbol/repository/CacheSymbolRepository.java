package com.bicycle.core.symbol.repository;

import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.provider.SymbolDataProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class CacheSymbolRepository implements SymbolRepository {
    
    private final SymbolDataProvider symbolDataProvider;
    private final Map<Exchange, Map<String, Symbol>> exchangeCodeSymbolCache = new HashMap<>();
    private final Map<Exchange, Int2ObjectOpenHashMap<Symbol>> exchangeTokenSymbolCache = new EnumMap<>(Exchange.class);

    private boolean isEmpty(Exchange exchange) {
        return exchangeCodeSymbolCache.isEmpty()
                || null == exchangeTokenSymbolCache.get(exchange)
                || exchangeTokenSymbolCache.get(exchange).isEmpty();
    }
    
    private void loadIfEmpty(Exchange exchange) {
        if(isEmpty(exchange)) { synchronized(exchangeTokenSymbolCache) { if(isEmpty(exchange)) {
            for(Symbol symbol : symbolDataProvider.findByExchange(exchange)) {
                exchangeCodeSymbolCache.computeIfAbsent(exchange, 
                        key -> new HashMap<>()).put(symbol.code(), symbol);
                exchangeTokenSymbolCache.computeIfAbsent(exchange,
                        key -> new Int2ObjectOpenHashMap<>(2048))
                                .put(symbol.token(), symbol);
            }
        } } }
    }
    
    @Override
    public Collection<Symbol> findByExchange(Exchange exchange) {
        loadIfEmpty(exchange);
        final Int2ObjectOpenHashMap<Symbol> tokenSymbolCache = exchangeTokenSymbolCache.get(exchange);
        return null == tokenSymbolCache ? Collections.emptyList() : tokenSymbolCache.values();
    }
    
    @Override
    public Symbol findByToken(int token, Exchange exchange) {
        loadIfEmpty(exchange);
        final Int2ObjectOpenHashMap<Symbol> tokenSymbolCache = exchangeTokenSymbolCache.get(exchange);
        return null == tokenSymbolCache ? null : tokenSymbolCache.get(token);
    }

    @Override
    public Symbol findByCode(String code, Exchange exchange) {
        loadIfEmpty(exchange);
        return exchangeCodeSymbolCache.getOrDefault(exchange, 
                Collections.emptyMap()).get(code);
    }
    
}
