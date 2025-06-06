package com.bicycle.backtest.workflow.stage.bar;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Predicate;

public class BarDataAmiBrokerConverterStage {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HHmm");

    public void execute(Exchange exchange, Timeframe timeframe, Predicate<Symbol> symbolPredicate) throws Exception {
        System.out.println("\n--------------- Initiating bar data conversion (AmiBroker) stage ---------------");
        System.out.printf("Converting data for exchange %s and timeframe %s\n", exchange.name(), timeframe.name());
        final SymbolRepository symbolRepository = getSymbolRepository(symbolPredicate);
        final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);
        final Path directory = Paths.get(Constant.HOME, "data", exchange.name(), timeframe.name());
        Files.createDirectories(directory);
        final Bar bar = new Bar();
        for(Symbol symbol : symbolRepository.findByExchange(exchange)){
            try(BufferedWriter writer = Files.newBufferedWriter(directory.resolve(symbol.code() + ".csv"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                Cursor<Bar> cursor = barRepository.get(symbol, timeframe)){
                for(int index = 0; index < cursor.size(); index++){
                    cursor.advance(bar);
                    writer.write(toString(bar));
                    writer.newLine();
                }
            }
            System.out.println(symbol.code());
        }
    }


    private static SymbolRepository getSymbolRepository(Predicate<Symbol> symbolPredicate) {
        final KiteSymbolDataProvider kiteSymbolDataProvider = new KiteSymbolDataProvider();
        final SymbolDataProvider symbolDataProvider = new FilteredSymbolDataProvider(kiteSymbolDataProvider, symbolPredicate);
        return new CacheSymbolRepository(symbolDataProvider);
    }

    private static String toString(Bar bar){
        final Date date = new Date(bar.date());
        return String.join(",", bar.symbol().code(), DATE_FORMAT.format(date),
                String.valueOf(bar.open()), String.valueOf(bar.high()), String.valueOf(bar.low()), String.valueOf(bar.close()),
                String.valueOf(bar.volume()), TIME_FORMAT.format(date));
    }

}
