package com.bicycle.core.bar.downloader;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.util.Dates;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BarDataVerifier {

    private final BarRepository barRepository;

    public List<Bar> verify(List<Bar> bars) throws InvalidDataException {
        verifyPersistentData(bars);
        return verifyDownloadedData(bars);
    }

    private void verifyPersistentData(List<Bar> bars) throws InvalidDataException {
        if(bars.isEmpty()) return;
        final Bar firstBar = bars.getFirst();
        final List<Bar> persistentBars = barRepository.findBySymbolAndTimeframe(firstBar.symbol(), firstBar.timeframe(), 1);
        if(persistentBars.isEmpty()) return;
        verify(persistentBars.getLast(), firstBar);
    }

    private List<Bar> verifyDownloadedData(List<Bar> bars) throws InvalidDataException {
        if(bars.size() <= 1) return bars;
        for(int index = bars.size() - 1; index > 0; index--){
            final Bar currentBar = bars.get(index);
            final Bar previousBar = bars.get(index - 1);
            final float previousBarClose = previousBar.close();
            final float currentBarOpen = currentBar.open();
            if(hasGap(previousBarClose, currentBarOpen)){
                final Symbol symbol = currentBar.symbol();
                final Timeframe timeframe = currentBar.timeframe();
                if(0 == barRepository.countBySymbolAndTimeframe(symbol, timeframe)){
                    return bars.subList(index, bars.size());
                } else {
                    throw InvalidDataException.builder()
                            .symbol(currentBar.symbol())
                            .timeframe(currentBar.timeframe())
                            .timestamp(Dates.toLocalDateTime(currentBar.date()))
                            .closeValue(previousBarClose)
                            .openValue(currentBarOpen)
                            .build();
                }
            }
        }
        return bars;
    }

    private void verify(Bar previousBar, Bar currentBar) throws InvalidDataException {
        final float previousBarClose = previousBar.close();
        final float currentBarOpen = currentBar.open();
        if(hasGap(previousBarClose, currentBarOpen)){
            throw InvalidDataException.builder()
                    .symbol(currentBar.symbol())
                    .timeframe(currentBar.timeframe())
                    .timestamp(Dates.toLocalDateTime(currentBar.date()))
                    .closeValue(previousBarClose)
                    .openValue(currentBarOpen)
                    .build();
        }
    }

    private boolean hasGap(float previousBarClose, float currentBarOpen){
        return previousBarClose > (currentBarOpen * 1.4) || previousBarClose < (currentBarOpen * 0.6);
    }


}
