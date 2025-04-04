package com.bicycle.core.bar.repository;

import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Cursor;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.repository.SymbolRepository;
import com.bicycle.util.Constant;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

@RequiredArgsConstructor
public class FileSystemDateIndexedBarRepository {
    private static final int BYTES = Integer.BYTES + Long.BYTES + 4 * Float.BYTES + Integer.BYTES;
    
    private final SymbolRepository symbolRepository;
    private final Map<Path, DateLocationIndex> cache = new HashMap<>();
    
    private Path getPath(Exchange exchange, Timeframe timeframe) {
        return Paths.get(Constant.HOME, "bars", exchange.name(), timeframe.name(), "ALL");
    }
    
    private DateLocationIndex getIndex(Path path) {
        return cache.computeIfAbsent(path, DateLocationIndex::new);
    }
    
    public long getEndDate(Exchange exchange, Timeframe timeframe) {
        final Path path = getPath(exchange, timeframe);
        if(!Files.exists(path)) return 0L;
        return getIndex(path).tail();
    }

    @SneakyThrows
    public Cursor<Bar> get(Exchange exchange, Timeframe timeframe) {
        final Path path = getPath(exchange, timeframe);
        if(!Files.exists(path) || BYTES > Files.size(path)) return Cursor.EMPTY;
        final FileChannel fileChannel = FileChannel.open(path);
        final long fileChannelSize = fileChannel.size();
        final MappedByteBuffer buffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannelSize);
        return new Cursor<>() {
            @Override public void close() throws Exception { fileChannel.close(); }
            @Override @SneakyThrows public int size() { return (int) (fileChannelSize / BYTES); }
            @Override public void advance(Bar bar) { read(bar, exchange, timeframe, buffer); }
        };
    }

    @SneakyThrows
    public Cursor<Bar> get(Exchange exchange, Timeframe timeframe, long fromInclusive, long toInclusive) {
        final Path path = getPath(exchange, timeframe);
        if(!Files.exists(path) || BYTES > Files.size(path)) return Cursor.EMPTY;

        final FileChannel fileChannel = FileChannel.open(path);
        
        final DateLocationIndex index = getIndex(path);
        final int count = index.count(fromInclusive, toInclusive);
        
        final long fileChannelSize = fileChannel.size();
        final long fromLocation = Math.min(index.location(fromInclusive), fileChannelSize);
        final long toLocation = Math.min(index.location(toInclusive) + index.count(toInclusive) * BYTES, fileChannelSize);
        
        final MappedByteBuffer buffer = fileChannel.map(MapMode.READ_ONLY, fromLocation, toLocation - fromLocation);
        return new Cursor<>() {
            @Override public void close() throws Exception { fileChannel.close(); }
            @Override @SneakyThrows public int size() { return count; }
            @Override public void advance(Bar bar) {
                read(bar, exchange, timeframe, buffer); 
            }
        };
    }
    
    private void read(Bar bar, Exchange exchange, Timeframe timeframe, ByteBuffer buffer) {
        bar.symbol(symbolRepository.findByToken(buffer.getInt(), exchange));
        bar.timeframe(timeframe);
        bar.date(buffer.getLong());
        bar.open(buffer.getFloat());
        bar.high(buffer.getFloat());
        bar.low(buffer.getFloat());
        bar.close(buffer.getFloat());
        bar.volume(buffer.getInt());
    }
    
    @SneakyThrows
    private void write(Bar bar, DataOutput output) {
        output.writeInt(bar.symbol().token());
        output.writeLong(bar.date());
        output.writeFloat(bar.open());
        output.writeFloat(bar.high());
        output.writeFloat(bar.low());
        output.writeFloat(bar.close());
        output.writeInt(bar.volume());
    }
    
    @SneakyThrows
    public void persist(Exchange exchange, Timeframe timeframe, Map<Long, Queue<Bar>> data) {
        if(data.isEmpty()) return;
        final Path path = getPath(exchange, timeframe);
        if(!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }
        try(RandomAccessFile file = new RandomAccessFile(path.toFile(), "rw")){
            file.seek(file.length());
            final DateLocationIndex index = getIndex(path);
            data.entrySet().stream().sorted(Entry.comparingByKey()).forEach(entry -> {
                if(100 < entry.getValue().size()) {
                    try {
                        index.set(entry.getKey(), file.getFilePointer(), entry.getValue().size());
                        for(Bar bar : entry.getValue()) write(bar, file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    
}
