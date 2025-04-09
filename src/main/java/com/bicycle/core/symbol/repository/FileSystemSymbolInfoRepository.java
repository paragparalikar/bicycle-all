package com.bicycle.core.symbol.repository;

import com.bicycle.core.symbol.SymbolInfo;
import com.bicycle.util.Constant;
import com.bicycle.util.Strings;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSystemSymbolInfoRepository implements SymbolInfoRepository {

    private final Map<Integer, SymbolInfo> cache = new HashMap<>();
    private final Path path = Paths.get(Constant.HOME, "symbols", "symbol-infos.csv");

    private void load() throws IOException {
        if(cache.isEmpty() && Files.exists(path)){
            Files.lines(path).map(SymbolInfo::parse).forEach(info -> cache.put(info.token(), info));
        }
    }

    @Override
    @SneakyThrows
    public void saveAll(Collection<SymbolInfo> infos){
        Files.createDirectories(path.getParent());
        final List<String> lines = infos.stream().map(SymbolInfo::toCSV).filter(Strings::hasText).toList();
        Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        cache.clear();
    }

    @Override
    @SneakyThrows
    public SymbolInfo findByToken(int token){
        load();
        return cache.get(token);
    }

}
