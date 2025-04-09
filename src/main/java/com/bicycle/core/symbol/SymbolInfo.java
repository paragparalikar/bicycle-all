package com.bicycle.core.symbol;

import com.bicycle.util.Strings;
import lombok.Builder;

@Builder
public record SymbolInfo(
        int token,
        SymbolAspect efficiency,
        SymbolAspect volatility,
        SymbolAspect spread,
        SymbolAspect volume,
        SymbolAspect turnover
) {

    public static SymbolInfo parse(String csv){
        if(!Strings.hasText(csv)) return null;
        final String[] tokens = csv.split(";");
        if(6 > tokens.length) return null;
        return SymbolInfo.builder()
                .token(Integer.parseInt(tokens[0]))
                .efficiency(SymbolAspect.parse(tokens[1]))
                .volatility(SymbolAspect.parse(tokens[2]))
                .spread(SymbolAspect.parse(tokens[3]))
                .volume(SymbolAspect.parse(tokens[4]))
                .turnover(SymbolAspect.parse(tokens[5]))
                .build();
    }

    public String toCSV(){
        if(null == efficiency || null == volatility || null == spread || null == volume || null == turnover) return null;
        return String.join(";",
                String.valueOf(token),
                efficiency.toCSV(),
                volatility.toCSV(),
                spread.toCSV(),
                volume.toCSV(),
                turnover.toCSV());
    }

}
