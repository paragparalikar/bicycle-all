package com.bicycle.core.bar.downloader;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.provider.BarDataProvider;
import com.bicycle.core.bar.provider.query.BarQuery;
import com.bicycle.core.bar.provider.query.BarQueryTransformer;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.bar.repository.FileSystemBarRepository;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.repository.SymbolRepository;
import com.bicycle.util.Constant;
import lombok.Builder;
import lombok.SneakyThrows;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

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
        for(Exchange exchange : exchanges){
            for (Timeframe timeframe : timeframes) {
                symbolRepository.findByExchange(exchange).parallelStream()
                        .forEach(symbol -> download(symbol, timeframe, 0));
                if(barRepository instanceof FileSystemBarRepository fileSystemBarRepository){
                    fileSystemBarRepository.transpose(exchange, timeframe);
                }
            }
        }
    }
    
    private void download(Symbol symbol, Timeframe timeframe, int attempt) {
        if(attempt >= MAX_RETRY_COUNT) {
            System.out.printf("%-20s %8s Failed\n", symbol.code(), timeframe.name());
            return;
        }
        try {
            final long lastDownloadDate = barRepository.getEndDate(symbol, timeframe);
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
            final List<Bar> verifiedBars = barDataVerifier.verify(bars);
            barRepository.persist(barQuery.symbol(), barQuery.timeframe(), verifiedBars);
            System.out.printf("%-20s %8d Downloaded\n", barQuery.symbol().code(), verifiedBars.size());
        } catch (InvalidDataException ide) {
            System.out.printf("%-20s %8s Gap on %s %f %f \n", barQuery.symbol().code(), barQuery.timeframe().name(),
                    Constant.DATE_TIME_FORMATTER.format(ide.getTimestamp()), ide.getCloseValue(), ide.getOpenValue());
            barRepository.delete(barQuery.symbol(), barQuery.timeframe());
            download(barQuery.symbol(), barQuery.timeframe(), ++attempt);
        }
    }

}
