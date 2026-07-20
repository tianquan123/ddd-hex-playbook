package __DDD_BASE_PACKAGE__.application.sampleorder.service.impl;

import __DDD_BASE_PACKAGE__.application.sampleorder.command.CancelSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.command.ConfirmSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.command.CreateSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.mapping.SampleOrderApplicationConverter;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.application.sampleorder.service.SampleOrderAppService;
import __DDD_BASE_PACKAGE__.domain.sampleorder.exception.SampleOrderNotFoundException;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrder;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.domain.sampleorder.repository.SampleOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
public class SampleOrderAppServiceImpl implements SampleOrderAppService {

    private final SampleOrderRepository repository;

    @Override
    @Transactional
    public SampleOrderView create(CreateSampleOrderCommand command) {
        SampleOrder order = SampleOrderApplicationConverter.INSTANCE.toDomain(
                command, new SampleOrderId(UUID.randomUUID().toString()));
        return SampleOrderApplicationConverter.INSTANCE.toView(repository.add(order));
    }

    @Override
    @Transactional
    public SampleOrderView confirm(ConfirmSampleOrderCommand command) {
        SampleOrder order = load(command.getId());
        order.confirm();
        return SampleOrderApplicationConverter.INSTANCE.toView(repository.update(order));
    }

    @Override
    @Transactional
    public SampleOrderView cancel(CancelSampleOrderCommand command) {
        SampleOrder order = load(command.getId());
        order.cancel();
        return SampleOrderApplicationConverter.INSTANCE.toView(repository.update(order));
    }

    private SampleOrder load(String value) {
        SampleOrderId id = new SampleOrderId(value);
        return repository.findById(id).orElseThrow(() -> new SampleOrderNotFoundException(id));
    }
}
