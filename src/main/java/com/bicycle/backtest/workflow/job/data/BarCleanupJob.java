package com.bicycle.backtest.workflow.job.data;

import com.bicycle.client.kite.utils.Constant;
import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.symbol.Symbol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class BarCleanupJob {

    private final BarRepository barRepository;

    public List<Bar> cleanup(Symbol symbol, Timeframe timeframe){
        log.info("Cleaning up bars for {} {}", timeframe, symbol.code());
        final List<Bar> bars = barRepository.findBySymbolAndTimeframe(symbol, timeframe);
        if (bars.size() > 1) {
            for (int index = bars.size() - 1; index > 0; index--) {
                final Bar currentBar = bars.get(index);
                final Bar previousBar = bars.get(index - 1);
                if (previousBar.close() > (currentBar.open() * 1.3)
                        || previousBar.close() < (currentBar.open() * 0.7)) {
                    final List<Bar> cleanBars = bars.subList(index, bars.size());
                    log.info("Break found in bar data. {} {} close on {} is {} wile open on {} is {}",
                            symbol.code(), timeframe,
                            Constant.DATE_FORMAT.format(new Date(previousBar.date())), Constant.CURRENCY_FORMAT.format(previousBar.close()),
                            Constant.DATE_FORMAT.format(new Date(currentBar.date())), Constant.CURRENCY_FORMAT.format(currentBar.open()));
                    barRepository.replace(symbol, timeframe, cleanBars);
                    return cleanBars;
                }
            }
        }
        return bars;
    }

}
