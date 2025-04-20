package com.bicycle.core.bar.downloader;

import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.client.kite.utils.Constant;
import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Cursor;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.bar.repository.FileSystemBarRepository;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.provider.FilteredSymbolDataProvider;
import com.bicycle.core.symbol.provider.SymbolDataProvider;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Predicate;

public class BarDataConverterMain {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    public static void main(String[] args) throws Exception {
        final SymbolRepository symbolRepository = getSymbolRepository();
        final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);

        final Bar bar = new Bar();
        for(Symbol symbol : symbolRepository.findByExchange(Exchange.NSE)){
            try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(Constant.HOME, "data", symbol.code() + ".csv"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                Cursor<Bar> cursor = barRepository.get(symbol, Timeframe.D)){
                for(int index = 0; index < cursor.size(); index++){
                    cursor.advance(bar);
                    writer.write(toString(bar));
                    writer.newLine();
                }
            }
            System.out.println(symbol.code());
        }
    }

    private static SymbolRepository getSymbolRepository() {
        final KiteSymbolDataProvider kiteSymbolDataProvider = new KiteSymbolDataProvider();
        final Predicate<Symbol> symbolPredicate = kiteSymbolDataProvider.equitiesAndIndices()
                .and(kiteSymbolDataProvider.indices());
        final SymbolDataProvider symbolDataProvider = new FilteredSymbolDataProvider(kiteSymbolDataProvider, symbolPredicate);
        return new CacheSymbolRepository(symbolDataProvider);
    }

    private static String toString(Bar bar){
        return String.join(",", bar.symbol().code(), DATE_FORMAT.format(new Date(bar.date())),
                String.valueOf(bar.open()), String.valueOf(bar.high()), String.valueOf(bar.low()), String.valueOf(bar.close()),
                String.valueOf(bar.volume()));
    }

}
