package com.bicycle.backtest.workflow;

import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;

import java.util.Comparator;

public class Test {

    public static void main(String[] args) {
        final KiteSymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider();
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);

        symbolRepository.findComponents(Symbol.NIFTY_MID_SELECT)
                .stream().sorted(Comparator.nullsFirst(Comparator.comparing(Symbol::code)))
                .forEach(System.out::println);

    }

}
