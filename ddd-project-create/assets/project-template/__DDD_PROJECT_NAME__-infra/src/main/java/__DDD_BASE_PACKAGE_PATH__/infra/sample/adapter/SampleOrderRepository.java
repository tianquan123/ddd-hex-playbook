package __DDD_BASE_PACKAGE__.infra.sample.adapter;

import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderAggregate;
import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderStatus;
import __DDD_BASE_PACKAGE__.infra.sample.persistence.SampleOrderMapper;
import __DDD_BASE_PACKAGE__.infra.sample.persistence.SampleOrderPO;

import java.util.Optional;

public final class SampleOrderRepository
        implements __DDD_BASE_PACKAGE__.domain.sample.repository.SampleOrderRepository {

    private final SampleOrderMapper mapper;

    public SampleOrderRepository(SampleOrderMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(SampleOrderAggregate order) {
        SampleOrderPO persistenceObject = new SampleOrderPO();
        persistenceObject.setId(order.id().getValue());
        persistenceObject.setProductCode(order.productCode());
        persistenceObject.setQuantity(order.quantity());
        persistenceObject.setStatus(order.status().name());
        mapper.upsert(persistenceObject);
    }

    @Override
    public Optional<SampleOrderAggregate> findById(SampleOrderId id) {
        return Optional.ofNullable(mapper.selectById(id.getValue()))
                .map(this::toAggregate);
    }

    private SampleOrderAggregate toAggregate(SampleOrderPO persistenceObject) {
        return SampleOrderAggregate.rehydrate(
                new SampleOrderId(persistenceObject.getId()),
                persistenceObject.getProductCode(),
                persistenceObject.getQuantity(),
                SampleOrderStatus.valueOf(persistenceObject.getStatus()));
    }
}
