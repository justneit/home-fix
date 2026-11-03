package com.homefix.servicecatalog;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.homefix.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class ServiceCategoryControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void listsSeededCategoriesPubliclyInDisplayOrder() throws Exception {
        mvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].code").value("PLUMBING"))
                .andExpect(jsonPath("$[0].name").value("Sửa ống nước"))
                .andExpect(jsonPath("$[0].active").doesNotExist());
    }
}
