package __DDD_BASE_PACKAGE__.trigger.http.sampleorder;

import __DDD_BASE_PACKAGE__.application.sampleorder.model.PageView;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderStatus;
import __DDD_BASE_PACKAGE__.model.common.response.PageResponse;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.CreateSampleOrderRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.request.SampleOrderPageRequest;
import __DDD_BASE_PACKAGE__.model.sampleorder.response.SampleOrderResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SampleOrderHttpConverterTest {

    @Test
    void converts_transport_models_without_leaking_them_into_application() {
        CreateSampleOrderRequest create = new CreateSampleOrderRequest();
        create.setProductCode("SKU-1");
        create.setQuantity(2);
        assertEquals("SKU-1", SampleOrderHttpConverter.INSTANCE.toCommand(create).getProductCode());

        SampleOrderPageRequest request = new SampleOrderPageRequest();
        request.setStatus("CONFIRMED");
        PageView<SampleOrderView> page = new PageView<>(1, 1, 20, List.of(view()));
        PageResponse<SampleOrderResponse> response = SampleOrderHttpConverter.INSTANCE.toResponse(page);

        assertEquals(SampleOrderStatus.CONFIRMED,
                SampleOrderHttpConverter.INSTANCE.toCriteria(request).getStatus());
        assertEquals("order-1", response.getRecords().getFirst().getId());
    }

    private static SampleOrderView view() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 10, 0);
        return new SampleOrderView("order-1", "SKU-1", 2, SampleOrderStatus.CONFIRMED, now, now);
    }
}
