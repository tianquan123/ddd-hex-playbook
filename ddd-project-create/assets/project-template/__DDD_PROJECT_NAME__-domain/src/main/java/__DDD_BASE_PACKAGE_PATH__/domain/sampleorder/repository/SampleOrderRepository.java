package __DDD_BASE_PACKAGE__.domain.sampleorder.repository;

import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrder;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderId;

import java.util.Optional;

public interface SampleOrderRepository {

    SampleOrder add(SampleOrder order);

    Optional<SampleOrder> findById(SampleOrderId id);

    SampleOrder update(SampleOrder order);
}
