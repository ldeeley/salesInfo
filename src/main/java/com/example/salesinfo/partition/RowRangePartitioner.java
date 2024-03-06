package com.example.salesinfo.partition;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;


public class RowRangePartitioner implements Partitioner {
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        int min = 1;
        int max = 1000;
        int targetSize = (max - min)/gridSize + 1;
        System.out.println("targetSize : " + targetSize);
        Map<String,ExecutionContext> result = new HashMap<>();

        int number = 0;
        int start = min;
        int end = start + targetSize - 1;

        while (start<=max) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if (end >=max){
                end = max;
            }
            value.putInt("minValue",start);
            value.putInt("maxValue",end);
            start += targetSize;
            end += targetSize;
            number++;
        }

        return result;
    }
}
