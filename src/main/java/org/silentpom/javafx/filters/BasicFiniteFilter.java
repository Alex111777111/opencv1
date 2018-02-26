package org.silentpom.javafx.filters;

import java.util.*;
import java.util.function.DoubleFunction;
import java.util.stream.DoubleStream;

/**
 * Created by Vlad on 24.02.2018.
 */
public class BasicFiniteFilter<T> {
    private ArrayDeque<T> data;
    private double[] filter;

    public BasicFiniteFilter(double... coefs) {
        double sum = DoubleStream.of(coefs).sum();
        filter = DoubleStream.of(coefs).map(x -> x / sum).toArray();

        data = new ArrayDeque<T>(filter.length);
    }

    public void reset() {
        data.clear();
    }

    protected void push(T elem) {
        data.add(elem);
        while(data.size() > filter.length) {
            data.remove();
        }
    }

    protected boolean isEmpty() {
        return data.isEmpty();
    }


    protected boolean isFull() {
        return data.size() == filter.length;
    }

    protected double[] cutFilter() {
        if (isFull()) {
            return filter;
        }

        double sum = DoubleStream.of(filter).limit(data.size()).sum();
        return DoubleStream.of(filter).limit(data.size()).map(x -> x / sum).toArray();
    }

    protected double filter(Extractor<T> func, double[] newFilter) {
        double res = 0;
        int i =0 ;
        Iterator<T> iter =  data.descendingIterator();
        while (iter.hasNext()) {
            double elem = func.extract(iter.next());
            res+=elem*newFilter[i];
            i++;
        }

        return res;
    }

    public interface Extractor<T> {
        double extract(T elem);
    }
}
