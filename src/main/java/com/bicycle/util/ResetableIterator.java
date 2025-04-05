package com.bicycle.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ResetableIterator {

    static boolean advance(int index, List<ResetableIterator> iterators) {
        if(index >= iterators.size()) return false;
        final ResetableIterator iterator = iterators.get(index);
        if(iterator.hasNext()) {
            iterator.advance();
            return true;
        } else if(index < iterators.size() - 1) {
            if(advance(index + 1, iterators)) {
                iterator.reset();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    static Map<String, List<Double>> toMap(List<ResetableIterator> iterators){
        final Map<String, List<Double>> result = new HashMap<>();
        iterators.forEach(ResetableIterator::reset);
        do{
            for(int index = 0; index < iterators.size(); index++){
                final ResetableIterator iterator = iterators.get(index);
                final List<Double> values = result.computeIfAbsent(iterator.name(), key -> new ArrayList<>());
                if(iterator instanceof FloatIterator floatIterator) values.add((double) floatIterator.value());
                else if(iterator instanceof IntegerIterator integerIterator) values.add((double) integerIterator.value());
            }
        }while(advance(0, iterators));
        return result;
    }

    
    String toValueString();
    
    boolean isSingleton();
    
    boolean hasNext();
    
    void advance();
    
    void reset();
    
    String name();

}
