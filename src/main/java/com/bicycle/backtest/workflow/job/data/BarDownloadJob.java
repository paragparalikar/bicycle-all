package com.bicycle.backtest.workflow.job.data;

import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.provider.BarDataProvider;
import com.bicycle.core.bar.provider.query.BarQuery;
import com.bicycle.core.bar.provider.query.BarQueryTransformer;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.util.Dates;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Builder
@RequiredArgsConstructor
class BarDownloadJob {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    private final BarRepository barRepository;
    private final BarDataProvider barDataProvider;
    private final BarQueryTransformer transformer = new BarQueryTransformer();

    public void downloadBars(Symbol symbol, Timeframe timeframe) {
        final ZonedDateTime lastDownloadDate = Dates.toZonedDateTime(barRepository.getLastDownloadDate(symbol, timeframe));
        final ZonedDateTime fromDate = lastDownloadDate.plusMinutes(timeframe.getMinuteMultiple());
        final ZonedDateTime toDate = ZonedDateTime.now();

        final BarQuery barQuery = BarQuery.builder()
                .symbol(symbol)
                .timeframe(timeframe)
                .from(fromDate)
                .to(toDate)
                .build();

        transformer.transform(barQuery)
                .map(barDataProvider::get)
                .ifPresentOrElse(bars -> {
                    barRepository.append(symbol, timeframe, bars);
                    log.info("{} {} {} {}", FORMATTER.format(fromDate),
                            String.format("%-5s", timeframe.name()),
                            String.format("%-15s", symbol.code()),
                            bars.size());
                }, () -> {
                    log.info("Nothing to download - {} - {}", symbol.code(), timeframe.name());
                });
    }
}
