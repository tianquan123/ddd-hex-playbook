package __DDD_BASE_PACKAGE__.application.sample;

import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderAggregate;
import __DDD_BASE_PACKAGE__.domain.sample.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.domain.sample.repository.SampleOrderRepository;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public final class SampleOrderAppService {

    private final SampleOrderRepository repository;
    private final Supplier<String> idSupplier;

    public SampleOrderAppService(SampleOrderRepository repository) {
        this(repository, () -> UUID.randomUUID().toString());
    }

    public SampleOrderAppService(SampleOrderRepository repository, Supplier<String> idSupplier) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.idSupplier = Objects.requireNonNull(idSupplier, "idSupplier");
    }

    public SampleOrderAggregate create(CreateSampleOrderCommand command) {
        Objects.requireNonNull(command, "command");
        SampleOrderAggregate order = SampleOrderAggregate.create(
                new SampleOrderId(idSupplier.get()), command.productCode(), command.quantity());
        repository.save(order);
        return order;
    }
}
