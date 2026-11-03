package com.homefix.servicecatalog;

import com.homefix.servicecatalog.dto.ServiceCategoryResponse;
import org.mapstruct.Mapper;

@Mapper
public interface ServiceCategoryMapper {

    ServiceCategoryResponse toResponse(ServiceCategory category);
}
