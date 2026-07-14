package __DDD_BASE_PACKAGE__.infra.sample.adapter;

import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderAggregate;
import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class InMemorySampleOrderRepositoryTest {

    @Test
    void saves_and_loads_order() {
        InMemorySampleOrderRepository repository = new InMemorySampleOrderRepository();
        SampleOrderId id = new SampleOrderId("order-1");
        SampleOrderAggregate order = SampleOrderAggregate.create(id, "SKU-1", 2);

        repository.save(order);

        assertSame(order, repository.findById(id).orElseThrow());
    }
}
