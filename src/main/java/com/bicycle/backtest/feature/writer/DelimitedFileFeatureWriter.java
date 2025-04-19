package com.bicycle.backtest.feature.writer;

import com.bicycle.util.Constant;
import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class DelimitedFileFeatureWriter implements FeatureWriter {

    private final String delimiter;
    private boolean areHeadersWritten;
    private final BufferedWriter writer;
    private final OutputStream outputStream;

    public DelimitedFileFeatureWriter(final String name, final String delimiter) throws IOException {
        this.delimiter = delimiter;
        final Path path = Paths.get(Constant.HOME, "reports", name);
        if(null != path.getParent()) Files.createDirectories(path.getParent());
        this.outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        this.writer = new BufferedWriter(new OutputStreamWriter(this.outputStream));
    }


    @Override
    @SneakyThrows
    public void writeHeaders(List<String> headers) {
        if(areHeadersWritten) throw new IllegalStateException("Headers have been already written");
        final String line = String.join(delimiter, headers);
        writer.write(line);
        writer.newLine();
        writer.flush();
        areHeadersWritten = true;
    }

    @Override
    @SneakyThrows
    public void writeValues(List<Object> values) {
        final String line = values.stream()
                .map(String::valueOf)
                .map(this::blankIfInfinityOrNaN)
                .collect(Collectors.joining(delimiter));
        writer.write(line);
        writer.newLine();
        writer.flush();
    }

    private String blankIfInfinityOrNaN(String text){
        return switch (text){
            case "NaN", "Infinity", "-Infinity" -> "";
            default -> text;
        };
    }


    @Override
    public void close() throws Exception {
        this.outputStream.flush();
        this.outputStream.close();
    }
}
