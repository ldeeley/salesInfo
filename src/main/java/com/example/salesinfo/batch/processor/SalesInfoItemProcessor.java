package com.example.salesinfo.batch.processor;

import com.example.salesinfo.dto.SalesInfoDTO;
import com.example.salesinfo.entity.SalesInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SalesInfoItemProcessor implements ItemProcessor<SalesInfoDTO, SalesInfo> {

    private final SalesInfoMapper salesInfoMapper;

    @Override
    public SalesInfo process(SalesInfoDTO item) throws Exception {
        System.out.println(" Thread : "+ Thread.currentThread().getName());
        System.out.println(" Process : "+ ProcessHandle.current().pid());
        return salesInfoMapper.mapToEntity(item);
    }
}
