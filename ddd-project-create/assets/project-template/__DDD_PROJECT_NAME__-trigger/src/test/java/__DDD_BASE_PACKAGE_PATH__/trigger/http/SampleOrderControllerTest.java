package __DDD_BASE_PACKAGE__.trigger.http;

import __DDD_BASE_PACKAGE__.application.sample.SampleOrderAppService;
import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderAggregate;
import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SampleOrderController.class)
@ContextConfiguration(classes = SampleOrderController.class)
class SampleOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SampleOrderAppService appService;

    @Test
    void creates_sample_order() throws Exception {
        when(appService.create(any())).thenReturn(
                SampleOrderAggregate.create(new SampleOrderId("order-1"), "SKU-1", 2));

        mockMvc.perform(post("/sample/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productCode\":\"SKU-1\",\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("order-1"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }
}
