package com.bicycle.core.bar.downloader;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.util.Dates;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BarDataVerifier {

    private final BarRepository barRepository;

    public void verify(List<Bar> bars) throws InvalidDataException {
        verifyDownloadedData(bars);
        verifyPersistentData(bars);
    }

    private void verifyPersistentData(List<Bar> bars) throws InvalidDataException {
        if(bars.isEmpty()) return;
        final Bar firstBar = bars.getFirst();
        final List<Bar> persistentBars = barRepository.findBySymbolAndTimeframe(firstBar.symbol(), firstBar.timeframe(), 1);
        if(persistentBars.isEmpty()) return;
        verify(persistentBars.getLast(), firstBar);
    }

    private void verifyDownloadedData(List<Bar> bars) throws InvalidDataException {
        if(bars.size() <= 1) return;
        for(int index = bars.size() - 1; index > 0; index--){
            verify(bars.get(index - 1), bars.get(index));
        }
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
