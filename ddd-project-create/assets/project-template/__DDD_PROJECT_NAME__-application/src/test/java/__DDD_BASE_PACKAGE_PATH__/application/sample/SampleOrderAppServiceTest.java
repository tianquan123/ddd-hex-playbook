package __DDD_BASE_PACKAGE__.application.sample;

import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderAggregate;
import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.domain.sample.repository.SampleOrderRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SampleOrderAppServiceTest {

    @Test
    void creates_and_saves_order() {
        CapturingRepository repository = new CapturingRepository();
        SampleOrderAppService service = new SampleOrderAppService(repository, () -> "order-1");

        SampleOrderAggregate created = service.create(new CreateSampleOrderCommand("SKU-1", 3));

        assertSame(created, repository.saved);
        assertEquals("order-1", created.id().value());
        assertEquals(3, created.quantity());
    }

    private static final class CapturingRepository implements SampleOrderRepository {
        private SampleOrderAggregate saved;

        @Override
        public void save(SampleOrderAggregate order) {
            this.saved = order;
        }

        @Override
        public Optional<SampleOrderAggregate> findById(SampleOrderId id) {
            return Optional.ofNullable(saved).filter(order -> order.id().equals(id));
        }
    }
}
