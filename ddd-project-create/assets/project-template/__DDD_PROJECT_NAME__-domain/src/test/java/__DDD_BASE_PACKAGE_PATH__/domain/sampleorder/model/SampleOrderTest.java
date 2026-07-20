package __DDD_BASE_PACKAGE__.domain.sampleorder.model;

import __DDD_BASE_PACKAGE__.domain.sampleorder.exception.InvalidSampleOrderStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SampleOrderTest {

    @Test
    void creates_pending_order() {
        SampleOrder order = SampleOrder.create(new SampleOrderId("order-1"), "SKU-1", 2);

        assertEquals("order-1", order.getId().getValue());
        assertEquals("SKU-1", order.getProductCode());
        assertEquals(2, order.getQuantity());
        assertEquals(SampleOrderStatus.PENDING, order.getStatus());
    }

    @Test
    void confirms_pending_order() {
        SampleOrder order = SampleOrder.create(new SampleOrderId("order-1"), "SKU-1", 2);

        order.confirm();

        assertEquals(SampleOrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void cancels_pending_order() {
        SampleOrder order = SampleOrder.create(new SampleOrderId("order-1"), "SKU-1", 2);

        order.cancel();

        assertEquals(SampleOrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void rejects_transition_from_terminal_state() {
        SampleOrder confirmed = SampleOrder.create(new SampleOrderId("order-1"), "SKU-1", 2);
        confirmed.confirm();
        SampleOrder cancelled = SampleOrder.create(new SampleOrderId("order-2"), "SKU-2", 1);
        cancelled.cancel();

        assertThrows(InvalidSampleOrderStateException.class, confirmed::confirm);
        assertThrows(InvalidSampleOrderStateException.class, confirmed::cancel);
        assertThrows(InvalidSampleOrderStateException.class, cancelled::confirm);
        assertThrows(InvalidSampleOrderStateException.class, cancelled::cancel);
    }

    @Test
    void rejects_invalid_identity_and_order_data() {
        assertThrows(IllegalArgumentException.class, () -> new SampleOrderId(" "));
        assertThrows(IllegalArgumentException.class,
                () -> SampleOrder.create(new SampleOrderId("order-1"), " ", 2));
        assertThrows(IllegalArgumentException.class,
                () -> SampleOrder.create(new SampleOrderId("order-1"), "SKU-1", 0));
    }
}
