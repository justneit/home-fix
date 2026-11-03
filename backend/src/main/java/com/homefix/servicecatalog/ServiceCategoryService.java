package com.homefix.servicecatalog;

import com.homefix.servicecatalog.dto.ServiceCategoryResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServiceCategoryService {

    private final ServiceCategoryRepository categories;
    private final ServiceCategoryMapper mapper;

    public ServiceCategoryService(ServiceCategoryRepository categories, ServiceCategoryMapper mapper) {
        this.categories = categories;
        this.mapper = mapper;
    }

    public List<ServiceCategoryResponse> listActive() {
        return categories.findByActiveTrueOrderByDisplayOrderAscNameAsc().stream()
                .map(mapper::toResponse)
                .toList();
    }
}
