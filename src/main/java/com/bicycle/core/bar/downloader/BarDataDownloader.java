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
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Builder
@RequiredArgsConstructor
public class BarDataDownloader {
    
    private final BarRepository barRepository;
    private final BarDataProvider barDataProvider;
    private final SymbolRepository symbolRepository;
    private final BarQueryTransformer queryTransformer;
    
    
    @SneakyThrows
    public void download() {
        try (ExecutorService executorService = Executors.newFixedThreadPool(20)) {
            for (Symbol symbol : symbolRepository.findByExchange(Exchange.NSE)) {
                for (Timeframe timeframe : List.of(Timeframe.D)) { // TODO all timeframes
                    executorService.execute(() -> download(symbol, timeframe));
                }
            }
            executorService.shutdown();
            if(!executorService.awaitTermination(5, TimeUnit.HOURS)){
                System.out.println("Wait time of 5 hours elapsed before the process could finish");
            }
        }
    }
    
    private void download(Symbol symbol, Timeframe timeframe) {
        try {
            final long ldd = barRepository.getLastDownloadDate(symbol, timeframe);
            final ZonedDateTime from = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ldd), 
                    ZoneId.systemDefault()).plusMinutes(timeframe.getMinuteMultiple());
            final ZonedDateTime to = ZonedDateTime.now();
            queryTransformer.transform(BarQuery.builder()
                    .symbol(symbol).timeframe(timeframe)
                    .to(to).from(from).build())
            .ifPresent(this::download);
        } catch(Exception e) {
            System.err.println(symbol.code() + "," + 0);
        }
    }
    
    private void download(BarQuery barQuery) {
        final List<Bar> bars = barDataProvider.get(barQuery);
        barRepository.append(truncateBadData(bars));
        System.out.println(barQuery.symbol().code() + "," + bars.size());
    }

    private List<Bar> truncateBadData(List<Bar> bars){
        if(bars.size() > 1){
            for(int index = bars.size() - 1; index > 0; index--){
                final Bar currentBar = bars.get(index);
                final Bar previousBar = bars.get(index - 1);
                if(previousBar.close() > (currentBar.open() * 1.3)
                || previousBar.close() < (currentBar.open() * 0.7)){
                    return bars.subList(index, bars.size());
                }
            }
        }
        return bars;
    }
    
}
