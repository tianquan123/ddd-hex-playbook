package __DDD_BASE_PACKAGE__.application.sampleorder.mapping;

import __DDD_BASE_PACKAGE__.application.sampleorder.command.CreateSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrder;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SampleOrderApplicationConverterTest {

    @Test
    void converts_command_to_valid_domain_and_domain_to_view() {
        SampleOrderId id = new SampleOrderId("order-1");
        SampleOrder created = SampleOrderApplicationConverter.INSTANCE.toDomain(
                new CreateSampleOrderCommand("SKU-1", 2), id);

        assertEquals(id, created.getId());
        assertEquals(SampleOrderStatus.PENDING, created.getStatus());

        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 10, 0);
        SampleOrder persisted = SampleOrder.reconstitute(id, "SKU-1", 2, SampleOrderStatus.PENDING, now, now);
        SampleOrderView view = SampleOrderApplicationConverter.INSTANCE.toView(persisted);

        assertEquals("order-1", view.getId());
        assertEquals(now, view.getCreateTime());
    }
}
