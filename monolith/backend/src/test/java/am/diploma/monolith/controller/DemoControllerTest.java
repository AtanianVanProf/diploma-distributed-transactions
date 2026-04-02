package am.diploma.monolith.controller;

import am.diploma.monolith.service.DemoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DemoController.class)
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DemoService demoService;

    @Test
    @DisplayName("POST /api/demo/reset returns 200 with confirmation message")
    void resetData_returns200WithMessage() throws Exception {
        mockMvc.perform(post("/api/demo/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Database reset to initial state"));
    }

    @Test
    @DisplayName("POST /api/demo/reset calls demoService.resetData()")
    void resetData_callsDemoService() throws Exception {
        mockMvc.perform(post("/api/demo/reset"));

        verify(demoService).resetData();
    }
}
