package com.bicycle.core.symbol.repository;

import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.util.Strings;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface SymbolRepository {
    
    Symbol findByToken(int token, Exchange exchange);
    
    Symbol findByCode(String code, Exchange exchange);
    
    Collection<Symbol> findByExchange(Exchange exchange);

    default Collection<Symbol> findComponents(String indexSymbolCode){
        try(final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(indexSymbolCode);
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))){
            final Set<Symbol> symbols = new HashSet<>();
            String line = null;
            while(null != (line = bufferedReader.readLine())) {
                if(Strings.hasText(line)) {
                    symbols.add(findByCode(line, Exchange.NSE));
                }
            }
            return symbols;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    
}
