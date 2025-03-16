package com.bicycle.backtest.workflow.job.data;

import com.bicycle.client.kite.adapter.KiteBrokerClientFactory;
import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.dataSource.BarDataSource;
import com.bicycle.core.bar.dataSource.FileSystemBarDataSource;
import com.bicycle.core.bar.provider.BarDataProvider;
import com.bicycle.core.bar.provider.LoadBalancedBarDataProvider;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.bar.repository.FileSystemBarRepository;
import com.bicycle.core.portfolio.FileSystemPortfolioRepository;
import com.bicycle.core.portfolio.PortfolioRepository;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.provider.SymbolDataProvider;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;
import com.bicycle.util.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class JobMain {

    final SymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider();
    final KiteBrokerClientFactory brokerClientFactory = new KiteBrokerClientFactory();
    final PortfolioRepository portfolioRepository = new FileSystemPortfolioRepository();
    final BarDataProvider barDataProvider = new LoadBalancedBarDataProvider(portfolioRepository, brokerClientFactory);
    final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
    final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);
    final BarDataSource barDataSource = new FileSystemBarDataSource(symbolRepository);

    final BarDownloadJob barDownloadJob = new BarDownloadJob(barRepository, barDataProvider);
    final BarCleanupJob barCleanupJob = new BarCleanupJob(barRepository);
    final BarTransposeJob barTransposeJob = new BarTransposeJob(barDataSource);


    public static void main(String[] args) throws InterruptedException {
        new JobMain().run(Exchange.NSE, Timeframe.D);
    }

    private void run(Exchange exchange, Timeframe timeframe) throws InterruptedException {
        final Map<Long, List<Bar>> cache = new ConcurrentHashMap<>();
        final Collection<Symbol> symbols = symbolRepository.findByExchange(exchange);

        final ThreadFactory threadFactory = new NamedThreadFactory("job-thread-", true);
        try (ExecutorService executorService = Executors.newFixedThreadPool(20, threadFactory)) {
            for (Symbol symbol : symbols) {
                executorService.execute(() -> {
                    try{
                        barDownloadJob.downloadBars(symbol, timeframe);
                        final List<Bar> cleanBars = barCleanupJob.cleanup(symbol, timeframe);
                        cleanBars.forEach(bar -> {
                            cache.computeIfAbsent(bar.date(), key -> Collections.synchronizedList(new ArrayList<>()))
                                    .add(bar);
                        });
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
        }
        barTransposeJob.transpose(exchange, timeframe, cache);
    }
}
