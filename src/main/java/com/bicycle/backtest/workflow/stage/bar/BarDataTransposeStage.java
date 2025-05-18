package com.bicycle.backtest.workflow.stage.bar;

import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.repository.FileSystemBarRepository;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;

import java.util.Collection;

public class BarDataTransposeStage {

    public void execute(Collection<Exchange> exchanges, Collection<Timeframe> timeframes) {
        System.out.println("\n--------------- Initiating bar data transpose stage ---------------");
        final KiteSymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider();
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        final FileSystemBarRepository barRepository = new FileSystemBarRepository(symbolRepository);
        for(Exchange exchange : exchanges){
            for(Timeframe timeframe : timeframes){
                barRepository.transpose(exchange, timeframe);
            }
        }
    }

}
