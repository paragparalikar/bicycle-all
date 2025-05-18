package com.bicycle.backtest.workflow;

import com.bicycle.backtest.workflow.stage.bar.BarDataDownloadStage;
import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class BarDataDownloadWorkflow {

    public static void main(String[] args) {
        //final Collection<String> symbolCodes = getComponentCodes();
        new BarDataDownloadStage().execute(
                List.of(Exchange.NSE),
                List.of(Timeframe.M10, Timeframe.M5, Timeframe.M3, Timeframe.M1),
                symbol -> Symbol.INDIA_VIX.equalsIgnoreCase(symbol.code()));
    }

    private static List<String> getComponentCodes(){
        final KiteSymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider();
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        return List.of(Symbol.NIFTY_50, Symbol.NIFTY_BANK, Symbol.NIFTY_FIN_SERVICE, Symbol.NIFTY_MID_SELECT, Symbol.NIFTY_NEXT_50).stream()
                .map(symbolRepository::findComponents)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(Symbol::code)
                .distinct().sorted().toList();
    }

}
