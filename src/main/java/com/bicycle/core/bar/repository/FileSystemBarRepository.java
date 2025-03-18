package com.bicycle.core.bar.repository;

import com.bicycle.client.kite.utils.Constant;
import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.repository.SymbolRepository;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class FileSystemBarRepository implements BarRepository {

    private final SymbolRepository symbolRepository;
    @Delegate private final FileSystemDateIndexedBarRepository fileSystemDateIndexedBarRepository;
    @Delegate private final FileSystemSymbolIndexedBarRepository fileSystemSymbolIndexedBarRepository;

    public FileSystemBarRepository(SymbolRepository symbolRepository){
        this.symbolRepository = symbolRepository;
        this.fileSystemSymbolIndexedBarRepository =new FileSystemSymbolIndexedBarRepository();
        this.fileSystemDateIndexedBarRepository = new FileSystemDateIndexedBarRepository(symbolRepository);
    }

    public void transpose(Exchange exchange, Timeframe timeframe){
        final long endDate = fileSystemDateIndexedBarRepository.getEndDate(exchange, timeframe);
        System.out.println("Transposing data from " + Constant.DATE_FORMAT.format(new Date(endDate)));
        final Map<Long, Queue<Bar>> accumulator = new HashMap<>();
        symbolRepository.findByExchange(exchange).parallelStream().forEach(symbol -> {
            final Set<Bar> bars = fileSystemSymbolIndexedBarRepository.get(symbol, timeframe, endDate);
            bars.forEach(bar -> accumulator.computeIfAbsent(bar.date(), key -> new ConcurrentLinkedQueue<>()).add(bar));
        });
        fileSystemDateIndexedBarRepository.persist(exchange, timeframe, accumulator);
        System.out.println("Finished transposing data");
    }

}
