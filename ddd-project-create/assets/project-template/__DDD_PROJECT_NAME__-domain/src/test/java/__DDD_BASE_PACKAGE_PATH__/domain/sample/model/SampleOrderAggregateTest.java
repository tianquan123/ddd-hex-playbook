package __DDD_BASE_PACKAGE__.domain.sample.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SampleOrderAggregateTest {

    @Test
    void creates_order_in_created_status() {
        SampleOrderAggregate order = SampleOrderAggregate.create(
                new SampleOrderId("order-1"), "SKU-1", 2);

        assertEquals(SampleOrderStatus.CREATED, order.status());
        assertEquals("SKU-1", order.productCode());
        assertEquals(2, order.quantity());
    }

    @Test
    void rehydrates_persisted_order() {
        SampleOrderAggregate order = SampleOrderAggregate.rehydrate(
                new SampleOrderId("order-1"), "SKU-1", 2, SampleOrderStatus.CREATED);

        assertEquals("order-1", order.id().getValue());
        assertEquals("SKU-1", order.productCode());
        assertEquals(2, order.quantity());
        assertEquals(SampleOrderStatus.CREATED, order.status());
    }

    @Test
    void rejects_blank_product_code() {
        assertThrows(IllegalArgumentException.class,
                () -> SampleOrderAggregate.create(new SampleOrderId("order-1"), " ", 2));
    }

    @Test
    void rejects_non_positive_quantity() {
        assertThrows(IllegalArgumentException.class,
                () -> SampleOrderAggregate.create(new SampleOrderId("order-1"), "SKU-1", 0));
    }
}
