package com.bicycle.core.bar.downloader;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.provider.BarDataProvider;
import com.bicycle.core.bar.provider.query.BarQuery;
import com.bicycle.core.bar.provider.query.BarQueryTransformer;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.bar.repository.InvalidDataException;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.repository.SymbolRepository;
import com.bicycle.util.Dates;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Builder
@RequiredArgsConstructor
public class BarDataDownloader {
    private static final int MAX_RETRY_COUNT = 3;
    
    private final BarRepository barRepository;
    private final BarDataProvider barDataProvider;
    private final SymbolRepository symbolRepository;
    private final BarQueryTransformer queryTransformer;
    
    
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
            verifyDownloadedData(bars);
            verifyPersistentData(bars);
            System.out.printf("%-20s\t%8d\n", barQuery.symbol().code(), bars.size());
        } catch (InvalidDataException ide) {
            System.out.println(ide.getMessage());
            download(barQuery.symbol(), barQuery.timeframe(), ++attempt);
        }
    }

    private void verifyPersistentData(List<Bar> bars) throws InvalidDataException {
        if(bars.isEmpty()) return;
        final Bar firstBar = bars.getFirst();
        final List<Bar> persistentBars = barRepository.findBySymbolAndTimeframe(firstBar.symbol(), firstBar.timeframe(), 1);
        if(persistentBars.isEmpty()) return;
        verify(persistentBars.getLast(), firstBar);
    }

    private void verifyDownloadedData(List<Bar> bars) throws InvalidDataException {
        if(bars.size() <= 1) return;
        for(int index = bars.size() - 1; index > 0; index--){
            verify(bars.get(index - 1), bars.get(index));
        }
    }

    private void verify(Bar previousBar, Bar currentBar) throws InvalidDataException {
        final float previousBarClose = previousBar.close();
        final float currentBarOpen = currentBar.open();
        if(hasGap(previousBarClose, currentBarOpen)){
            throw InvalidDataException.builder()
                    .symbol(currentBar.symbol())
                    .timeframe(currentBar.timeframe())
                    .timestamp(Dates.toLocalDateTime(currentBar.date()))
                    .closeValue(previousBarClose)
                    .openValue(currentBarOpen)
                    .build();
        }
    }

    private boolean hasGap(float previousBarClose, float currentBarOpen){
        return previousBarClose > (currentBarOpen * 1.2) || previousBarClose < (currentBarOpen * 0.8);
    }
    
}
