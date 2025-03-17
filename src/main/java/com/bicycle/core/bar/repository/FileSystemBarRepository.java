package com.bicycle.core.bar.repository;

import com.bicycle.core.symbol.repository.SymbolRepository;
import lombok.experimental.Delegate;

public class FileSystemBarRepository implements BarRepository {

    @Delegate private final FileSystemDateIndexedBarRepository fileSystemDateIndexedBarRepository;
    @Delegate private final FileSystemSymbolIndexedBarRepository fileSystemSymbolIndexedBarRepository;

    public FileSystemBarRepository(SymbolRepository symbolRepository){
        this.fileSystemSymbolIndexedBarRepository =new FileSystemSymbolIndexedBarRepository();
        this.fileSystemDateIndexedBarRepository = new FileSystemDateIndexedBarRepository(symbolRepository);
    }

}
