package __DDD_BASE_PACKAGE__.infra.sample.adapter;

import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderAggregate;
import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.domain.sample.repository.SampleOrderRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemorySampleOrderRepository implements SampleOrderRepository {

    private final Map<SampleOrderId, SampleOrderAggregate> orders = new ConcurrentHashMap<>();

    @Override
    public void save(SampleOrderAggregate order) {
        orders.put(order.id(), order);
    }

    @Override
    public Optional<SampleOrderAggregate> findById(SampleOrderId id) {
        return Optional.ofNullable(orders.get(id));
    }
}
