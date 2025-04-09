package com.bicycle.core.symbol;


public record SymbolAspect(int rank, Level level) {

    public static SymbolAspect parse(String csv){
        final String[] tokens = csv.split(",");
        return new SymbolAspect(Integer.parseInt(tokens[0]), Level.valueOf(tokens[1]));
    }


    public String toCSV(){
        return String.join(",", String.valueOf(rank), level.name());
    }



}
