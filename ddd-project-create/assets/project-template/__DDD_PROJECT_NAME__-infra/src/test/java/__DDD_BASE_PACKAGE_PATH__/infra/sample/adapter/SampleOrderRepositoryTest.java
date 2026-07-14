package __DDD_BASE_PACKAGE__.infra.sample.adapter;

import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderAggregate;
import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.infra.sample.persistence.SampleOrderMapper;
import __DDD_BASE_PACKAGE__.infra.sample.persistence.SampleOrderPO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SampleOrderRepositoryTest {

    @Test
    void saves_order_as_persistence_object() {
        FakeSampleOrderMapper mapper = new FakeSampleOrderMapper();
        SampleOrderRepository repository = new SampleOrderRepository(mapper);

        repository.save(SampleOrderAggregate.create(new SampleOrderId("order-1"), "SKU-1", 2));

        assertEquals("order-1", mapper.stored.getId());
        assertEquals("SKU-1", mapper.stored.getProductCode());
        assertEquals(2, mapper.stored.getQuantity());
        assertEquals("CREATED", mapper.stored.getStatus());
    }

    @Test
    void loads_order_from_persistence_object() {
        FakeSampleOrderMapper mapper = new FakeSampleOrderMapper();
        mapper.stored = new SampleOrderPO();
        mapper.stored.setId("order-1");
        mapper.stored.setProductCode("SKU-1");
        mapper.stored.setQuantity(2);
        mapper.stored.setStatus("CREATED");
        SampleOrderRepository repository = new SampleOrderRepository(mapper);

        SampleOrderAggregate order = repository.findById(new SampleOrderId("order-1")).orElseThrow();

        assertEquals("order-1", order.id().getValue());
        assertEquals("SKU-1", order.productCode());
        assertEquals(2, order.quantity());
        assertEquals("CREATED", order.status().name());
    }

    private static final class FakeSampleOrderMapper implements SampleOrderMapper {
        private SampleOrderPO stored;

        @Override
        public int upsert(SampleOrderPO order) {
            stored = order;
            return 1;
        }

        @Override
        public SampleOrderPO selectById(String id) {
            return stored != null && stored.getId().equals(id) ? stored : null;
        }
    }
}
