package com.bicycle.core.symbol.repository;

import com.bicycle.core.symbol.SymbolInfo;

import java.util.Collection;

public interface SymbolInfoRepository {
    void saveAll(Collection<SymbolInfo> infos);

    SymbolInfo findByToken(int token);
}
