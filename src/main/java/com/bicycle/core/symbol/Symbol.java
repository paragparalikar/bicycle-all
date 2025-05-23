package com.bicycle.core.symbol;

import lombok.Builder;
import lombok.With;

@Builder
public record Symbol(
        Exchange exchange, 
        int token,          // Exchange token provided by both kite and shoonya
        @With String code,  // Used by Kite, NSE, Bicycle
        @With String name,  // Used by Shoonya
        String type, 
        String segment,
        float tickSize, 
        int lotSize) {

    public static final String INDIA_VIX = "INDIA VIX";
    public static final String NIFTY_50 = "NIFTY 50";
    public static final String NIFTY_BANK = "NIFTY BANK";
    public static final String NIFTY_NEXT_50 = "NIFTY NEXT 50";
    public static final String NIFTY_MID_SELECT = "NIFTY MID SELECT";
    public static final String NIFTY_FIN_SERVICE = "NIFTY FIN SERVICE";


    @Override
    public int hashCode() {
        return token;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Symbol other = (Symbol) obj;
        return exchange == other.exchange && token == other.token;
    }
    
}
