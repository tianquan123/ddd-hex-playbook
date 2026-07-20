package __DDD_BASE_PACKAGE__.trigger.http.sampleorder;

import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderAppService;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderQueryService;
import __DDD_BASE_PACKAGE__.domain.sampleorder.exception.SampleOrderNotFoundException;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderStatus;
import __DDD_BASE_PACKAGE__.trigger.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SampleOrderController.class)
@ContextConfiguration(classes = SampleOrderController.class)
@Import(GlobalExceptionHandler.class)
class SampleOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SampleOrderAppService appService;

    @MockBean
    private SampleOrderQueryService queryService;

    @Test
    void creates_sample_order() throws Exception {
        when(appService.create(any())).thenReturn(view("order-1", SampleOrderStatus.PENDING));

        mockMvc.perform(post("/sample/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productCode\":\"SKU-1\",\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("order-1"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void gets_sample_order() throws Exception {
        when(queryService.get(any())).thenReturn(Optional.of(view("order-1", SampleOrderStatus.CONFIRMED)));

        mockMvc.perform(get("/sample/orders/order-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void maps_missing_order_to_not_found_contract() throws Exception {
        when(queryService.get(any())).thenThrow(
                new SampleOrderNotFoundException(new SampleOrderId("missing")));

        mockMvc.perform(get("/sample/orders/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("SAMPLE_ORDER_NOT_FOUND"));
    }

    @Test
    void maps_validation_failure_to_stable_contract() throws Exception {
        mockMvc.perform(post("/sample/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productCode\":\" \",\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(appService);
    }

    private static SampleOrderView view(String id, SampleOrderStatus status) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 10, 0);
        return new SampleOrderView(id, "SKU-1", 2, status, now, now);
    }
}
