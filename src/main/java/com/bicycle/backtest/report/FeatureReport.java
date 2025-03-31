package com.bicycle.backtest.report;

import com.bicycle.backtest.MockPosition;
import com.bicycle.backtest.strategy.trading.MockTradingStrategy;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.util.pool.ObjectPool;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class FeatureReport implements Report, AutoCloseable {

    public static ReportBuilder builder(ReportBuilder delegate, FeatureCaptor featureCaptor){
        return new FeatureReportBuilder(delegate, featureCaptor);
    }

    public static class FeatureCaptor {

        public int count() {
            // TODO Auto-generated method stub
            return 0;
        }

        public void capture(Symbol symbol, Timeframe timeframe, ByteBuffer byteBuffer) {
            // TODO Auto-generated method stub

        }

    }

    private final RandomAccessFile file;
    @Delegate private final Report delegate;
    private final FeatureCaptor featureCaptor;
    private final Map<Integer, ByteBuffer> cache = new HashMap<>();
    private final ObjectPool<ByteBuffer> pool;

    public FeatureReport(Report delegate, FeatureCaptor featureCaptor) {
        this.delegate = delegate;
        this.file = createFile();
        this.featureCaptor = featureCaptor;
        final int byteBufferSize = (featureCaptor.count() + 1) * Float.BYTES;
        this.pool = new ObjectPool<>(() -> ByteBuffer.allocate(byteBufferSize));
    }
    
    @SneakyThrows
    private RandomAccessFile createFile() {
        final File file = Files.createTempFile("bicycle", "features").toFile();
        file.deleteOnExit();
        return new RandomAccessFile(file, "rw");
    }
    
    @Override
    public void open(MockPosition trade) {
        final ByteBuffer byteBuffer = pool.aquire();
        byteBuffer.clear();
        featureCaptor.capture(trade.getSymbol(), trade.getTimeframe(), byteBuffer);
        cache.put(trade.getId(), byteBuffer);
    }

    @Override
    @SneakyThrows
    public void close(MockPosition trade) {
        final ByteBuffer byteBuffer = cache.remove(trade.getId());
        byteBuffer.putFloat(trade.getClosePercentProfitLoss());
        file.write(byteBuffer.array());
        pool.release(byteBuffer);
    }
    
    public int getFeatureCount() {
        return featureCaptor.count();
    }
    
    @Override
    @SuppressWarnings("unchecked") 
    public <T extends Report> T unwrap(Class<T> type) {
        if(type.isAssignableFrom(getClass())) return (T) this;
        return delegate.unwrap(type);
    }
    
    @Override
    public void close() throws Exception {
        file.close();
        cache.clear();
        pool.clear();
    }

}

@RequiredArgsConstructor
class FeatureReportBuilder implements ReportBuilder {

    private final ReportBuilder delegateReportBuilder;
    private final FeatureReport.FeatureCaptor featureCaptor;

    @Override
    public Report build(float initialMargin, MockTradingStrategy tradingStrategy, long startDate, long endDate) {
        final Report delegateReport = delegateReportBuilder.build(initialMargin, tradingStrategy, startDate, endDate);
        return new FeatureReport(delegateReport, featureCaptor);
    }
}