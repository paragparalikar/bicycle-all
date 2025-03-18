package com.bicycle.core.bar.repository;

import com.bicycle.util.Constant;
import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Cursor;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.util.FileUtils;
import lombok.SneakyThrows;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileSystemSymbolIndexedBarRepository {
    private static final int BYTES = Long.BYTES + 4 * Float.BYTES + Integer.BYTES;

    private Path getPath(Symbol symbol, Timeframe timeframe) {
        return FileUtils.createParentDirectoriesIfNotExist(Paths.get(
                Constant.HOME, "bars", symbol.exchange().name(), timeframe.name(), symbol.code()));
    }

    @SneakyThrows
    private void read(Bar bar, DataInput input) {
        bar.date(input.readLong());
        bar.open(input.readFloat());
        bar.high(input.readFloat());
        bar.low(input.readFloat());
        bar.close(input.readFloat());
        bar.volume(input.readInt());
    }
    
    @SneakyThrows
    private void write(Bar bar, DataOutput output) {
        output.writeLong(bar.date());
        output.writeFloat(bar.open());
        output.writeFloat(bar.high());
        output.writeFloat(bar.low());
        output.writeFloat(bar.close());
        output.writeInt(bar.volume());
    }

    @SneakyThrows
    public void persist(Symbol symbol, Timeframe timeframe, List<Bar> bars) {
    	if(null == bars || bars.isEmpty()) return;
        final Path path = getPath(symbol, timeframe);
        if(!Files.exists(path)) Files.createDirectories(path.getParent());
        try(RandomAccessFile file = new RandomAccessFile(path.toFile(), "rw")){
            if(BYTES > file.length() || file.readLong() >= bars.getFirst().date()) {
                file.setLength(0);
                file.seek(0);
            } else {
                file.seek(file.length());
            }
            bars.forEach(bar -> write(bar, file));
        }
    }

    @SneakyThrows
    public void delete(Symbol symbol, Timeframe timeframe){
        Files.deleteIfExists(getPath(symbol, timeframe));
    }

    @SneakyThrows
    public int count(Symbol symbol, Timeframe timeframe){
        final Path path = getPath(symbol, timeframe);
        return Files.exists(path) ? (int) (Files.size(path) / BYTES) : 0;
    }

    public Cursor<Bar> get(Symbol symbol, Timeframe timeframe){
        return get(symbol, timeframe, Integer.MAX_VALUE);
    }

    @SneakyThrows
    public Cursor<Bar> get(Symbol symbol, Timeframe timeframe, int limit) {
        final Path path = getPath(symbol, timeframe);
        if(!Files.exists(path)) return Cursor.EMPTY;
        final RandomAccessFile file = new RandomAccessFile(path.toFile(), "r");
        final int count = Math.min(limit, (int) (file.length() / BYTES));
        file.seek(Math.max(0, file.length() - count * BYTES));
        return new Cursor<>() {
            @Override public void close() throws Exception { file.close(); }
            @Override public int size() { return count; }
            @Override public void advance(Bar bar) {
                read(bar, file);
                bar.symbol(symbol);
                bar.timeframe(timeframe);
            }
        };
    }

    @SneakyThrows
    Set<Bar> get(Symbol symbol, Timeframe timeframe, long fromExclusive){
        final Path path = getPath(symbol, timeframe);
        if(!Files.exists(path)) return Collections.emptySet();
        try(RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")){
            final Set<Bar> bars = new HashSet<>();
            file.seek(file.length() - BYTES);
            long date = file.readLong();
            while(date > fromExclusive){
                 bars.add(new Bar(symbol, timeframe, date,
                         file.readFloat(), file.readFloat(), file.readFloat(),file.readFloat(),
                         file.readInt()));
                file.seek(file.getFilePointer() - 2 * BYTES);
                date = file.readLong();
            }
            return bars;
        }
    }


    @SneakyThrows
    public long getEndDate(Symbol symbol, Timeframe timeframe) {
        final Path path = getPath(symbol, timeframe);
        if(!Files.exists(path)) return 0;
        try(RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")){
            file.seek(file.length() - BYTES);
            return file.readLong();
        }
    }

}
