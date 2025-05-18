package com.bicycle.backtest.workflow;

import com.bicycle.backtest.workflow.stage.feature.FeatureClusteringStage;
import com.bicycle.client.kite.adapter.KiteSymbolDataProvider;
import com.bicycle.core.bar.Bar;
import com.bicycle.core.bar.Timeframe;
import com.bicycle.core.bar.repository.BarRepository;
import com.bicycle.core.bar.repository.FileSystemBarRepository;
import com.bicycle.core.symbol.Exchange;
import com.bicycle.core.symbol.Symbol;
import com.bicycle.core.symbol.provider.SymbolDataProvider;
import com.bicycle.core.symbol.repository.CacheSymbolRepository;
import com.bicycle.core.symbol.repository.SymbolRepository;
import smile.data.DataFrame;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SymbolClusteringWorkflow {
    private static final int BAR_COUNT = 500;
    private static final Exchange EXCHANGE = Exchange.NSE;
    private static final Timeframe TIMEFRAME = Timeframe.D;
    private static final double MIN_MEAN_VOLUME = 100_000;
    private static final double MIN_MEAN_TURNOVER = 100_00_000;

    public static void main(String[] args) {
        final SymbolDataProvider symbolDataProvider = new KiteSymbolDataProvider();
        final SymbolRepository symbolRepository = new CacheSymbolRepository(symbolDataProvider);
        final BarRepository barRepository = new FileSystemBarRepository(symbolRepository);

        final Map<Symbol, double[]> symbolData = symbolRepository.findByExchange(EXCHANGE).stream()
                .map(symbol -> barRepository.get(symbol, TIMEFRAME, BAR_COUNT).toList(Bar::new))
                .filter(bars -> null != bars && !bars.isEmpty() && BAR_COUNT == bars.size())
                .filter(SymbolClusteringWorkflow::barPredicate)
                .collect(Collectors.toMap(bars -> bars.getFirst().symbol(), SymbolClusteringWorkflow::transform));
        System.out.printf("Clustering %d symbols\n", symbolData.size());
        final DataFrame dataFrame = toDataFrame(symbolData);
        final List<List<String>> results = new FeatureClusteringStage().findClusterNames(dataFrame);
    }

    private static boolean barPredicate(List<Bar> bars){
        double meanTurnover = 0, meanVolume = 0;
        for(Bar bar : bars){
            meanTurnover += bar.close() * bar.volume();
            meanVolume += bar.volume();
        }
        meanTurnover /= bars.size();
        meanVolume /= bars.size();
        return meanTurnover >= MIN_MEAN_TURNOVER && meanVolume >= MIN_MEAN_VOLUME;
    }

    private static double[] transform(List<Bar> bars){
        final double[] values = new double[bars.size()];
        for(int index = 1; index < bars.size(); index++){
            final Bar currentBar = bars.get(index);
            final Bar previousBar = bars.get(index - 1);
            values[index] = (currentBar.close() - previousBar.close()) / previousBar.close();
        }
        return values;
    }

    private static DataFrame toDataFrame(Map<Symbol, double[]> symbolData){
        final List<Symbol> symbols = symbolData.keySet().stream().sorted(Comparator.comparing(Symbol::code)).toList();
        final String[] names = symbols.stream().map(Symbol::code).toArray(String[]::new);
        final double[][] data = new double[BAR_COUNT][symbols.size()];
        for(int symbolIndex = 0; symbolIndex < symbols.size(); symbolIndex++){
            final Symbol symbol = symbols.get(symbolIndex);
            final double[] values = symbolData.get(symbol);
            for(int valueIndex = 0; valueIndex < values.length; valueIndex++){
                data[valueIndex][symbolIndex] = values[valueIndex];
            }
        }
        return DataFrame.of(data, names);
    }

}
