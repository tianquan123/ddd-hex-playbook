package __DDD_BASE_PACKAGE__.trigger.rpc.sampleorder;

import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderAppService;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderQueryService;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderStatus;
import __DDD_BASE_PACKAGE__.model.common.response.ApiResponse;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.CreateSampleOrderRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.response.SampleOrderResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SampleOrderProviderTest {

    private final SampleOrderAppService appService = mock(SampleOrderAppService.class);
    private final SampleOrderQueryService queryService = mock(SampleOrderQueryService.class);
    private final SampleOrderProvider provider = new SampleOrderProvider(appService, queryService);

    @Test
    void exposes_successful_facade_contract() {
        when(appService.create(any())).thenReturn(view("order-1"));
        CreateSampleOrderRequest request = new CreateSampleOrderRequest();
        request.setProductCode("SKU-1");
        request.setQuantity(2);

        ApiResponse<SampleOrderResponse> response = provider.create(request);

        assertTrue(response.isSuccess());
        assertEquals("order-1", response.getData().getId());
    }

    @Test
    void returns_stable_failure_for_missing_order() {
        when(queryService.get(any())).thenReturn(Optional.empty());

        ApiResponse<SampleOrderResponse> response = provider.get("missing");

        assertFalse(response.isSuccess());
        assertEquals("SAMPLE_ORDER_NOT_FOUND", response.getCode());
    }

    private static SampleOrderView view(String id) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 10, 0);
        return new SampleOrderView(id, "SKU-1", 2, SampleOrderStatus.PENDING, now, now);
    }
}
