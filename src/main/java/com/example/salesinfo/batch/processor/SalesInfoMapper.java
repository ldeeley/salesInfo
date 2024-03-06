package com.example.salesinfo.batch.processor;

import com.example.salesinfo.dto.SalesInfoDTO;
import com.example.salesinfo.entity.SalesInfo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SalesInfoMapper {

    SalesInfo mapToEntity(SalesInfoDTO salesInfoDTO);
}
