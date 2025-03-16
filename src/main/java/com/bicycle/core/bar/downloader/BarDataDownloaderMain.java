package com.bicycle.core.bar.downloader;

import com.bicycle.client.kite.adapter.KiteBrokerClientFactory;
import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.provider.BarDataProvider;
import com.bicycle.core.bar.provider.LoadBalancedBarDataProvider;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.bar.repository.FileSystemBarRepository;
import com.bicycle.core.portfolio.FileSystemPortfolioRepository;
import com.bicycle.core.portfolio.PortfolioRepository;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.provider.SymbolDataProvider;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;

import java.util.Collection;
import java.util.List;

public class BarDataDownloaderMain {

    public static void main(String[] args) {
        final SymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider().equitiesOnly();
        final KiteBrokerClientFactory brokerClientFactory = new KiteBrokerClientFactory();
        final BarDataDownloader barDataDownloader = createBarDataDownloader(brokerClientFactory, symbolDataProvider);

        final Collection<Exchange> exchanges = List.of(Exchange.NSE);
        final Collection<Timeframe> timeframes = List.of(Timeframe.D);
        barDataDownloader.download(exchanges, timeframes);
    }

    private static BarDataDownloader createBarDataDownloader(KiteBrokerClientFactory brokerClientFactory, SymbolDataProvider symbolDataProvider) {
        final PortfolioRepository portfolioRepository = new FileSystemPortfolioRepository();
        final BarDataProvider barDataProvider = new LoadBalancedBarDataProvider(portfolioRepository, brokerClientFactory);
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);
        return new BarDataDownloader(symbolRepository, barRepository, barDataProvider);
    }

}
