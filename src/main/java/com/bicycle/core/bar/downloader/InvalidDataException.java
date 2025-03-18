package com.bicycle.core.bar.downloader;

import com.bicycle.util.Constant;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Symbol;
import lombok.*;

import java.time.LocalDateTime;

@Value
@Builder
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InvalidDataException extends Exception {

    Symbol symbol;
    Timeframe timeframe;
    LocalDateTime timestamp;
    float closeValue, openValue;

    @Override
    public String getMessage() {
        return String.format("Invalid data : %-15s %-5s %s, Gap : %f and %f", symbol.code(), timeframe.name(),
                Constant.DATE_TIME_FORMATTER.format(timestamp), closeValue, openValue);
    }
}
