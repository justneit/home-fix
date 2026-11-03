package com.homefix.servicecatalog;

import com.homefix.servicecatalog.dto.ServiceCategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Service catalog")
class ServiceCategoryController {

    private final ServiceCategoryService service;

    ServiceCategoryController(ServiceCategoryService service) {
        this.service = service;
    }

    /** Small, bounded reference list: returned whole rather than paginated. */
    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Active service categories, in display order")
    List<ServiceCategoryResponse> list() {
        return service.listActive();
    }
}
