package com.homefix.servicecatalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    List<ServiceCategory> findByActiveTrueOrderByDisplayOrderAscNameAsc();
}
