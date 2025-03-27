package com.bicycle.core.bar.downloader;

import com.bicycle.client.kite.adapter.KiteBrokerClientFactory;
import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.client.yahoo.adapter.YahooBarDataProvider;
import com.bicycle.client.yahoo.adapter.YahooSymbolDataProvider;
import com.bicycle.core.bar.Timeframe;
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

import java.util.List;
import java.util.function.Predicate;

public class BarDataDownloaderMain {

    public static void main(String[] args) {
        downloadYahooData();
        downloadKiteData();
    }

    private static void downloadYahooData(){
        final BarDataProvider barDataProvider = new YahooBarDataProvider();
        final SymbolDataProvider symbolDataProvider = new YahooSymbolDataProvider();
        final BarDataDownloader barDataDownloader = createBarDataDownloader(barDataProvider, symbolDataProvider);
        barDataDownloader.download(List.of(Exchange.CDS, Exchange.MCX, Exchange.SNP500, Exchange.NASDAQ, Exchange.DOWJONES,
                Exchange.FTSE, Exchange.NIKKEI, Exchange.HANGSENG, Exchange.SSE, Exchange.DAX, Exchange.CAC), List.of(Timeframe.D));

    }

    private static void downloadKiteData(){
        final KiteBrokerClientFactory brokerClientFactory = new KiteBrokerClientFactory();
        final PortfolioRepository portfolioRepository = new FileSystemPortfolioRepository();
        final BarDataProvider barDataProvider = new LoadBalancedBarDataProvider(portfolioRepository, brokerClientFactory);
        final SymbolDataProvider symbolDataProvider = createSymbolDataProvider();
        final BarDataDownloader barDataDownloader = createBarDataDownloader(barDataProvider, symbolDataProvider);
        barDataDownloader.download(List.of(Exchange.NSE), List.of(Timeframe.D));
    }

    private static SymbolDataProvider createSymbolDataProvider() {
        final KiteSymbolDataProvider kiteSymbolDataProvider = new KiteSymbolDataProvider();
        final Predicate<Symbol> symbolPredicate = kiteSymbolDataProvider.equitiesAndIndices();
        return new FilteredSymbolDataProvider(kiteSymbolDataProvider, symbolPredicate);
    }

    private static BarDataDownloader createBarDataDownloader(BarDataProvider barDataProvider, SymbolDataProvider symbolDataProvider) {
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);
        return new BarDataDownloader(symbolRepository, barRepository, barDataProvider);
    }

}
