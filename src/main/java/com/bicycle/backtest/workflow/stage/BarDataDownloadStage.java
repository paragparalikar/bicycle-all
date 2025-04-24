package com.bicycle.backtest.workflow.stage;

import com.bicycle.client.kite.adapter.KiteBrokerClientFactory;
import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.downloader.BarDataDownloader;
import com.bicycle.core.bar.provider.BarDataProvider;
import com.bicycle.core.bar.provider.LoadBalancedBarDataProvider;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.bar.repository.FileSystemBarRepository;
import com.bicycle.core.portfolio.FileSystemPortfolioRepository;
import com.bicycle.core.portfolio.PortfolioRepository;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.provider.FilteredSymbolDataProvider;
import com.bicycle.core.symbol.provider.SymbolDataProvider;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;

import java.util.Collection;
import java.util.function.Predicate;

public class BarDataDownloadStage {

    public void execute(Collection<Exchange> exchanges, Collection<Timeframe> timeframes, Predicate<Symbol> symbolPredicate){
        final KiteBrokerClientFactory brokerClientFactory = new KiteBrokerClientFactory();
        final PortfolioRepository portfolioRepository = new FileSystemPortfolioRepository();
        final BarDataProvider barDataProvider = new LoadBalancedBarDataProvider(portfolioRepository, brokerClientFactory);
        final SymbolDataProvider symbolDataProvider = createSymbolDataProvider(symbolPredicate);
        final BarDataDownloader barDataDownloader = createBarDataDownloader(barDataProvider, symbolDataProvider);
        barDataDownloader.download(exchanges, timeframes);
    }

    private static SymbolDataProvider createSymbolDataProvider(Predicate<Symbol> symbolPredicate) {
        final KiteSymbolDataProvider kiteSymbolDataProvider = new KiteSymbolDataProvider();
        return new FilteredSymbolDataProvider(kiteSymbolDataProvider, symbolPredicate);
    }

    private static BarDataDownloader createBarDataDownloader(BarDataProvider barDataProvider, SymbolDataProvider symbolDataProvider) {
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);
        return new BarDataDownloader(symbolRepository, barRepository, barDataProvider);
    }

}
