package com.bicycle.core.symbol;

import lombok.Builder;

@Builder
public record SymbolInfo(
        int token,
        int efficiencyRank, Level efficiencyLevel,
        int volatilityRank, Level volatiliyLevel,
        int spreadRank, Level spreadLevel,
        int volumeRank, Level volumeLevel,
        int turnoverRank, Level turnoverLevel
) {

    public static SymbolInfo parse(String csv){
        final String[] tokens = csv.split(",");
        return SymbolInfo.builder()
                .token(Integer.parseInt(tokens[0]))
                .efficiencyRank(Integer.parseInt(tokens[1]))
                .efficiencyLevel(Level.valueOf(tokens[2]))
                .volatilityRank(Integer.parseInt(tokens[3]))
                .volatiliyLevel(Level.valueOf(tokens[4]))
                .spreadRank(Integer.parseInt(tokens[5]))
                .spreadLevel(Level.valueOf(tokens[6]))
                .volumeRank(Integer.parseInt(tokens[7]))
                .volumeLevel(Level.valueOf(tokens[8]))
                .turnoverRank(Integer.parseInt(tokens[9]))
                .turnoverLevel(Level.valueOf(tokens[10]))
                .build();
    }

    public String toCSV(){
        return String.join(",",
                String.valueOf(token),
                String.valueOf(efficiencyRank), efficiencyLevel.name(),
                String.valueOf(volatilityRank), volatiliyLevel.name(),
                String.valueOf(spreadRank), spreadLevel.name(),
                String.valueOf(volumeRank), volumeLevel.name(),
                String.valueOf(turnoverRank), turnoverLevel.name());
    }

}
