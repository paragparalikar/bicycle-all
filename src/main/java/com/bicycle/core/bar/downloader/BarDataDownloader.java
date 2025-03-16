package com.bicycle.core.bar.downloader;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.provider.BarDataProvider;
import com.bicycle.core.bar.provider.query.BarQuery;
import com.bicycle.core.bar.provider.query.BarQueryTransformer;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.repository.SymbolRepository;
import lombok.Builder;
import lombok.SneakyThrows;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarDataDownloader {
    private static final int MAX_RETRY_COUNT = 3;
    
    private final BarRepository barRepository;
    private final BarDataVerifier barDataVerifier;
    private final BarDataProvider barDataProvider;
    private final SymbolRepository symbolRepository;
    private final BarQueryTransformer queryTransformer;

    @Builder
    public BarDataDownloader(
            SymbolRepository symbolRepository,
            BarRepository barRepository,
            BarDataProvider barDataProvider){
        this.barRepository = barRepository;
        this.symbolRepository = symbolRepository;
        this.barDataProvider = barDataProvider;
        this.queryTransformer = new BarQueryTransformer();
        this.barDataVerifier = new BarDataVerifier(barRepository);
    }

    @SneakyThrows
    public void download(Collection<Exchange> exchanges, Collection<Timeframe> timeframes) {
        try (ExecutorService executorService = Executors.newFixedThreadPool(20)) {
            for(Exchange exchange : exchanges){
                for (Symbol symbol : symbolRepository.findByExchange(exchange)) {
                    for (Timeframe timeframe : timeframes) {
                        executorService.execute(() -> download(symbol, timeframe, 0));
                    }
                }
            }
        }
    }
    
    private void download(Symbol symbol, Timeframe timeframe, int attempt) {
        if(attempt >= MAX_RETRY_COUNT) {
            System.err.printf("Maximum retry count of %d exceeded for %s %s\n",
                    MAX_RETRY_COUNT, symbol.code(), timeframe.name());
            return;
        }
        try {
            final long lastDownloadDate = barRepository.getLastDownloadDate(symbol, timeframe);
            final ZonedDateTime from = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastDownloadDate),
                    ZoneId.systemDefault()).plusMinutes(timeframe.getMinuteMultiple());
            final ZonedDateTime to = ZonedDateTime.now();
            queryTransformer.transform(BarQuery.builder()
                            .symbol(symbol).timeframe(timeframe)
                            .to(to).from(from).build())
                    .ifPresent(barQuery -> download(barQuery, attempt));
        } catch(Exception e) {
            System.err.println(symbol.code() + "," + 0);
        }
    }
    
    private void download(BarQuery barQuery, int attempt) {
        try{
            final List<Bar> bars = barDataProvider.get(barQuery);
            barDataVerifier.verify(bars);
            barRepository.append(barQuery.symbol(), barQuery.timeframe(), bars);
            System.out.printf("Downloaded %8d bars for %-20s\n", bars.size(), barQuery.symbol().code());
        } catch (InvalidDataException ide) {
            System.out.println(ide.getMessage());
            barRepository.deleteAll(barQuery.symbol(), barQuery.timeframe());
            download(barQuery.symbol(), barQuery.timeframe(), ++attempt);
        }
    }

}
