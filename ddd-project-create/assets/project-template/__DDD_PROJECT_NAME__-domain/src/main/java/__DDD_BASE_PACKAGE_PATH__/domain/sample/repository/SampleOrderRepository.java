package __DDD_BASE_PACKAGE__.domain.sample.repository;

import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderAggregate;
import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderId;

import java.util.Optional;

public interface SampleOrderRepository {

    void save(SampleOrderAggregate order);

    Optional<SampleOrderAggregate> findById(SampleOrderId id);
}
